package Packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class BasicPacket {
    protected short opCode;
    protected DatagramPacket packet;
    protected InetAddress address;
    protected int port;

    public BasicPacket(DatagramPacket packet){
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

    short getOpCodeFromPacket(){
        ByteBuffer b = ByteBuffer.allocate(packet.getLength());
        b.put(packet.getData());
        b.flip();

        return b.getShort();
    }

    public DatagramPacket getAsUDPPacket(){
        return packet;
    }

    void createPacket(ByteBuffer b, int size){
        byte[] bytes = new byte[size];
        b.flip();
        b.get(bytes);
        packet = new DatagramPacket(bytes,bytes.length,address,port);
    }
}
