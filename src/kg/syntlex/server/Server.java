package kg.syntlex.server;

import kg.syntlex.message_connection.Connection;
import kg.syntlex.message_connection.ConsoleHelper;
import kg.syntlex.message_connection.Message;
import kg.syntlex.message_connection.MessageType;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Enter the port number:");
        int port = ConsoleHelper.readInt();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Server started");

            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }

        } catch (IOException ex) {
            ConsoleHelper.writeMessage(ex.getMessage());
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
            try {
                entry.getValue().send(message);
            } catch (IOException ex) {
                ConsoleHelper.writeMessage("Failed to send a message");
            }
        }
    }

    private static class Handler extends Thread {
        private final Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Connection to a remote address: "
                    + socket.getRemoteSocketAddress() + " has been established");
            String username = null;

            try (Connection connection = new Connection(socket)) {
                username = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, username));
                sendListOfUsers(connection, username);
                serverMainLoop(connection, username);
                Message message = connection.receive();

          //      attachFile();

                if (message.getType() == MessageType.DISABLE_USER) {
                    connectionMap.remove(username);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, username));
                    ConsoleHelper.writeMessage("Connection with the server has been closed.");
                    connection.close();
                }
            } catch (IOException | ClassNotFoundException ex) {
                ConsoleHelper.writeMessage("An error occurred while exchanging data with remote address");
            }
            // Здесь можно добавить таймер и удалить пользователя с чата


            connectionMap.remove(username);
            sendBroadcastMessage(new Message(MessageType.USER_REMOVED, username));
            //     ConsoleHelper.writeMessage("Connection with the server has been closed.");


        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message response = connection.receive();

                if (response.getType() == MessageType.USER_NAME) {
                    String username = response.getData();
                    if (username != null && !username.isEmpty()) {
                        if (connectionMap.get(username) == null) {
                            connectionMap.put(username, connection);
                            connection.send(new Message(MessageType.NAME_ACCEPTED));
                            return username;
                        } else
                            ConsoleHelper.writeMessage("User with this name already exists in this chat. Try another name.");
                    } else ConsoleHelper.writeMessage("The username can not be empty. Try again.");
                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for (String user : connectionMap.keySet()) {
                Message message = new Message(MessageType.USER_ADDED, user);
                if (!user.equals(userName)) connection.send(message);
            }
        }

        //Этап третий – главный цикл обработки сообщений сервером.
//Добавь приватный метод void serverMainLoop(Connection connection, String userName)
//throws IOException, ClassNotFoundException, где значение параметров такое же,
//как и у метода sendListOfUsers. Он должен:
//10.1.	 Принимать сообщение клиента
//10.2.	 Если принятое сообщение – это текст (тип TEXT), то формировать новое
//текстовое сообщение путем конкатенации: имени клиента, двоеточия, пробела и
//текста сообщения. Например, если мы получили сообщение с текстом "привет чат" от
//пользователя "Боб", то нужно сформировать сообщение "Боб: привет чат".
//10.3.	 Отправлять сформированное сообщение всем клиентам с помощью метода
//sendBroadcastMessage.
//10.4.	 Если принятое сообщение не является текстом, вывести сообщение об ошибке
//10.5.	 Организовать бесконечный цикл, внутрь которого перенести функционал
//пунктов 10.1-10.4.
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message receivedMessage = connection.receive();
                if (receivedMessage.getType() == MessageType.TEXT) {
                    StringBuffer text = new StringBuffer();
                    // Сюда можно добавить дату. Наример 25.08.2020 Боб: привет чат
                    text.append(userName).append(": ").append(receivedMessage.getData());
                    sendBroadcastMessage(new Message(MessageType.TEXT, text.toString()));
                    // Вместо There is an error можно добавить картинку или
                    //голосовое сообщения если это не текст
                } else ConsoleHelper.writeMessage("There is an error");
            }
        }

        private void attachFile() {

            String object;
            try {
                while (true) {

                    ObjectInputStream obIn = new ObjectInputStream(socket.getInputStream());

                    while ((object = (String) obIn.readObject()) != null) {
                        if (object.equals("Send photo")) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss'.txt'");
                            String fileName = dateFormat.format(new Date());
                            FileOutputStream out = new FileOutputStream(fileName);
                            DataInputStream in = new DataInputStream(socket.getInputStream());
                            byte[] bytes = new byte[5 * 1024];
                            int count, total = 0;
                            long lenght = in.readLong();
                            while ((count = in.read(bytes)) > -1) {
                                total += count;
                                out.write(bytes, 0, count);
                                if (total == lenght) break;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Unfortunaly attach");
            }


        }
    }
}
