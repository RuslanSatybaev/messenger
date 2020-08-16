package kg.syntlex.client;

import kg.syntlex.message_connection.Connection;
import kg.syntlex.message_connection.ConsoleHelper;
import kg.syntlex.message_connection.Message;
import kg.syntlex.message_connection.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        try {
            synchronized (this) {
                this.wait();
            }
        } catch (InterruptedException ex) {
            ConsoleHelper.writeMessage("A problem occurred. Exiting.");
            return;
        }

        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");

            String messageText;

            while (!(messageText = ConsoleHelper.readString()).equals("exit")) {
                if (shouldSentTextFromConsole()) sendTextMessage(messageText);
            }
        } else ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter the server address:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter the port number:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter your nickname:");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSentTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        Message message = new Message(MessageType.TEXT, text);
        try {
            connection.send(message);
        } catch (IOException ex) {
            ConsoleHelper.writeMessage("Error occurred while sending your message");
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("User \"" + userName + "\" joined this chat");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("User \"" + userName + "\" left this chat");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message received = connection.receive();
                switch (received.getType()) {
                    case NAME_REQUEST: {
                        String name = getUserName();
                        connection.send(new Message(MessageType.USER_NAME, name));
                        break;
                    }
                    case NAME_ACCEPTED: {
                        notifyConnectionStatusChanged(true);
                        return;
                    }
                    default:
                        throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                switch (message.getType()) {
                    case TEXT: {
                        processIncomingMessage(message.getData());
                        break;
                    }
                    case USER_ADDED: {
                        informAboutAddingNewUser(message.getData());
                        break;
                    }
                    case USER_REMOVED: {
                        informAboutDeletingNewUser(message.getData());
                        break;
                    }
                    default:
                        throw new IOException("Unexpected MessageType");
                }
            }
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(getServerAddress(), getServerPort());
                Client.this.connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException ex) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}
