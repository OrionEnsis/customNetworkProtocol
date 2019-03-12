
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.zip.ZipFile;

class Client {
    private final int PORT = 2697;
    private final boolean HAS_SLIDING_WINDOW;
    private final boolean SIM_DROP_PACKETS;
    private final short PACKET_SIZE = 4096;
    private final short DATA_CODE = 3;
    private final String sendAddress = "pi.cs.oswego.edu";

    private DatagramSocket socket;
    private InetAddress address;

    private ArrayBlockingQueue<Short> ackQueue; //set of packets awaiting ack
    private ConcurrentHashMap<Short, DatagramPacket> allPackets;
    private ArrayBlockingQueue<DatagramPacket> packetQueue;
    Client(String[] args){
        ackQueue = new ArrayBlockingQueue<>(10000);
        allPackets = new ConcurrentHashMap<>();
        packetQueue = new ArrayBlockingQueue<>(10000);

        //get ipvMode
        try {
            socket = new DatagramSocket(PORT);
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

            //TODO retool these so packets are made independent of writerequest
            //make packets
            start = makeWriteRequestPacket(filename,z);

            //start sending.
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

    private DatagramPacket makeWriteRequestPacket(String filename,ZipFile zipFile){
        byte[] fileNameAsBytes = filename.getBytes();
        byte[] octet = "octet".getBytes();
        int totalBytesNeeded = fileNameAsBytes.length + octet.length + 10; //10 is number of extra bytes for spacing
        ByteBuffer b = ByteBuffer.allocate(totalBytesNeeded);
        makeDataPackets(zipFile);

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
    private void handleOverQueuing(){
        //while not interrupted
            //check for members in queue
            //if number exceeds max number
                //requeue until below threshold

    }

    private void receiveAcks(){
        //while not interrupted
            //wait for reply
            //read ack
            //add ack to set
    }

    private void send(DatagramPacket startPacket){
        //send write request
        //get ack
        //start ack thread
        //while there are packets to send and we are still awaiting acks
            //if in single mode and still ack is still waiting.
                //continue
            //if send packet exists and is not received
                //if not sim drop
                    //send packet
                //add to ack queue
    }
}
