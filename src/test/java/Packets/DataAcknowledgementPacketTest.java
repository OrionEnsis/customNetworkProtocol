package Packets;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class DataAcknowledgementPacketTest {

    @Test
    public void testAcknowledgement1(){
        InetAddress address;
        short packetNum = 37;
        try {
            address = InetAddress.getLocalHost();
            DataAcknowledgementPacket packet = new DataAcknowledgementPacket(address,1234,packetNum);
            DataAcknowledgementPacket packet2 = new DataAcknowledgementPacket(packet.getAsUDPPacket());

            Assert.assertEquals(packetNum,packet2.getPacketNum());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}