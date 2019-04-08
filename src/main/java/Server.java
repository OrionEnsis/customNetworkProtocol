import Packets.BasicPacket;
import Packets.DataPacket;
import Packets.WriteAcknowledgementPacket;
import Packets.WriteRequestPacket;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashSet;

class Server {
    private final int PORT = 2697;
    private InetAddress address;
    private DatagramSocket socket;

    private String filename;
    private short packetSize;
    private long numOfPackets;
    private HashSet<DataPacket> receivedPackets;

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

        System.out.println("Server Established, awaiting connection");
    }

    void run(){
        byte[] bytes;
        if(packetSize == 0)
            bytes = new byte[15333];
        else
            bytes = new byte[packetSize];

        DatagramPacket currentPacket = new DatagramPacket(bytes,bytes.length);
        short packetType;
        //while true
        while(true) {
            try {
                //wait for packet
                socket.receive(currentPacket);
                //System.out.println("Received Packet");
                BasicPacket bp = new BasicPacket(currentPacket);

                //get packet type
                packetType = bp.getOpCode();
                //System.out.println("packet type: " + packetType);

                //if packet is file packet
                if(packetType == 1) {
                    //set up for file transfer
                    System.out.println("write request packet Received");
                    WriteRequestPacket wrPacket = new WriteRequestPacket(currentPacket);
                    socket.send(wrPacket.createAcknowledgementForThisPacket().getAsUDPPacket());
                    packetSize = wrPacket.getPacketSize();
                    numOfPackets = wrPacket.getNumOfPackets();
                    filename = wrPacket.getFileName();

                    System.out.println("filename " + filename);

                    //we now know packet information
                    bytes = new byte[packetSize];
                    currentPacket = new DatagramPacket(bytes,bytes.length);
                }
                //else if it is a data packet
                else if( packetType == 3) {
                    //handle data
                    DataPacket dPacket = new DataPacket(currentPacket);
                    socket.send(dPacket.makeAcknowledgement().getAsUDPPacket());
                    receivedPackets.add(dPacket);
//                    System.out.println(receivedPackets.size() + "/" + numOfPackets);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //if all packets received
            if(numOfPackets != 0 && numOfPackets == (long)receivedPackets.size()) {
                //turn into a file
                makeFile();
                numOfPackets = 0;
                receivedPackets.clear();
                bytes = new byte[15333];
                currentPacket = new DatagramPacket(bytes,bytes.length);
            }
        }
    }

    private void makeFile() {
        try {
            System.out.println("Making File " + filename);
            String extendeddir = filename.substring(0,filename.lastIndexOf(File.separator));
            String directory= System.getProperty("user.home")+ File.separator + "transferredFiles" + File.separator + extendeddir;
            String trueFilename = filename.substring(filename.lastIndexOf(File.separator));
            File dir = new File(directory);
            dir.mkdirs();
            File f = new File(directory, trueFilename);
            f.createNewFile();
            byte[] bytes = DataPacket.getDataFromCollection(receivedPackets);
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
