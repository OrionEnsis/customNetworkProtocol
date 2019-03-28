
import Packets.DataAcknowledgementPacket;
import Packets.DataPacket;
import Packets.WriteRequestPacket;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;


class Client {
    private final int RNG = 50;
    private final int PORT = 2697;
    private final boolean HAS_SLIDING_WINDOW;
    private final boolean SIM_DROP_PACKETS;
    private final short PACKET_SIZE = 512;
    private final String SEND_ADDRESS = "pi.cs.oswego.edu";
    private final int TIMEOUT = 500;
    private final int BLOCK_SIZE = 32;

    private DatagramSocket socket;
    private InetAddress address;

    private HashSet<Long> blockPackets;                //set of packets awaiting ack
    private HashMap<Long, DataPacket> allPackets;      //hash of all packets
    private Queue<Long> packetQueue;                   //queue of all remaining packets

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
                address = Inet4Address.getByName(SEND_ADDRESS);
            } else {
                //IPV6
                address = Inet6Address.getByName(SEND_ADDRESS);
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        //get window or stack
        HAS_SLIDING_WINDOW = args[2].equalsIgnoreCase("window");

        //get drop packets.
        SIM_DROP_PACKETS = args[3].equalsIgnoreCase("SimON");
    }

    void run(){
        Scanner scanner = new Scanner(System.in);
        String filename;
        File f;
        //ZipFile z = null;
        WriteRequestPacket start;

        //prompt for directory
        System.out.println("Enter the file Directory");
        //filename = scanner.nextLine();
        //filename = "/home/jspagnol/chatlog.txt";
        filename = "/home/jspagnol/Desktop/Network TransferTestFiles";
        f = new File(filename);
        filename = f.getName();
        //zip it up  //TODO figure this shit out




        //start sending.
        if(f.isDirectory()){
            getAllFiles(f,filename);
        }else{
            //make packets
            makeDataPackets(f);
            System.out.println("sending " + filename);
            start = new WriteRequestPacket(address,PORT,filename,PACKET_SIZE,(long)allPackets.size());
            send(start);
        }
    }

    private void getAllFiles(File file,String dir) {
        if(file.isDirectory())
            for (File f:
                 file.listFiles()) {

                if(f.isDirectory()){
                    getAllFiles(f, dir + File.separator + f.getName());
                }
                else{
                    System.out.println("sending " + f.getName());
                    makeDataPackets(f);
                    send(new WriteRequestPacket(address,PORT,dir + File.separator + f.getName(),PACKET_SIZE,(long)allPackets.size()));
                }
            }
    }


    private void makeDataPackets(File data){
        allPackets = new HashMap<>();
        //convert data to binary
        try {

            byte[] bytes = Files.readAllBytes(data.toPath());

            List<DataPacket> packets = DataPacket.getPacketsNeededForData(bytes,PACKET_SIZE,address,PORT);
            for (DataPacket packet:
                 packets) {
                allPackets.put(packet.getPacketNum(),packet);
                packetQueue.add(packet.getPacketNum());
            }

        //Queue up all packets
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send(WriteRequestPacket startPacket){
        Random r = new Random();
        byte[] data = new byte[startPacket.getAsUDPPacket().getData().length];
        boolean notReceivedStartAck = true;
        DatagramPacket receiveAck = new DatagramPacket(data, data.length);
        int maxPackets;

        //setup for sliding window
        if(HAS_SLIDING_WINDOW){
            maxPackets = BLOCK_SIZE;
        }
        else{
            maxPackets = 1;
        }

        //while we are a waiting for the OK to send the file.
        while(notReceivedStartAck) {
            try {
                System.out.println("Sending first ack Packet");
                //send write request
                socket.send(startPacket.getAsUDPPacket());

                //get ack
                //TODO this should examine for a correct acknowledgement
                socket.receive(receiveAck);
                notReceivedStartAck = false;
                System.out.println("acknowledgement received.  Starting data Transfer");
            } catch (IOException e) {
                System.out.println("packet failed for some reason, resending");
            }
        }

        //while there are packets to send and we are still awaiting acks
        while(!blockPackets.isEmpty() || !packetQueue.isEmpty()) {
            long currentPacket;
            //send "block" of packets
            try {
                blockPackets = new HashSet<>();

                while(!packetQueue.isEmpty() && blockPackets.size() < maxPackets) {
                    currentPacket = packetQueue.poll();
                    blockPackets.add(currentPacket);
                    if(!SIM_DROP_PACKETS || r.nextInt(100) > RNG)
                        socket.send(allPackets.get(currentPacket).getAsUDPPacket());
                    else
                        System.out.println("SIM Drop of " + currentPacket);
                }
            } catch (IOException e) {
                System.out.println("Not all packets sent.");
            }

            try {
                //receive up to block of packets
                while (blockPackets.size() != 0) {
                    //System.out.println("awaiting acks.");
                    data = new byte[10];//number of bytes in ack packet
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    socket.receive(packet);
                    DataAcknowledgementPacket daPacket = new DataAcknowledgementPacket(packet);
                    long packetNum = daPacket.getPacketNum();
                    //System.out.println("Receieved ack for " + packetNum);
                    blockPackets.remove(packetNum);
                }

                //success we can increase the packet amount by 1
                if(HAS_SLIDING_WINDOW){
                    maxPackets++;
                }
            } catch (IOException e) {
                //insert the remainders
                packetQueue.addAll(blockPackets);
                System.out.println("Only a partial full block sent.");
                if(HAS_SLIDING_WINDOW){
                    maxPackets /= 2 ;
                    if(maxPackets < 1)
                        maxPackets = 1;
                }
            }
        }

        //file should have been sent.
        System.out.println("File sent");
        blockPackets = new HashSet<>();
        packetQueue = new ArrayDeque<>();
    }

}
