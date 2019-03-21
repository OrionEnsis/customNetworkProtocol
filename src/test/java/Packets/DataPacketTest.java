package Packets;

import org.junit.Assert;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class DataPacketTest {


    @Test
    public void testAcknowledgement1(){
        InetAddress address;
        short packetNum = 37;
        String test = "test";
        try {
            address = InetAddress.getLocalHost();
            DataPacket packet = new DataPacket(address,1234,packetNum,test.getBytes());
            DataPacket packet2 = new DataPacket(packet.getAsUDPPacket());

            Assert.assertEquals(packetNum,packet2.getPacketNum());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAcknowledgement2(){
        InetAddress address;
        short packetNum = 37;
        String test = "test";
        try {
            address = InetAddress.getLocalHost();
            DataPacket packet = new DataPacket(address,1234,packetNum,test.getBytes());
            DataPacket packet2 = new DataPacket(packet.getAsUDPPacket());

            String result = new String(packet2.getData());
            Assert.assertEquals(test,result);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}