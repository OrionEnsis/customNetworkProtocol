package Packets;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class BasicPacketTest {

    @Test
    public void opCodeRetrievalTest1(){
        short opCode = 5;
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
            BasicPacket packet = new BasicPacket(opCode, address,1234);
            assertEquals(opCode, packet.getOpCodeFromPacket());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void opCodeRetrievalTest2(){
        short opCode = 5;
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
            BasicPacket packet = new BasicPacket(opCode, address,1234);
            BasicPacket packet2 = new BasicPacket(packet.getAsUDPPacket());
            assertEquals(opCode, packet2.getOpCodeFromPacket());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}