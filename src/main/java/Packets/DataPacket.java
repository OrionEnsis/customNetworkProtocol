package Packets;

import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataPacket extends BasicPacket {
    private short packetNum;
    private byte[] data;

    public DataPacket(InetAddress address, int port, short packetNum, byte[] data){
        super((short)4,address,port);
        this.packetNum = packetNum;
        this.data = data;

        makePacket();
    }

    public DataPacket(DatagramPacket packet){
        super((short)4,packet.getAddress(),packet.getPort());
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

            i+= endRange;

            packets.add(new DataPacket(address,port,packetNum,packetData));
            packetNum++;
        }

        return packets;
    }
}
