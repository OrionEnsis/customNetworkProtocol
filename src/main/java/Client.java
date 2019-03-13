
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.ZipFile;

class Client {
    private final int PORT = 2697;
    private final boolean HAS_SLIDING_WINDOW;
    private final boolean SIM_DROP_PACKETS;
    private final short PACKET_SIZE = 4096;
    private final short DATA_CODE = 3;
    private final String sendAddress = "pi.cs.oswego.edu";
    private final int TIMEOUT = 5000;
    private final int BLOCK_SIZE = 100;

    private DatagramSocket socket;
    private InetAddress address;

    private HashSet<Short> blockPackets;                    //set of packets awaiting ack
    private HashMap<Short, DatagramPacket> allPackets;  //hash of all packets
    private Queue<Short> packetQueue;                   //queue of all remaining packets

    Client(String[] args){
        blockPackets = new HashSet<>();
        allPackets = new HashMap<>();
        packetQueue = new ArrayDeque<>();

        //get ipvMode
        try {
            socket = new DatagramSocket(PORT);
            socket.setSoTimeout(TIMEOUT);

            //get ipvMode
            if (args[1].equals("ipv4")) {
                //IPV4
                address = Inet4Address.getByName(sendAddress);
            } else {
                //IPV6
                address = Inet6Address.getByName(sendAddress);
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        //get window or stack
        HAS_SLIDING_WINDOW = args[2].equalsIgnoreCase("windowed");

        //get drop packets.
        SIM_DROP_PACKETS = args[3].equalsIgnoreCase("SimON");
    }

    void run(){
        Scanner scanner = new Scanner(System.in);
        String filename;
        File f;
        ZipFile z = null;
        DatagramPacket start;

        //prompt for directory
        System.out.println("Enter the file Directory");
        filename = scanner.nextLine();
        f = new File(filename);
        filename = f.getName();
        //zip it up
        try {
            z = new ZipFile(f);

            //make packets
            makeDataPackets(z);

            //start sending.
            start = makeWriteRequestPacket(filename);
            send(start);

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if (z != null)
                    z.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private DatagramPacket makeWriteRequestPacket(String filename){
        byte[] fileNameAsBytes = filename.getBytes();
        byte[] octet = "octet".getBytes();
        int totalBytesNeeded = fileNameAsBytes.length + octet.length + 10; //10 is number of extra bytes for spacing
        ByteBuffer b = ByteBuffer.allocate(totalBytesNeeded);

        //2bytes == 1 for write request
        b.putShort((short)1);

        //filename
        b.put(fileNameAsBytes);
        b.put((byte)0);

        //mode -always octet
        b.put(octet);
        b.put((byte)0);

        //packet num
        b.putShort((short)allPackets.size());
        b.put((byte)0);

        //packet write size
        b.putShort(PACKET_SIZE);
        b.put((byte)0);

        byte[] bytes = new byte[totalBytesNeeded];
        b.flip();
        b.get(bytes);
        return makePacket(bytes);
    }

    private void makeDataPackets(ZipFile data){
        final int HEADER_SIZE = 4;
        //convert data to binary
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream zipOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            zipOutputStream.writeObject(data);

            byte[] bytes = byteArrayOutputStream.toByteArray();

            int i = 0;
            short packetNum = 0;
            byte[] byteData;

            //while we still have data to process
            while(i < bytes.length){
                //grab max size for packet -4
                int endRange = PACKET_SIZE - HEADER_SIZE;
                if(endRange < bytes.length)
                    byteData = Arrays.copyOfRange(bytes,i,endRange);
                else{
                    byteData = Arrays.copyOfRange(bytes,i,bytes.length);
                }
                i += endRange;

                //save packet dataNum for later
                allPackets.put(packetNum,markDatagramPacket(packetNum,byteData));
                packetQueue.add(packetNum);
                packetNum++;

            }

        //Queue up all packets
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private DatagramPacket markDatagramPacket(short num, byte[] data){
        ByteBuffer b = ByteBuffer.allocate(4 + data.length);
        //mark as data packet
        b.putShort(DATA_CODE);

        //mark packet dataNum;
        b.putShort(num);

        //add data
        b.put(data);

        byte[] bytes = new byte[4 + data.length];
        b.flip();
        b.get(bytes);

        return makePacket(data);
    }

    private DatagramPacket makePacket(byte[] data){
        return new DatagramPacket(data,0,data.length,address,PORT);
    }

    private void send(DatagramPacket startPacket){
        byte[] data = new byte[startPacket.getLength()];
        boolean notReceivedStartAck = true;
        DatagramPacket receiveAck = new DatagramPacket(data, data.length);
        int maxPackets;
        if(HAS_SLIDING_WINDOW){
            maxPackets = BLOCK_SIZE;
        }
        else{
            maxPackets = 1;
        }
        while(notReceivedStartAck) {
            try {
                //send write request
                socket.send(startPacket);
                //get ack

                socket.receive(receiveAck);
                notReceivedStartAck = false;
            } catch (IOException e) {
                System.out.println("packet failed for some reason, resending");
            }
        }

        //while there are packets to send and we are still awaiting acks
        while(!blockPackets.isEmpty() && !packetQueue.isEmpty()) {
            short currentPacket;
            //send "block" of packets
            try {
                blockPackets = new HashSet<>();
                for (int i = 0; i < maxPackets && !packetQueue.isEmpty(); i++) {
                    currentPacket = packetQueue.poll();
                    blockPackets.add(currentPacket);
                    socket.send(allPackets.get(currentPacket));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                //receive up to block of packets
                while (blockPackets.size() != 0) {
                    data = new byte[7];//number of bytes in ackpacket
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    socket.receive(packet);
                    short packetNum = decodeReceivePacket(packet);
                    blockPackets.remove(packetNum);
                }
            } catch (IOException e) {
                //insert the remainders
                packetQueue.addAll(blockPackets);
            }
        }
    }

    private short decodeReceivePacket(DatagramPacket packet){
        short temp = -1;
        ByteBuffer b = ByteBuffer.allocate(packet.getLength());
        b.put(packet.getData());
        b.flip();

        short opCode = b.getShort();

        if(opCode == 6){
            b.get();
            temp = b.getShort();
        }
        return temp;
    }
}
