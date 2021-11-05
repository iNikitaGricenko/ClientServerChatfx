package com.wolfhack.client;

import com.wolfhack.network.ITCPConnectionListener;
import com.wolfhack.network.TCPConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

public class ChatClient extends Application {

    class ClientSide implements ITCPConnectionListener {
        static final String IP_ADDRESS      =       "192.168.0.102";
        static final int PORT               =       8181;
        TCPConnection connection;

        private ClientSide() {
            try {
                connection = new TCPConnection(this, IP_ADDRESS, PORT);
            } catch (IOException ioException) {
                printMessage("Connection exception: " + ioException, null);
            }
        }

        @Override
        public void onConnectionReady(TCPConnection tcpConnection) {
            printMessage("Connection ready...", tcpConnection);
        }

        @Override
        public void onReceiveString(TCPConnection tcpConnection, String message) {
            printMessage(message, tcpConnection);
        }

        @Override
        public void onDisconnect(TCPConnection tcpConnection) {
            printMessage("Connection close", null);
        }

        @Override
        public void onException(TCPConnection tcpConnection, Exception exception) {
            printMessage("Connection exception: " + exception, tcpConnection);
        }

        public void Send() {
            String message = fldMessage.getText();
            if (message.equals("")) return;
            fldMessage.setText(null);
            connection.sendMessage(message);
            System.out.println(connection.getUserName());
        }
        public void Send(String msg) {
            if (msg.equals("")) return;
            connection.sendMessage(msg);
            System.out.println(client.connection.getUserName());
        }
    }

    private static final int WIDTH   =  600;
    private static final int HEIGHT  =  400;
    private ClientSide client = new ClientSide();

    public static void main(String[] args) {
        launch(args);
    }

    @FXML
    private Button btnSend = new Button();
    @FXML
    private TextField fldMessage = new TextField(), fldNickname = new TextField();
    @FXML
    private TextArea ChatField = new TextArea();

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("chat.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        primaryStage.setTitle("Chat");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public void initialize() {
        fldNickname.setText("User" + new Random().nextInt(hashCode()));
        fldNickname.setOnAction(e -> {
            client.Send(" changed his nickname to: " + fldNickname.getText());
            client.connection.setUserName("WolFHacK", client.connection);
            System.out.println(client.connection.getUserName());
        });
    }

    private synchronized void printMessage(String message, TCPConnection tcpConnection) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ChatField.appendText(message + "\n");
            }
        });
    }

    @FXML
    private void SendMessage(ActionEvent event) {
        client.Send();
    }

    @FXML
    private void SendOnField(KeyEvent event) {
        if (!event.getCode().equals(KeyCode.ENTER)) return;
        client.Send();
    }
}
