package Packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class DataAcknowledgementPacket extends BasicPacket{
    private long packetNum;

    DataAcknowledgementPacket(InetAddress address, int port, long packetNum){
        super((short)4,address,port);
        this.packetNum = packetNum;

        makePacket();
    }

    public DataAcknowledgementPacket(DatagramPacket packet){
        super((short) 4,packet.getAddress(),packet.getPort());

        this.packet = packet;
        decodePacket();
    }

    public long getPacketNum() {
        return packetNum;
    }

    private void decodePacket() {
        ByteBuffer b = ByteBuffer.allocate(packet.getData().length);
        b.put(packet.getData());
        b.flip();

        opCode = b.getShort();
        packetNum = b.getLong();
    }

    private void makePacket(){
        ByteBuffer b = ByteBuffer.allocate(10);
        b.putShort(opCode);
        b.putLong(packetNum);

        createPacket(b,10);
    }

}
