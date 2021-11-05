package com.wolfhack.server;

import com.sun.jdi.Value;
import com.wolfhack.network.ITCPConnectionListener;
import com.wolfhack.network.TCPConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChatServer implements ITCPConnectionListener {

    public static void main(String[] args) {
        new ChatServer();
    }

    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    private final Thread sThread;

    private ServerSocket sSocket;

    private final Map<String, String> commands = new HashMap<>();
    {
        commands.put("!QUIT", "Using to disconnect from server");
        commands.put("!CLOSE_SERVER", "Using by administrator to close server connection");
        commands.put("!SIP", "Using to show your IP");
        commands.put("!SPORT", "Using to show your Port");
        commands.put("!SCHANNEL", "Using to show connection channel");
        commands.put("!SADDRESS", "Using to show server IP and Port");
        commands.put("!UIP", "Using to show server IP");
        commands.put("!UPORT", "Using to show server Port");
        commands.put("!CONNECTED_LIST", "Using to show all connections");
        commands.put("!SET_NAME:", "Using to set/change your nickname");
        commands.put("?", "Using to show command list");
    }

    private ChatServer() {
        System.out.println("Server running...");
        ChatServer s = this;
        sThread = new Thread()
        {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(8181)) {
                    sSocket = serverSocket;
                    while (!sThread.isInterrupted()) {
                        try {
                            new TCPConnection(s, sSocket.accept());
                        } catch (IOException ioException) {
                            System.out.println("TCPConnection exception: " + ioException);
                            if (ioException.getMessage().equals("Socket is closed"))
                            {
                                serverSocket.close();
                                sThread.interrupt();
                                System.exit(1);
                            }
                        }
                    }
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }
            }
        };
        sThread.start();
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        if (tcpConnection.getUserIp(tcpConnection).replace("/","").equals("192.168.0.102")) {
            sendToAllConnection("Administrator connected");
            return;
        }
        sendToAllConnection("Client connected: " + tcpConnection);
    }

    public void toDisconnect(TCPConnection tcpConnection)
    {
        tcpConnection.disconnect();
    }

    private void sendToAllConnection(String message) {
        System.out.println(message);

        for (TCPConnection i : connections)
            i.sendMessage(message);

    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String message) {
        if (commands.containsKey(message))
        {
            onCommand(tcpConnection, message);
            return;
        }
        if (message.startsWith("!SET_NAME:")) onCommand(tcpConnection, message);
        if (tcpConnection.getUserIp(tcpConnection).replace("/","").equals("192.168.0.102"))
            tcpConnection.setUserName("Administrator", tcpConnection);
        sendToAllConnection(tcpConnection.getDate() + tcpConnection.getUserName() + ": " + message);
    }

    private void onCommand(TCPConnection tcpConnection, String command)
    {
        String nick = "";
        if(command.startsWith("!SET_NAME:")){
            nick = command.substring(11);
            command = command.substring(0, 10);
            System.out.println(command);
            System.out.println(nick);
        }
        switch (command){
            case "!QUIT":
                sendToAllConnection("Disconnect from server...");
                toDisconnect(tcpConnection);
                return;
            case "!CLOSE_SERVER":
                if (tcpConnection.getUserName().equals("Administrator"))
                {
                    sendToAllConnection("(Admin) Closing server...");
                    try {
                        sSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                else
                    sendToAllConnection("Access to manipulation of server was denied");
                return;
            case "!SIP":
                sendToAllConnection("Server ip is " + tcpConnection.getServerIp());
                return;
            case "!SPORT":
                sendToAllConnection("Server port is " + tcpConnection.getServerPort());
                return;
            case "!SCHANNEL":
                sendToAllConnection("Channel is " + tcpConnection.getChannel(tcpConnection));
                return;
            case "!SADDRESS":
                sendToAllConnection("Server address is " + tcpConnection.getServerAddress());
                return;
            case "!UIP":
                sendToAllConnection("User ip is " + tcpConnection.getUserIp(tcpConnection));
                return;
            case "!UPORT":
                sendToAllConnection("User port is " + tcpConnection.getUserPort(tcpConnection));
                return;
            case "!CONNECTED_LIST":
                sendToAllConnection("Connections: ");
                for (TCPConnection conn: connections)
                    sendToAllConnection(conn.getUserIp(conn));
                return;
            case "!SET_NAME:":
                sendToAllConnection(tcpConnection.getUserName() + " changed his nickname to:");
                tcpConnection.setUserName(nick, tcpConnection);
                sendToAllConnection(tcpConnection.getUserName());
                return;
            case "?":
                sendToAllConnection("Commands: ");
                commands.forEach((Key, Value)->sendToAllConnection(Key + " \t " + Value));
                    //sendToAllConnection(com_s);
                return;
            default:
                sendToAllConnection("nothing happened");
        }
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnection("Client disconnected: " + tcpConnection);
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception exception) {
        System.out.println("TCPConnection exception: " + exception);
    }
}
