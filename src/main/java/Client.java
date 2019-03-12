import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ArrayBlockingQueue;

class Client {
    private final int PORT = 2697;
    private final boolean HAS_SLIDING_WINDOW;
    private final boolean SIM_DROP_PACKETS;

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
                address = Inet4Address.getLocalHost();
            } else {
                //IPV6
                address = Inet6Address.getLocalHost();
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
        //prompt for directory
        //zip it up
        //make packets
        //start ack thread
        //start sending.
    }

    private DatagramPacket makeWriteRequestPacket(String filename){
        //2bytes == 1 for write request
        //filename
        //0 byte
        //mode -always octet
        //0 byte
        //packet num
        //0
        //packet write size
        //0
        return null;
    }

    private DatagramPacket makeDataPackets(byte[] data){
        //while we still have data to process
            //grab max size for packet -4 bytes
            //mark as data packet
            //mark packet dataNum;
            //save packet dataNum for later
        //Queue up all packets
        return null;
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

    private void send(){
        //send write request
        //get ack
        //while there are packets to send and we are still awaiting acks
            //if in single mode and still ack is still waiting.
                //continue
            //if send packet exists and is not received
                //if not sim drop
                    //send packet
                //add to ack queue
    }
}
