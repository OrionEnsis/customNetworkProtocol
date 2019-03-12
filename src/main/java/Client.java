import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.zip.ZipFile;

class Client {
    private final int PORT = 2697;
    private final boolean HAS_SLIDING_WINDOW;
    private final boolean SIM_DROP_PACKETS;
    private final short PACKET_SIZE = 4096;
    private final String sendAddress = "pi.cs.oswego.edu";

    private DatagramSocket socket;
    private InetAddress address;

    private ArrayBlockingQueue<Short> ackQueue; //set of packets awaiting ack
    private ConcurrentHashMap<Short,DatagramPacket> allPackets;
    private ArrayBlockingQueue<DatagramPacket> packetQueue;
    Client(String[] args){

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

        byte bytes[] = new byte[totalBytesNeeded];
        b.flip();
        b.get(bytes);
        DatagramPacket packet = new DatagramPacket(bytes,0, totalBytesNeeded,address,PORT);
        return packet;
    }

    private void makeDataPackets(ZipFile data){
        //while we still have data to process
            //grab max size for packet -4 bytes
            //mark as data packet
            //mark packet dataNum;
            //save packet dataNum for later
        //Queue up all packets
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
