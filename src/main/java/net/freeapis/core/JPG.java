package net.freeapis.core;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;

/**
 * freeapis,Inc.
 * Copyright(C): 2016
 * NOTE: we only support baseline and progressive JPGs here
 * due to the structure of the loader class, we only get a buffer
 * with a maximum size of 4096 bytes. so if the SOF marker is outside
 * if this range we can't detect the file size correctly.
 *
 * @author Administrator
 * @date 2019年09月18日 16:38
 */
class JPG implements Image.Parser {

    private static final String JPG_HEADER = "ffd8";
    private static final String EXIF_MARKER = "45786966";
    private static final int APP1_DATA_SIZE_BYTES = 2;
    private static final int EXIF_HEADER_BYTES = 6;
    private static final int TIFF_BYTE_ALIGN_BYTES = 2;
    private static final String BIG_ENDIAN_BYTE_ALIGN = "4d4d";
    private static final String LITTLE_ENDIAN_BYTE_ALIGN = "4949";

    // Each entry is exactly 12 bytes
    private static final int IDF_ENTRY_BYTES = 12;
    private static final int NUM_DIRECTORY_ENTRIES_BYTES = 2;

    @Override
    public boolean isValid(ByteBuffer buffer) {
        String SOIMarker = ByteUtil.readHexString(buffer, 0, 2);
        return JPG_HEADER.equals(SOIMarker);
    }

    @Override
    public Pair<Integer, Integer> size(ByteBuffer buffer) {
        // Skip 4 chars, they are for signature
        buffer = ByteUtil.slice(buffer, 4);

        int orientation = 0;
        byte next;
        while (buffer.limit() > 0) {
            // read length of the next block
            int i = ByteUtil.readUInt16BE(buffer, 0);

            if (isEXIF(buffer)) {
                orientation = validateExifBlock(buffer, i);
            }

            // ensure correct format
            validateBuffer(buffer, i);

            // 0xFFC0 is baseline standard(SOF)
            // 0xFFC1 is baseline optimized(SOF)
            // 0xFFC2 is progressive(SOF2)
            next = buffer.get(i + 1);
            if (next == (byte)0xC0 || next == (byte)0xC1 || next == (byte)0xC2) {
                Pair<Integer, Integer> size = extractSize(buffer, i + 5);

                // TODO: is orientation=0 a valid answer here?
                if (orientation != -1) {
                    return size;
                }
                return size;
            }
            // move to the next block
            buffer = ByteUtil.slice(buffer, i + 2);
        }
        return null;
    }

    private boolean isEXIF(ByteBuffer buffer) {
        return EXIF_MARKER.equals(ByteUtil.readHexString(buffer, 2, 6));
    }

    private Pair<Integer, Integer> extractSize(ByteBuffer buffer, int index) {
        return Pair.of(
                ByteUtil.readUInt16BE(buffer, index + 2),
                ByteUtil.readUInt16BE(buffer, index)
        );
    }

    private int validateExifBlock(ByteBuffer buffer, int index) {
        // Skip APP1 Data Size
        ByteBuffer exifBlock = ByteUtil.slice(buffer, APP1_DATA_SIZE_BYTES, index);

        // Consider byte alignment
        String byteAlign = ByteUtil.readHexString(buffer,
                EXIF_HEADER_BYTES, EXIF_HEADER_BYTES + TIFF_BYTE_ALIGN_BYTES);

        // Ignore Empty EXIF. Validate byte alignment
        boolean isBigEndian = BIG_ENDIAN_BYTE_ALIGN.equals(byteAlign);
        boolean isLittleEndian = LITTLE_ENDIAN_BYTE_ALIGN.equals(byteAlign);

        if (isBigEndian || isLittleEndian) {
            return extractOrientation(exifBlock, isBigEndian);
        }

        return -1;
    }

    private int extractOrientation(ByteBuffer exifBlock, boolean isBigEndian) {
        // TODO: assert that this contains 0x002A
        // let STATIC_MOTOROLA_TIFF_HEADER_BYTES = 2
        // let TIFF_IMAGE_FILE_DIRECTORY_BYTES = 4

        // TODO: derive from TIFF_IMAGE_FILE_DIRECTORY_BYTES
        int idfOffset = 8;

        // IDF osset works from right after the header bytes
        // (so the offset includes the tiff byte align)
        int offset = EXIF_HEADER_BYTES + idfOffset;

        int idfDirectoryEntries = ByteUtil.readUInt16(exifBlock, offset, isBigEndian);

        for (int directoryEntryNumber = 0; directoryEntryNumber < idfDirectoryEntries; directoryEntryNumber++) {
            int start = offset + NUM_DIRECTORY_ENTRIES_BYTES + (directoryEntryNumber * IDF_ENTRY_BYTES);
            int end = start + IDF_ENTRY_BYTES;

            // Skip on corrupt EXIF blocks
            if (start > exifBlock.limit()) {
                return -1;
            }

            ByteBuffer block = ByteUtil.slice(exifBlock, start, end);
            int tagNumber = ByteUtil.readUInt16(block, 0, isBigEndian);

            // 0x0112 (decimal: 274) is the `orientation` tag ID
            if (tagNumber == 274) {
                int dataFormat = ByteUtil.readUInt16(block, 2, isBigEndian);
                if (dataFormat != 3) {
                    return -1;
                }

                // unsinged int has 2 bytes per component
                // if there would more than 4 bytes in total it's a pointer
                long numberOfComponents = ByteUtil.readUInt32(block, 4, isBigEndian);
                if (numberOfComponents != 1) {
                    return -1;
                }

                return ByteUtil.readUInt16(block, 8, isBigEndian);
            }
        }
        return -1;
    }

    private void validateBuffer(ByteBuffer buffer, int index) {
        // index should be within buffer limits
        if (index > buffer.limit()) {
            throw new IllegalStateException("Corrupt JPG, exceeded buffer limits");
        }
        // Every JPEG block must begin with a 0xFF
        if (buffer.get(index) != (byte)0xFF) {
            throw new IllegalStateException("Invalid JPG, marker table corrupted");
        }
    }
}