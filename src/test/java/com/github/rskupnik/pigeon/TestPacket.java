package com.github.rskupnik.pigeon;

import com.github.rskupnik.pigeon.commons.Packet;
import com.github.rskupnik.pigeon.commons.annotations.PacketDataField;
import com.github.rskupnik.pigeon.commons.annotations.PigeonPacket;

@PigeonPacket(id = 1)
public class TestPacket extends Packet {

    @PacketDataField
    private int testData;

    public int getTestData() {
        return testData;
    }

    public void setTestData(int testData) {
        this.testData = testData;
    }
}
