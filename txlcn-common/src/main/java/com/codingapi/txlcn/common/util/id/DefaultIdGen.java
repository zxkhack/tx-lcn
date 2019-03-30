package com.codingapi.txlcn.common.util.id;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Description:
 * Date: 2/1/19
 *
 * @author ujued
 */
public class DefaultIdGen implements IdGen {

    /**
     * 2019-2-1
     */
    private static final long START_TIME = 1548992829394L;

    private long machineId;

    private int machineOffset;

    private int timeOffset;

    private long seq;

    private int seqLen;

    private long lastTime;

    private ByteBuffer byteBuffer = ByteBuffer.allocate(12);

    public DefaultIdGen(int seqLen, long machineId) {
        this.seqLen = seqLen;
        this.machineOffset = seqLen;
        this.machineId = machineId;
        this.timeOffset = 46;
    }

    @Override
    public synchronized String nextId() {
        long curTime = System.currentTimeMillis();
        if (curTime < lastTime) {
            throw new IllegalStateException("");
        }

        if (curTime == lastTime) {
            seq = (seq + 1) & (~(-1 << seqLen));
            if (seq == 0L) {
                curTime = tilNextMillis();
            }
        } else {
            seq = 0L;
        }

        lastTime = curTime;
        long time = curTime - START_TIME;

        long seqWithMachine = (time << timeOffset) | (machineId << machineOffset) | seq;

        return new BigInteger(bytes(seqWithMachine, (int) ((time >> 18) & 16777215))).toString(16);
    }

    private synchronized byte[] bytes(long l, int o) {
        byteBuffer.clear();
        for (int i = 0; i < 8; i++) {
            byte b = (byte) (l >> (i * 8) & 255);
            byteBuffer.put(b);
        }
        byteBuffer.put((byte) (o & 255));
        byteBuffer.put((byte) ((o >> 8) & 255));
        byteBuffer.put((byte) (o >> 16 & 255));
        byteBuffer.put((byte) (o >> 24 & 255));
        byteBuffer.flip();
        byte[] bs = new byte[11];
        byteBuffer.get(bs);
        for (int i = 0; i < bs.length / 2; i++) {
            byte temp = bs[i];
            bs[i] = bs[bs.length - 1 - i];
            bs[bs.length - 1 - i] = temp;
        }
        return bs;
    }

    private long tilNextMillis() {
        long newTime = System.currentTimeMillis();
        while (newTime <= lastTime) {
            newTime = System.currentTimeMillis();
        }
        return newTime;
    }
}
