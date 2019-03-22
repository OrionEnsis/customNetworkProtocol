import Packets.BasicPacket;
import Packets.DataPacket;
import Packets.WriteAcknowledgementPacket;

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
    private short numOfPackets;
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
                System.out.println("Received Packet");
                BasicPacket bp = new BasicPacket(currentPacket);

                //get packet type
                packetType = bp.getOpCode();

                //if packet is file packet
                if(packetType == 1) {
                    //set up for file transfer
                    System.out.println("write request packet Received");
                    WriteAcknowledgementPacket waPacket = new WriteAcknowledgementPacket(currentPacket);
                    socket.send(waPacket.getAsUDPPacket());

                    //we now know packet information
                    bytes = new byte[waPacket.getPacketSize()];
                    currentPacket = new DatagramPacket(bytes,bytes.length);
                }
                //else if it is a data packet
                else if( packetType == 3) {
                    System.out.println("Data packet received");
                    //handle data
                    DataPacket dPacket = new DataPacket(currentPacket);
                    socket.send(dPacket.makeAcknowledgement().getAsUDPPacket());
                    receivedPackets.add(dPacket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //if all packets received
            if(numOfPackets == receivedPackets.size()) {
                //turn into a file
                makeFile();
                break;
            }
        }
    }

    private void makeFile() {
        try {
            //TODO add directory
            byte[] bytes = DataPacket.getDataFromCollection(receivedPackets);
            FileOutputStream fileOutputStream = new FileOutputStream(filename);
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
