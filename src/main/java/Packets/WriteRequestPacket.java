package Packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class WriteRequestPacket extends BasicPacket{
    private String filename;
    private String mode;
    private short packetSize;
    private long numOfPackets;

    public WriteRequestPacket(InetAddress address, int port, String filename, short packetSize, long numOfPackets){
        super((short)1,address,port);
        this.filename = filename;
        this.mode = "octet";
        this.packetSize = packetSize;
        this.numOfPackets = numOfPackets;

        makePacket();
    }

    public WriteRequestPacket(DatagramPacket packet){
        super((short) 1,packet.getAddress(),packet.getPort());
        this.packet = packet;

        decodePacket();
    }

    public WriteAcknowledgementPacket createAcknowledgementForThisPacket(){
        return new WriteAcknowledgementPacket(packet.getAddress(),packet.getPort(),packetSize,numOfPackets);
    }

    public String getFileName(){
        return filename;
    }

    public String getMode() {
        return mode;
    }

    public long getNumOfPackets() {
        return numOfPackets;
    }

    public short getPacketSize() {
        return packetSize;
    }

    protected void makePacket(){
        byte[] fileNameAsBytes = filename.getBytes(); //each character is 1 byte
        byte[] modeAsBytes = mode.getBytes();
        int spacingBytes = 10; //extra bytes for formatting
        int totalBytesNeeded = fileNameAsBytes.length + modeAsBytes.length + spacingBytes;
        ByteBuffer b = ByteBuffer.allocate(totalBytesNeeded);

        //opcode
        b.putShort(opCode);

        //filenameStuff
        b.put(fileNameAsBytes);
        b.put((byte)0);

        //mode
        b.put(modeAsBytes);
        b.put((byte)0);

        //number of packets
        b.putLong(numOfPackets);
        b.put((byte)0);

        //packet size
        b.putShort(packetSize);
        b.put((byte)0);

        //make the new packet
        createPacket(b,totalBytesNeeded);
    }

    private void decodePacket() {
        ByteBuffer b = ByteBuffer.allocate(packet.getData().length);
        b.put(packet.getData());
        b.flip();

        //get opCode
        opCode = b.getShort();

        //get filename
        filename = getStringFromBuffer(b);
        b.get();

        //get mode
        mode = getStringFromBuffer(b);
        b.get();

        //get number of packets
        numOfPackets = b.getShort();
        b.get();

        //get packet size
        packetSize = b.getShort();
        b.get();

    }

    static String getStringFromBuffer(ByteBuffer b) {
        int startPosition = b.position();
        byte nextByte = b.get();

        while (nextByte != 0){
            nextByte = b.get();
        }
        int endPosition = b.position()-1;
        b.position(startPosition);

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = b.position(); i < endPosition; i = b.position()) {
            char c = (char)b.get();

            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
}
