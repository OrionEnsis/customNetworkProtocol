package Packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class BasicPacket {
    protected short opCode;
    protected DatagramPacket packet;
    protected InetAddress address;
    protected int port;

    BasicPacket(short opcode, InetAddress address, int port){
        this.opCode = opcode;
        this.address = address;
        this.port = port;
        makePacket();
    }

    protected void makePacket(){
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


        return b.getShort();
    }

    DatagramPacket getAsUDPPacket(){
        return packet;
    }
}
