package com.groendom_chat.groep_technologies.ClientServer.Client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by P on 23.10.2017.
 */

public class SocketHandler {

  private static Socket socket;
  private static ObjectInputStream inputStream;
  private static ObjectOutputStream outputStream;

  public static synchronized Socket getSocket() {
    return socket;
  }

  public static synchronized void setSocket(Socket socket) {
    SocketHandler.socket = socket;
  }

  public static synchronized ObjectInputStream getInputStream() {
    return inputStream;
  }

  public static synchronized void setInputStream(ObjectInputStream inputStream) {
    SocketHandler.inputStream = inputStream;
  }

  public static synchronized ObjectOutputStream getOutputStream() {
    return outputStream;
  }

  public static synchronized void setOutputStream(ObjectOutputStream outputStream) {
    SocketHandler.outputStream = outputStream;
  }
}