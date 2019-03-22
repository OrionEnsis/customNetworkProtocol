package Packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class DataAcknowledgementPacket extends BasicPacket{
    private short packetNum;

    DataAcknowledgementPacket(InetAddress address, int port, short packetNum){
        super((short)4,address,port);
        this.packetNum = packetNum;

        makePacket();
    }

    public DataAcknowledgementPacket(DatagramPacket packet){
        super((short) 4,packet.getAddress(),packet.getPort());

        this.packet = packet;
        decodePacket();
    }

    public short getPacketNum() {
        return packetNum;
    }

    private void decodePacket() {
        ByteBuffer b = ByteBuffer.allocate(packet.getData().length);
        b.put(packet.getData());
        b.flip();

        opCode = b.getShort();
        packetNum = b.getShort();
    }

    private void makePacket(){
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putShort(opCode);
        b.putShort(packetNum);

        createPacket(b,4);
    }

}
