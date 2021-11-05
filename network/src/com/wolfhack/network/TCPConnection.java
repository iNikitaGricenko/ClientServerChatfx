package com.wolfhack.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread;
    private final ITCPConnectionListener eventListener;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private String userName = "Guest";

    public TCPConnection(ITCPConnectionListener eventListener, String Address, int Port) throws IOException {
        this(eventListener, new Socket(Address, Port));
    }

    public TCPConnection(ITCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()) {
                        eventListener.onReceiveString(TCPConnection.this, reader.readLine());
                    }
                } catch (IOException ioException) {
                    eventListener.onException(TCPConnection.this, ioException);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();
    };

    public String getDate() {
        Date date = new Date();
        System.out.println("("+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+")");
        return "("+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds()+") ";
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName, TCPConnection tcpConnection) {
        tcpConnection.userName = userName;
        this.userName = userName;
    }

    public synchronized void sendMessage(String message) {
        try {
            writer.write(message + "\r\n");
            writer.flush();
        } catch (IOException ioException) {
            eventListener.onException(TCPConnection.this, ioException);
            disconnect();
        }
    }

    public synchronized String getUserIp(TCPConnection tcpConnection)
    {
        if (tcpConnection.socket.getInetAddress() == socket.getInetAddress())
            return socket.getInetAddress().toString();
        else
            return "Access denied";
    }

    public synchronized String getChannel(TCPConnection tcpConnection)
    {
        if (tcpConnection.socket.getInetAddress() == socket.getInetAddress())
            return socket.getChannel().toString();
        else
            return "Access denied";
    }

    public synchronized String getUserPort(TCPConnection tcpConnection)
    {
        if (tcpConnection.socket.getInetAddress() == socket.getInetAddress())
            return ""+socket.getPort();
        else
            return "Access denied";
    }

    public synchronized String getServerIp()
    {
        return socket.getLocalAddress().toString();
    }

    public synchronized String getServerPort()
    {
        return ""+socket.getLocalPort();
    }

    public synchronized String getServerAddress()
    {
        return socket.getLocalSocketAddress().toString();
    }

    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException ioException) {
            eventListener.onException(TCPConnection.this, ioException);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": "  + socket.getPort();
    }
}
