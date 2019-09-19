package net.freeapis.core;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;

/**
 * freeapis,Inc.
 * Copyright(C): 2016
 *
 * @author Administrator
 * @date 2019年09月18日 17:44
 */
class GIF implements Image.Parser{

    private static final String GIF_REGEX = "^GIF8[79]a";

    @Override
    public boolean isValid(ByteBuffer buffer) {
        String signature = ByteUtil.readString(buffer,"ascii",0,6);
        return signature.matches(GIF_REGEX);
    }

    @Override
    public Pair<Integer, Integer> size(ByteBuffer buffer) {
        return Pair.of(
                ByteUtil.readUInt16LE(buffer, 6),
                ByteUtil.readUInt16LE(buffer, 8)
        );
    }
}