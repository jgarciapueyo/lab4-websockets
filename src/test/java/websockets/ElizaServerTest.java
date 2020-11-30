package websockets;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.tyrus.client.ClientManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import websockets.web.ElizaWebSocket;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static java.lang.String.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ElizaServerTest {

	private static final Logger LOGGER = Grizzly.logger(ElizaServerTest.class);
	
	@Value("${local.server.port}")
    private int port;
	private String url;

	@Before
	public void setup() throws DeploymentException {
		url = "ws://localhost:" + port + "/eliza";
	}

	@Test(timeout = 5000)
	public void onOpen() throws DeploymentException, IOException, URISyntaxException, InterruptedException {
		CountDownLatch latch = new CountDownLatch(3);
		List<String> list = new ArrayList<>();
		ClientEndpointConfig configuration = ClientEndpointConfig.Builder.create().build();
		ClientManager client = ClientManager.createClient();
		Session session = client.connectToServer(new Endpoint() {

			@Override
			public void onOpen(Session session, EndpointConfig config) {
				session.addMessageHandler(new ElizaOnOpenMessageHandler(list, latch));
			}

		}, configuration, new URI(url));
        session.getAsyncRemote().sendText("bye");
        latch.await();
		assertEquals(3, list.size());
		assertEquals("The doctor is in.", list.get(0));
	}

	@Test(timeout = 1000)
	public void onChat() throws DeploymentException, IOException, URISyntaxException, InterruptedException {
		List<String> list = new ArrayList<>();
		ClientEndpointConfig configuration = ClientEndpointConfig.Builder.create().build();
		ClientManager client = ClientManager.createClient();
		Session session = client.connectToServer(new ElizaEndpointToComplete(list), configuration, new URI(url));
		
		session.getAsyncRemote().sendText("sorry for being an asshole");
		Thread.sleep(250); //Wait for the answer of the server
		session.getAsyncRemote().sendText("bye");
		Thread.sleep(250); //Wait for the server to close the current client
		assertEquals(5, list.size());
		assertEquals("Please don't apologize.", list.get(3));
	}

    private static class ElizaOnOpenMessageHandler implements MessageHandler.Whole<String> {

        private final List<String> list;
        private final CountDownLatch latch;

        ElizaOnOpenMessageHandler(List<String> list, CountDownLatch latch) {
            this.list = list;
            this.latch = latch;
        }

        @Override
        public void onMessage(String message) {
            LOGGER.info(format("Client received \"%s\"", message));
            list.add(message);
            latch.countDown();
        }
    }

    private static class ElizaEndpointToComplete extends Endpoint {

        private final List<String> list;

        ElizaEndpointToComplete(List<String> list) {
            this.list = list;
        }

        @Override
        public void onOpen(Session session, EndpointConfig config) {
            session.addMessageHandler(new ElizaMessageHandlerToComplete());
        }

        private class ElizaMessageHandlerToComplete implements MessageHandler.Whole<String> {

            @Override
            public void onMessage(String message) {
                list.add(message);
            }
        }
    }
}
