package net.freeapis.core;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.ByteBuffer;

/**
 * freeapis,Inc.
 * Copyright(C): 2016
 *
 *
 * @author Administrator
 * @date 2019年09月18日 18:45
 */
class PSD implements Image.Parser{

    @Override
    public boolean isValid(ByteBuffer buffer) {
        return "8BPS".equals(ByteUtil.readString(buffer,"ascii",0,4));
    }

    @Override
    public Pair<Integer, Integer> size(ByteBuffer buffer) {
        return Pair.of(
                (int)ByteUtil.readUInt32BE(buffer, 18),
                (int)ByteUtil.readUInt32BE(buffer, 14)
        );
    }
}
