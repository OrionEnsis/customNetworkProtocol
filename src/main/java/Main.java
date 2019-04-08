import java.io.IOException;

public class Main {
    //args list -server/client -ipv4/6 -window/nowindow -droppackets
    public static void main(String[] args) {
        if(args[0].equalsIgnoreCase("Client")) {
            Client c = new Client(args);
            try {
                c.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            Server s = new Server(args);
            s.run();
        }
    }
}
