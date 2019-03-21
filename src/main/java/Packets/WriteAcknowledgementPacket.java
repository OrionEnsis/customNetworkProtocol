package Packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class WriteAcknowledgementPacket extends BasicPacket {
    private short packetSize;
    private short numOfPackets;

    public WriteAcknowledgementPacket(InetAddress address, int port, short packetSize, short numOfPackets){
        super((short) 4, address, port);

        this.packetSize = packetSize;
        this.numOfPackets = numOfPackets;

        makePacket();
    }

    public WriteAcknowledgementPacket(DatagramPacket packet){
        super((short) 4,packet.getAddress(),packet.getPort());
        this.packet = packet;

        decodePacket();
    }

    public short getPacketSize() {
        return packetSize;
    }

    public short getNumOfPackets() {
        return numOfPackets;
    }

    private void decodePacket() {
        ByteBuffer b = ByteBuffer.allocate(packet.getData().length);
        b.put(packet.getData());
        b.flip();

        opCode = retrieveShort(b);
        numOfPackets = retrieveShort(b);
        packetSize = retrieveShort(b);
    }

    private void makePacket(){
        short spaceNeeded = 9; //3 0s and 3 shorts
        ByteBuffer b = ByteBuffer.allocate(spaceNeeded);

        //opCode
        insertShort(b,opCode);

        //Num of Packets
        insertShort(b,numOfPackets);

        //Packet Size
        insertShort(b,packetSize);

        byte[] bytes = new byte[spaceNeeded];
        b.flip();
        b.get(bytes);

        packet = new DatagramPacket(bytes,bytes.length,address,port);
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
