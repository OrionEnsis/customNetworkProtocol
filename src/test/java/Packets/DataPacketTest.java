package Packets;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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

    @Test
    public void testFileAssembly1() {
        try {
            URL res = getClass().getClassLoader().getResource("Test.txt");
            File f = Paths.get(res.toURI()).toFile();
            byte[] fData = Files.readAllBytes(f.toPath());
            short packetSize = 2048;
            InetAddress address = InetAddress.getLocalHost();

            List<DataPacket> packets = DataPacket.getPacketsNeededForData(Files.readAllBytes(f.toPath()),packetSize,address,1234);
            Assert.assertEquals(1,packets.size());
            byte[] data = DataPacket.getDataFromCollection(packets);
            Assert.assertArrayEquals(fData, data);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFileAssembly2() {
        try {
            URL res = getClass().getClassLoader().getResource("Test2.txt");
            File f = Paths.get(res.toURI()).toFile();
            byte[] fData = Files.readAllBytes(f.toPath());
            short packetSize = 128;
            InetAddress address = InetAddress.getLocalHost();

            List<DataPacket> packets = DataPacket.getPacketsNeededForData(Files.readAllBytes(f.toPath()),packetSize,address,1234);
            Assert.assertTrue(packets.size() > 1);
            byte[] data = DataPacket.getDataFromCollection(packets);
            Assert.assertArrayEquals(fData, data);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}