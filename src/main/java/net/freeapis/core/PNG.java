package net.freeapis.core;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;

/**
 * freeapis,Inc.
 * Copyright(C): 2016
 *
 * @author Administrator
 * @date 2019年09月18日 14:50
 */
class PNG implements Image.Parser {
    private boolean pngFried = false;
    /**
     * PNG\r\n\x1a\n
     */
    private static final String PNG_SIGNATURE = new String(new byte[]{0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
    private static final String PNG_IMAGE_HEADER_CHUNK_NAME = "IHDR";

    /**
     * 兼容iphone平台下的PNGS
     * Used to detect "fried" png's: http://www.jongware.com/pngdefry.html
     */
    private static final String PNG_FRIED_CHUNK_NAME = "CgBI";

    @Override
    public boolean isValid(ByteBuffer buffer) {
        String pngSignature = ByteUtil.readString(buffer, "ascii", 1, 8);
        if (PNG_SIGNATURE.equals(pngSignature)) {
            String chunkName = ByteUtil.readString(buffer, "ascii", 12, 16);
            if (PNG_FRIED_CHUNK_NAME.equals(chunkName)) {
                chunkName = ByteUtil.readString(buffer, "ascii", 28, 32);
                pngFried = true;
            }
            if (!PNG_IMAGE_HEADER_CHUNK_NAME.equals(chunkName)) {
                throw new IllegalStateException("Invalid PNG");
            }
            return true;
        }
        return false;
    }

    @Override
    public Pair<Integer, Integer> size(ByteBuffer buffer) {
        int widthFragmentPos = 16;
        int heightFragmentPos = 20;
        if (pngFried) {
            widthFragmentPos = 32;
            heightFragmentPos = 36;
        }
        long width = ByteUtil.readUInt32BE(buffer, widthFragmentPos);
        long height = ByteUtil.readUInt32BE(buffer, heightFragmentPos);
        return Pair.of((int)width, (int)height);
    }
}