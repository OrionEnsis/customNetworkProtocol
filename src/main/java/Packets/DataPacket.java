package Packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

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

    public byte[] getData() {
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

}
