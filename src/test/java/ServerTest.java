import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class ServerTest {

    @Test
    void getStringFromBufferTest1() {
        String testWord = "Phillip";
        ByteBuffer b = ByteBuffer.allocate(testWord.length()+1);
        b.put(testWord.getBytes());
        b.put((byte) 0);
        b.flip();

        String[] args = {"Server", "ipv4"};
        Server s = new Server(args);

        String result = s.getStringFromBuffer(b);
        assertEquals(result, testWord);
    }
}