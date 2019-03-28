package Packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class WriteAcknowledgementPacket extends BasicPacket {
    private short packetSize;
    private long numOfPackets;

    WriteAcknowledgementPacket(InetAddress address, int port, short packetSize, long numOfPackets){
        super((short) 4, address, port);

        this.packetSize = packetSize;
        this.numOfPackets = numOfPackets;

        makePacket();
    }

    WriteAcknowledgementPacket(DatagramPacket packet){
        super((short) 4,packet.getAddress(),packet.getPort());
        this.packet = packet;

        decodePacket();
    }

    short getPacketSize() {
        return packetSize;
    }

    long getNumOfPackets() {
        return numOfPackets;
    }

    private void decodePacket() {
        ByteBuffer b = ByteBuffer.allocate(packet.getData().length);
        b.put(packet.getData());
        b.flip();

        opCode = retrieveShort(b);
        numOfPackets = b.getLong();
        b.get();
        packetSize = retrieveShort(b);
    }

    private void makePacket(){
        short spaceNeeded = 15;
        ByteBuffer b = ByteBuffer.allocate(spaceNeeded);

        //opCode
        insertShort(b,opCode);

        //Num of Packets
        b.putLong(numOfPackets);
        b.put((byte)0);

        //Packet Size
        insertShort(b,packetSize);

        createPacket(b,spaceNeeded);
    }

    private void insertShort(ByteBuffer b, short s){
        //opCode
        b.putShort(s);
        b.put((byte)0);
    }

    private short retrieveShort(ByteBuffer b){
        short temp = b.getShort();
        b.get();

        return temp;
    }
}
