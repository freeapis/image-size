package net.freeapis.core;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;

/**
 * freeapis,Inc.
 * Copyright(C): 2016
 *
 * @author Administrator
 * @date 2019年09月18日 18:29
 */
class ICO implements Image.Parser {

    private static final int TYPE_ICON = 1;

    /**
     * ICON Header
     * <p>
     * | Offset | Size | Purpose |
     * | 0	    | 2    | Reserved. Must always be 0.  |
     * | 2      | 2    | Image type: 1 for icon (.ICO) image, 2 for cursor (.CUR) image. Other values are invalid. |
     * | 4      | 2    | Number of images in the file. |
     */
    private static final int SIZE_HEADER = 2 + 2 + 2; // 6

    /**
     * Image Entry
     * <p>
     * | Offset | Size | Purpose |
     * | 0	    | 1    | Image width in pixels. Can be any number between 0 and 255. Value 0 means width is 256 pixels. |
     * | 1      | 1    | Image height in pixels. Can be any number between 0 and 255. Value 0 means height is 256 pixels. |
     * | 2      | 1    | Number of colors in the color palette. Should be 0 if the image does not use a color palette. |
     * | 3      | 1    | Reserved. Should be 0. |
     * | 4      | 2    | ICO format: Color planes. Should be 0 or 1. |
     * |        |      | CUR format: The horizontal coordinates of the hotspot in number of pixels from the left. |
     * | 6      | 2    | ICO format: Bits per pixel. |
     * |        |      | CUR format: The vertical coordinates of the hotspot in number of pixels from the top. |
     * | 8      | 4    | The size of the image's data in bytes |
     * | 12     | 4    | The offset of BMP or PNG data from the beginning of the ICO/CUR file |
     */
    private static final int SIZE_IMAGE_ENTRY = 1 + 1 + 1 + 1 + 2 + 2 + 4 + 4; // 16

    @Override
    public boolean isValid(ByteBuffer buffer) {
        if (ByteUtil.readUInt16LE(buffer,0) != 0) {
            return false;
        }
        return ByteUtil.readUInt16LE(buffer,2) == TYPE_ICON;
    }

    @Override
    public Pair<Integer, Integer> size(ByteBuffer buffer) {
        int nbImages = ByteUtil.readUInt16LE(buffer,4);
        Pair<Integer, Integer> imageSize = getImageSize(buffer, 0);

        if (nbImages == 1) {
            return imageSize;
        }

        for (int imageIndex = 1; imageIndex < nbImages; imageIndex += 1) {
            Pair<Integer, Integer> maxImageSize = getImageSize(buffer, imageIndex);
            if(maxImageSize.getLeft() > imageSize.getLeft()
                    && maxImageSize.getRight() > imageSize.getRight()){
                imageSize = maxImageSize;
            }
        }
        return imageSize;
    }

    private int getSizeFromOffset(ByteBuffer buffer, int offset) {
        int value = ByteUtil.readUInt8(buffer, offset);
        return value == 0 ? 256 : value;
    }

    private Pair<Integer, Integer> getImageSize(ByteBuffer buffer, int imageIndex) {
        int offset = SIZE_HEADER + (imageIndex * SIZE_IMAGE_ENTRY);
        return Pair.of(
                getSizeFromOffset(buffer, offset),
                getSizeFromOffset(buffer, offset + 1)
        );
    }
}