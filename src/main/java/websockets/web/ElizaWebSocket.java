package websockets.web;

import org.glassfish.grizzly.Grizzly;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import websockets.service.Eliza;

public class ElizaWebSocket extends TextWebSocketHandler {

  private static final Logger LOGGER = Grizzly.logger(ElizaWebSocket.class);
  private Eliza eliza = new Eliza();

  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
    LOGGER.info("Server Message ... " + session.getId());
    Scanner currentLine = new Scanner(message.getPayload().toLowerCase());
    if (currentLine.findInLine("bye") == null) {
      LOGGER.info("Server received \"" + message + "\"");
      session.sendMessage(new TextMessage(eliza.respond(currentLine)));
      session.sendMessage(new TextMessage("---"));
    } else {
      session.close(new CloseStatus(CloseStatus.NORMAL.getCode(), "Alright then, goodbye!"));
    }
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws IOException {
    LOGGER.info("Server Connected ... " + session.getId());
    session.sendMessage(new TextMessage("The doctor is in."));
    session.sendMessage(new TextMessage("What's on your mind?"));
    session.sendMessage(new TextMessage("---"));
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeReason) throws IOException {
    LOGGER.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable errorReason) throws IOException {
    LOGGER.log(Level.SEVERE,
            String.format("Session %s closed because of %s", session.getId(), errorReason.getClass().getName()),
            errorReason);
  }

}
