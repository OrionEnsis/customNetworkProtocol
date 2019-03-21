package Packets;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class WriteRequestPacketTest {

    @Test
    public void packetCreation1() {
        InetAddress address;
        String filename = "testfile.test";
        short numOfPackets = 512;
        short packetSize = 1024;
        try {
            address = InetAddress.getLocalHost();
            WriteRequestPacket packet = new WriteRequestPacket(address,1234,filename,packetSize,numOfPackets);
            WriteRequestPacket packet2 = new WriteRequestPacket(packet.getAsUDPPacket());

            Assert.assertEquals(filename,packet2.getFileName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void packetCreation2() {
        InetAddress address;
        String filename = "testfile.test";
        short numOfPackets = 512;
        short packetSize = 1024;
        try {
            address = InetAddress.getLocalHost();
            WriteRequestPacket packet = new WriteRequestPacket(address,1234,filename,packetSize,numOfPackets);
            WriteRequestPacket packet2 = new WriteRequestPacket(packet.getAsUDPPacket());

            Assert.assertEquals(numOfPackets,packet2.getNumOfPackets());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void packetCreation3() {
        InetAddress address;
        String filename = "testfile.test";
        short numOfPackets = 512;
        short packetSize = 1024;
        try {
            address = InetAddress.getLocalHost();
            WriteRequestPacket packet = new WriteRequestPacket(address,1234,filename,packetSize,numOfPackets);
            WriteRequestPacket packet2 = new WriteRequestPacket(packet.getAsUDPPacket());

            Assert.assertEquals(packetSize,packet2.getPacketSize());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getStringFromBufferTest1() {
        String testWord = "Phillip";
        ByteBuffer b = ByteBuffer.allocate(testWord.length()+1);
        b.put(testWord.getBytes());
        b.put((byte) 0);
        b.flip();

        String result = WriteRequestPacket.getStringFromBuffer(b);
        assertEquals(result, testWord);
    }
}