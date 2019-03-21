package Packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class BasicPacket {
    protected short opCode;
    protected DatagramPacket packet;
    protected InetAddress address;
    protected int port;

    BasicPacket(DatagramPacket packet){
        address = packet.getAddress();
        port = packet.getPort();
        this.packet = packet;
        opCode = getOpCodeFromPacket();
    }
    BasicPacket(short opcode, InetAddress address, int port){
        this.opCode = opcode;
        this.address = address;
        this.port = port;
        makePacket();
    }

    private void makePacket(){
        ByteBuffer b = ByteBuffer.allocate(2);
        b.putShort(opCode);
        b.flip();

        packet = new DatagramPacket(b.array(),b.array().length,address,port);
    }

    public short getOpCode(){
        return opCode;
    }

    public short getOpCodeFromPacket(){
        ByteBuffer b = ByteBuffer.allocate(packet.getLength());
        b.put(packet.getData());
        b.flip();

        return b.getShort();
    }

    DatagramPacket getAsUDPPacket(){
        return packet;
    }
}
