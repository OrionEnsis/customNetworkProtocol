import java.net.*;

class Server {
    private final int PORT = 2697;
    private InetAddress address;
    private DatagramSocket socket;

    Server(String[] args){
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
        //while true
            //wait for packet
            //if packet is file packet
                //set up for file transfer
            //else if it is a data packet
                //handle data

            //if all packets received
                //turn into a file
    }

    private void handleWriteRequest(DatagramPacket packet){
        //read packet
        //2bytes == 1 for write request
        //filename
        //0 byte
        //mode -always octet
        //0 byte
        //packet num
        //0
        //packet size
        //0

        //create a byte array for them.
        //send ack back
    }

    private void sendWriteAck(){
        //2 byte = 6
        //0
        //packet num
        //0
        //packet size
        //0
    }

    private void sendDataAck(){
        //2 byte 4
        //2 byte package number
    }

    private void makeFile(){
        //get save directory
        //unzip file
        //put file there
    }
}
