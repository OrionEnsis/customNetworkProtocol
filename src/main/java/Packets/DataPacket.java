package Packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

public class DataPacket extends BasicPacket implements Comparable<DataPacket>{
    private long packetNum;
    private byte[] data;

    DataPacket(InetAddress address, int port, long packetNum, byte[] data){
        super((short)3,address,port);
        this.packetNum = packetNum;
        this.data = data;

        makePacket();
    }

    public DataPacket(DatagramPacket packet){
        super((short)3,packet.getAddress(),packet.getPort());
        this.packet = packet;

        decodePacket();
    }

    private void decodePacket() {
        ByteBuffer b = ByteBuffer.allocate(packet.getData().length);
        data = new byte[packet.getData().length - 10];
        b.put(packet.getData());
        b.flip();

        opCode = b.getShort();
        packetNum = b.getLong();

        b.get(data);
    }

    public long getPacketNum() {
        return packetNum;
    }

    byte[] getData() {
        return data;
    }

    private void makePacket(){
        int spaceNeeded = 10 + data.length;
        ByteBuffer b = ByteBuffer.allocate(spaceNeeded);

        b.putShort(opCode);
        b.putLong(packetNum);
        b.put(data);

        createPacket(b,spaceNeeded);
    }

    public static List<DataPacket> getPacketsNeededForData(byte[] data, short packetSize, InetAddress address, int port){
        int headerSize = 10;
        int i = 0;
        ArrayList<DataPacket> packets = new ArrayList<>();
        long packetNum = 0;
        while(i < data.length){
            int endRange = packetSize - headerSize +i;
            byte[] packetData;
            if(endRange<data.length){
                packetData = Arrays.copyOfRange(data,i,endRange);
            }
            else{
                packetData = Arrays.copyOfRange(data,i,data.length);
            }

            i = endRange;

            packets.add(new DataPacket(address,port,packetNum,packetData));
            packetNum++;
        }

        return packets;
    }

    public DataAcknowledgementPacket makeAcknowledgement(){
        return new DataAcknowledgementPacket(address,port,packetNum);
    }

    public static byte[] getDataFromCollection(Collection<DataPacket> packets) throws IOException {
        List<DataPacket> list = new ArrayList<>(packets);
        Collections.sort(list);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (DataPacket d :
                list) {
            byteArrayOutputStream.write(d.data);
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public int compareTo(DataPacket o) {
        return Long.compare(this.packetNum,o.getPacketNum());
    }
}
