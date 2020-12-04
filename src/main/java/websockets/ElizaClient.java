package websockets;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.tyrus.client.ClientManager;
import websockets.web.ElizaClientEndpoint;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ElizaClient {
  private static final Logger LOGGER = Grizzly.logger(ElizaClient.class);
  public static final CountDownLatch LATCH = new CountDownLatch(1);

  public static void main(String[] args) {
    runClient();
  }

  private static void runClient() {
    ClientManager client = ClientManager.createClient();
    try {
      client.connectToServer(ElizaClientEndpoint.class, new URI("ws://localhost:8080/eliza"));
      LATCH.await();
    } catch (DeploymentException | IOException | URISyntaxException | InterruptedException e) {
      LOGGER.log(Level.SEVERE, e.toString(), e);
    }
  }
}
