package Packets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

public class DataPacket extends BasicPacket implements Comparable<DataPacket>{
    private short packetNum;
    private byte[] data;

    DataPacket(InetAddress address, int port, short packetNum, byte[] data){
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
        data = new byte[packet.getData().length - 4];
        b.put(packet.getData());
        b.flip();

        opCode = b.getShort();
        packetNum = b.getShort();

        b.get(data);
    }

    public short getPacketNum() {
        return packetNum;
    }

    byte[] getData() {
        return data;
    }

    private void makePacket(){
        int spaceNeeded = 4 + data.length;
        ByteBuffer b = ByteBuffer.allocate(spaceNeeded);

        b.putShort(opCode);
        b.putShort(packetNum);
        b.put(data);

        createPacket(b,spaceNeeded);
    }

    public static List<DataPacket> getPacketsNeededForData(byte[] data, short packetSize, InetAddress address, int port){
        int headerSize = 4;
        int i = 0;
        ArrayList<DataPacket> packets = new ArrayList<>();
        short packetNum = 0;
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
        return Short.compare(this.packetNum,o.getPacketNum());
    }
}
