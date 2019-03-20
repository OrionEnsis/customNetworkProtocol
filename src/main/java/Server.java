import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class Server {
    private final int PORT = 2697;
    private InetAddress address;
    private DatagramSocket socket;

    private String filename;
    private short packetSize;
    private short numOfPackets;
    private HashSet<Short> receivedPackets;

    Server(String[] args){
        receivedPackets = new HashSet<>();
        try {
            socket = new DatagramSocket(PORT);
            //get ipvMode
            if (args[1].equals("ipv4")) {
                //IPV4
                address = Inet4Address.getLocalHost();
            } else {
                //IPV6
                address = Inet6Address.getLocalHost();
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    void run(){
        byte[] bytes;
        if(packetSize == 0)
            bytes = new byte[15333];
        else
            bytes = new byte[packetSize];

        DatagramPacket currentPacket = new DatagramPacket(bytes,bytes.length);
        short packetType;
        ByteBuffer b = null;
        //while true
        while(true) {
            try {
                //wait for packet
                socket.receive(currentPacket);

                //get packet type
                packetType = determinePacketType(currentPacket);

                //if packet is file packet
                if(packetType == 1) {
                    //set up for file transfer
                    setupWriteRequest(currentPacket);
                    bytes = new byte[packetSize];
                    currentPacket = new DatagramPacket(bytes,bytes.length);
                    b = ByteBuffer.allocate(packetSize*numOfPackets);
                }
                //else if it is a data packet
                else if( packetType == 3) {
                    //handle data
                    if(b != null)
                        handleDataPacket(currentPacket,b);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //if all packets received
            if(numOfPackets == receivedPackets.size()) {
                //turn into a file
                if(b != null)
                    makeFile(b);
                break;
            }
        }
    }

    private short determinePacketType(DatagramPacket packet){
        short temp;
        ByteBuffer b = ByteBuffer.allocate(packet.getLength());
        b.put(packet.getData());
        b.flip();

        temp = b.getShort();
        return temp;
    }
    private void setupWriteRequest(DatagramPacket packet) throws IOException {
        handleWriteRequest(packet);
        sendWriteAck(packet.getAddress(),packet.getPort());
    }
    private void handleWriteRequest(DatagramPacket packet){
        ByteBuffer b = ByteBuffer.allocate(packet.getLength());
        b.put(packet.getData());
        b.flip();
        //read packet
        //2bytes == 1 for write request
        b.getShort(); //we already know this

        //filename
        filename = getStringFromBuffer(b);
        //0 byte
        b.get();

        //mode -always octet
        String mode = getStringFromBuffer(b);
        //0 byte
        b.get();

        //packet num
        numOfPackets = b.getShort();
        //0
        b.get();

        //packet size
        packetSize = b.getShort();
        //0
        b.get();

    }

    private void sendWriteAck(InetAddress address, int port) throws IOException {
        byte[] bytes = new byte[9];//3 shorts and 3 0 bytes
        DatagramPacket packet = new DatagramPacket(bytes,bytes.length,address,port);
        ByteBuffer b = ByteBuffer.allocate(bytes.length);
        //2 byte = 6
        short opcode = 6;
        b.putShort(opcode);
        //0
        b.put((byte)0);
        //packet num
        b.putShort(numOfPackets);
        //0
        b.put((byte)0);
        //packet size
        b.putShort(packetSize);
        //0
        b.put((byte)0);

        //write to packet
        b.flip();
        packet.setData(b.array());

        //send packet
        socket.send(packet);
    }

    private String getStringFromBuffer(ByteBuffer b){
        int startPosition = b.position();
        byte nextByte = b.get();

        while (nextByte != 0){
            nextByte = b.get();
        }
        int endPosition = b.position();
        b.position(startPosition);

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = b.position(); i < endPosition; i = b.position()) {
            stringBuilder.append(b.getChar());
        }
        return stringBuilder.toString();

    }
    private void handleDataPacket(DatagramPacket packet, ByteBuffer b) throws IOException {
        ByteBuffer packetBuffer = ByteBuffer.allocate(packet.getLength());
        packetBuffer.put(packet.getData());
        packetBuffer.flip();

        short opCode = packetBuffer.getShort();
        short packetNum = packetBuffer.getShort();

        b.position(packetNum *packetSize);
        b.put(packetBuffer.array(),packetBuffer.arrayOffset(),packetBuffer.remaining());

        sendDataAck(packet.getAddress(),packet.getPort(),packetNum);

    }
    private void sendDataAck(InetAddress address, int port, short packetNum) throws IOException {
        byte[] bytes = new byte[4];//2 shorts
        DatagramPacket packet = new DatagramPacket(bytes,bytes.length,address,port);
        ByteBuffer b = ByteBuffer.allocate(bytes.length);
        //2 byte 4
        short opCode = 4;
        b.putShort(opCode);
        //2 byte package number
        b.putShort(packetNum);

        //send packet
        b.flip();
        packet.setData(b.array());
        socket.send(packet);
        receivedPackets.add(packetNum);
    }

    private void makeFile(ByteBuffer b) {
        try {
            //get save directory
            String directory = File.separator  + "sentFiles" + File.separator;
            //convert the file
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(b.array());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

            ZipFile zipFile = (ZipFile) objectInputStream.readObject();
            //unzip file
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                InputStream stream = zipFile.getInputStream(entry);

                File f = new File(directory + entry.getName());

                //buffer streams
                FileOutputStream fileWriter = new FileOutputStream(f);
                byte[] bytes = new byte[stream.available()];
                stream.read(bytes,0,bytes.length);


                //write file
                fileWriter.write(bytes);
                fileWriter.flush();
                fileWriter.close();
                stream.close();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
