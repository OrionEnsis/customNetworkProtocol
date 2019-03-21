package Packets;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class WriteAcknowledgementPacketTest {

    @Test
    public void acknowledgementTest1(){
        InetAddress address;
        short numOfPackets = 512;
        short packetSize = 1024;
        try {
            address = InetAddress.getLocalHost();
            WriteAcknowledgementPacket packet = new WriteAcknowledgementPacket(address,1234,packetSize,numOfPackets);
            WriteAcknowledgementPacket packet2 = new WriteAcknowledgementPacket(packet.getAsUDPPacket());

            Assert.assertEquals(packetSize,packet2.getPacketSize());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void acknowledgementTest2(){
        InetAddress address;
        short numOfPackets = 512;
        short packetSize = 1024;
        try {
            address = InetAddress.getLocalHost();
            WriteAcknowledgementPacket packet = new WriteAcknowledgementPacket(address,1234,packetSize,numOfPackets);
            WriteAcknowledgementPacket packet2 = new WriteAcknowledgementPacket(packet.getAsUDPPacket());

            Assert.assertEquals(numOfPackets,packet2.getNumOfPackets());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}