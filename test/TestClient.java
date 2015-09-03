
import echoclient.EchoClient;
import echoserver.EchoServer;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import presentation.ClientGUI;

/**
 * @author Lars Mortensen
 */
public class TestClient {

    public TestClient() {
    }

    @BeforeClass
    public static void setUpClass() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                EchoServer.main(null);
            }
        }).start();
    }

    @AfterClass
    public static void tearDownClass() {
        EchoServer.stopServer();
    }

    @Before
    public void setUp() {
    }

    @Test
    public void send() throws IOException {
        //ClientGUI client = new ClientGUI();
        //client.setVisible(true);
        EchoClient client = new EchoClient();
        client.connect("localhost", 9090);
        //client.connect("localhost", 9090);
        client.send("Hello");
        assertEquals("HELLO", client.receive());
        
    }

}
