package kg.syntlex.client;


import kg.syntlex.message_connection.Message;
import kg.syntlex.message_connection.MessageType;

public class ClientGuiController extends Client {
    private final ClientGuiModel model = new ClientGuiModel();
    private final ClientGuiView view = new ClientGuiView(this);

    private volatile boolean isConnect = false; //флаг отобаржающий состояние подключения клиента  серверу

    public boolean isConnect() {
        return isConnect;
    }

    public static void main(String[] args) {
        ClientGuiController controller2 = new ClientGuiController();
        controller2.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new GuiSocketThread();
    }

    @Override
    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.run();
    }

    @Override
    protected String getServerAddress() {
        return view.getServerAddress();
    }

    @Override
    protected int getServerPort() {
        return view.getServerPort();
    }

    @Override
    protected String getUserName() {
        return view.getUserName();
    }

    public ClientGuiModel getModel() {
        return model;
    }

    public class GuiSocketThread extends SocketThread {
        @Override
        protected void processIncomingMessage(String message) {
            model.setNewMessage(message);
            view.refreshMessages();
        }

        @Override
        protected void informAboutAddingNewUser(String userName) {
            model.addUser(userName);
            view.refreshUsers();
        }

        @Override
        protected void informAboutDeletingNewUser(String userName) {
            model.deleteUser(userName);
            view.refreshUsers();
        }

        @Override
        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            isConnect = clientConnected;
            view.notifyConnectionStatusChanged(clientConnected);
        }
    }


    //метод реализующий отключение нашего клиента от чата
    protected void disableClient() {
        try {
            if (isConnect) {
                connection.send(new Message(MessageType.DISABLE_USER));
//                model.getAllUserNames().clear();
                isConnect = false;
                view.refreshListUsers(model.getAllUserNames());
                view.errorDialogWindow("Time is over");
            } else view.errorDialogWindow("Вы уже отключены");
        } catch (Exception e) {
            view.errorDialogWindow("Сервисное сообщение: произошла ошибка при отключении.");
        }
    }


}
