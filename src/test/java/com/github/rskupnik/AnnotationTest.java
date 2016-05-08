package com.github.rskupnik;

import com.github.rskupnik.annotations.AnnotationsScanner;
import com.github.rskupnik.annotations.IncomingPacket;
import com.github.rskupnik.annotations.PacketDataField;

public class AnnotationTest {

    public AnnotationTest() {
        AnnotationsScanner.getInstance().scan();
    }

    public static void main(String[] args) {
        new AnnotationTest();
    }

    @IncomingPacket(id=5)
    class Howdy {

        @PacketDataField
        private String message;
    }
}
