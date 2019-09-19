package net.freeapis.core;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;

/**
 * freeapis,Inc.
 * Copyright(C): 2016
 * @author Administrator
 * @date 2019年09月18日 17:48
 */
class BMP implements Image.Parser {

    @Override
    public boolean isValid(ByteBuffer buffer) {
        return "BM".equals(ByteUtil.readString(buffer,"ascii",0,2));
    }

    @Override
    public Pair<Integer, Integer> size(ByteBuffer buffer) {
        return Pair.of(
                (int)ByteUtil.readUInt32LE(buffer, 6),
                Math.abs(ByteUtil.readInt32LE(buffer, 8))
        );
    }
}