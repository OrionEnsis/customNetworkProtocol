
import Packets.DataAcknowledgementPacket;
import Packets.DataPacket;
import Packets.WriteRequestPacket;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;


class Client {
    private final int RNG = 1;
    private final int PORT = 2697;
    private final boolean HAS_SLIDING_WINDOW;
    private final boolean SIM_DROP_PACKETS;
    private boolean BENCHMARK;
    private final short PACKET_SIZE = 512;
    private final String SEND_ADDRESS_V4 = "pi.cs.oswego.edu";
    private final String SEND_ADDRESS_V6 = "fe80::225:90ff:fe4d:f030";
    private final int TIMEOUT = 500;
    private final int BLOCK_SIZE = 32;

    private String statsFile;
    private DatagramSocket socket;
    private InetAddress address;

    private HashSet<Long> blockPackets;                //set of packets awaiting ack
    private HashMap<Long, DataPacket> allPackets;      //hash of all packets
    private Queue<Long> packetQueue;                   //queue of all remaining packets

    Client(String[] args){
        blockPackets = new HashSet<>();
        allPackets = new HashMap<>();
        packetQueue = new ArrayDeque<>();
        statsFile = "results-";

        //get ipvMode
        try {
            socket = new DatagramSocket(PORT);
            socket.setSoTimeout(TIMEOUT);

            //get ipvMode
            if (args[1].equals("ipv4")) {
                //IPV4
                address = Inet4Address.getByName(SEND_ADDRESS_V4);
                statsFile+="IPV4-";
            } else {
                //IPV6
                address = Inet6Address.getByName(SEND_ADDRESS_V6);
                statsFile+="IPV6-";
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        //get window or stack
        HAS_SLIDING_WINDOW = args[2].equalsIgnoreCase("window");
        if(HAS_SLIDING_WINDOW){
            statsFile+="window-";
        }
        else{
            statsFile+="nowindow-";
        }

        //get drop packets.
        SIM_DROP_PACKETS = args[3].equalsIgnoreCase("SimON");
        if(SIM_DROP_PACKETS){
            statsFile+="drop";
        }
        else{
            statsFile+="nodrop";
        }

        try{
            BENCHMARK = args[4].equalsIgnoreCase("benchmark");
        }catch(ArrayIndexOutOfBoundsException e){
            BENCHMARK = false;
        }
    }

    void run() throws IOException {
        String filename;
        File f;
        WriteRequestPacket start;
        int trials;
        if(BENCHMARK)
            trials = 100;
        else
            trials = 1;

        //TODO file writing goes here
        FileWriter fw = new FileWriter(statsFile+".csv");
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("Time,\n");

        for (int i = 0; i < trials; i++) {
            long startTime = System.nanoTime();
            //prompt for directory
            filename = "/home/jspagnol/Desktop/Network TransferTestFiles";
            f = new File(filename);
            filename = f.getName();

            //start sending.
            if (f.isDirectory()) {
                getAllFiles(f, filename);
            } else {
                //make packets
                makeDataPackets(f);
                System.out.println("sending " + filename);
                start = new WriteRequestPacket(address, PORT, filename, PACKET_SIZE, (long) allPackets.size());
                send(start);
            }
            long endTime = System.nanoTime();
            long time = endTime-startTime;
            bw.write(time + ",\n");
            bw.flush();
        }
        bw.close();
    }

    private void getAllFiles(File file,String dir) {
        File[] files = file.listFiles();
        if(files != null) {
            for (File f :
                    files) {

                if (f.isDirectory()) {
                    getAllFiles(f, dir + File.separator + f.getName());
                } else {
                    System.out.println("sending " + f.getName());
                    makeDataPackets(f);
                    send(new WriteRequestPacket(address, PORT, dir + File.separator + f.getName(), PACKET_SIZE, (long) allPackets.size()));
                }
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
        long estRTT = 0;
        long sampleRTT;
        long diff;
        long dev = 0;
        long timeout = TIMEOUT;

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
                long eststart = System.currentTimeMillis();
                socket.send(startPacket.getAsUDPPacket());

                //get ack
                //TODO this should examine for a correct acknowledgement
                socket.receive(receiveAck);
                long estend = System.currentTimeMillis();
                estRTT = estend -eststart;
                socket.setSoTimeout((int)estRTT);
                notReceivedStartAck = false;
                System.out.println("acknowledgement received.  Starting data Transfer");
            } catch (IOException e) {
                System.out.println("packet failed for some reason, resending");
            }
        }
        int falsePackets = 0;
        //while there are packets to send and we are still awaiting acks
        while(!blockPackets.isEmpty() || !packetQueue.isEmpty() || falsePackets > 0) {
            long currentPacket;
            long sampleStart = 0;
            long sampleEnd = 1;

            //send "block" of packets
            try {

                blockPackets = new HashSet<>();
                sampleStart = System.currentTimeMillis();
                while(!packetQueue.isEmpty() && blockPackets.size() + falsePackets< maxPackets) {
                    currentPacket = packetQueue.poll();
                    blockPackets.add(currentPacket);
                    if(!SIM_DROP_PACKETS || r.nextInt(100) > RNG)
                        socket.send(allPackets.get(currentPacket).getAsUDPPacket());
                    else {
                        //System.out.println("SIM Drop of " + currentPacket);
                        falsePackets++;
                    }
                }

                //socket.setSoTimeout((int) timeout);
                //receive up to block of packets
                if (blockPackets.size() + falsePackets != 0) {

                    //System.out.println("awaiting acks.");
                    data = new byte[10];//number of bytes in ack packet
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    socket.receive(packet);
                    sampleEnd = System.currentTimeMillis();
                    DataAcknowledgementPacket daPacket = new DataAcknowledgementPacket(packet);
                    long packetNum = daPacket.getPacketNum();
                    //System.out.println("Receieved ack for " + packetNum);
                    blockPackets.remove(packetNum);
                }

                //success we can increase the packet amount by 1
                if(HAS_SLIDING_WINDOW){
                    maxPackets++;
                }

                //calculate new timeout
                sampleRTT = sampleEnd - sampleStart;
                diff = sampleRTT - estRTT;
                estRTT = estRTT + (long)(.75*diff);
                dev = (long)(dev + (.25 * (Math.abs(diff)-dev)));
                timeout = estRTT + 4*dev;

                if(timeout < 2)
                    timeout = 2;
                //System.out.println("timeout: " + timeout);
                socket.setSoTimeout((int)timeout);

            } catch (IOException e) {
                //insert the remainders
                packetQueue.addAll(blockPackets);
                falsePackets = 0;
                //System.out.println("Only a partial full block sent.");
                if(HAS_SLIDING_WINDOW){
                    maxPackets /= 2 ;
                    if(maxPackets < 2)
                        maxPackets = 2;
                }
                try {
                    timeout *=2;
                    if(timeout > 128)
                        timeout = 128;
                    socket.setSoTimeout((int)timeout);
                    System.out.println("timeout: " + timeout);
                } catch (SocketException ex) {
                    ex.printStackTrace();
                }
            }
        }

        //file should have been sent.
        System.out.println("File sent");
        blockPackets.clear();
        packetQueue.clear();
    }

}
