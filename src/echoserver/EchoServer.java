package echoserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;
import utils.Utils;

public class EchoServer {

    private static List<ClientHandler> clientHandlers = new ArrayList();
    private static ClientHandler current;

    private static boolean keepRunning = true;
    private static ServerSocket serverSocket;
    private static final Properties properties = Utils.initProperties("server.properties");

    public static void stopServer() {
        keepRunning = false;
    }

    private class ClientHandlerThread extends Thread {

        Socket socket;

        public ClientHandlerThread(Socket s) {
            socket = s;
        }

        public void run() {
            try {
                handleClient(socket);
            } catch (IOException ex) {
                Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void handleClient(Socket socket) throws IOException {
        Scanner input = new Scanner(socket.getInputStream());
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        String message = input.nextLine(); //IMPORTANT blocking call
        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, String.format("Received the message: %1$S ", message));
        while (!message.equals(ProtocolStrings.STOP)) {
            for (ClientHandler ch : clientHandlers) {
                ch.send(message.toUpperCase());
            }
            Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, String.format("Received the message: %1$S ", message.toUpperCase()));
            message = input.nextLine(); //IMPORTANT blocking call
        }
        // now the server has received a ##STOP## call from client
        writer.println(ProtocolStrings.STOP);//Echo the stop message back to the client for a nice closedown
        removeHandler(socket); // remove the handler associated with the current socket

        socket.close();
        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Closed a Connection");
    }

    private void runServer() {
        String logFile = properties.getProperty("logFile");
        Utils.setLogFile(logFile, EchoServer.class.getName());
        int port = Integer.parseInt(properties.getProperty("port"));
        String ip = properties.getProperty("serverIp");

        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Sever started. Listening on: " + port + ", bound to: " + ip);
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
            do {
                Socket socket = serverSocket.accept(); //Important Blocking call
                Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Connected to a client");
                clientHandlers.add(new ClientHandler(socket));
                new ClientHandlerThread(socket).start();
            } while (keepRunning);
        } catch (IOException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Utils.closeLogger(EchoServer.class.getName());
        }
    }

    private static void removeHandler(Socket s) {
        for (ClientHandler ch : clientHandlers) {
            Socket current = ch.socket;
            if (current == s) {
                // this means we have the same object - now delete the clienthandler
                // associated with it
                clientHandlers.remove(ch);
                break;
            }
        }

    }

    public static void main(String[] args) {

        new EchoServer().runServer();
    }
}
