package kg.syntlex.client;


import kg.syntlex.message_connection.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSentTextFromConsole() {
        return false;
    }

    private static int counter = 0;

    @Override
    protected String getUserName() {
        if (counter == 99) counter = 0;
        return "date_bot_" + counter++;
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);

            String senderName = "";
            String senderMessageText;

            if (message.contains(": ")) {
                senderName = message.split(": ")[0];
                senderMessageText = message.split(": ")[1];
            } else {
                senderMessageText = message;
            }

            SimpleDateFormat format = null;

            switch (senderMessageText) {
                case "дата":
                    format = new SimpleDateFormat("d.MM.yyyy");
                    break;
                case "день":
                    format = new SimpleDateFormat("d");
                    break;
                case "месяц":
                    format = new SimpleDateFormat("MMMM");
                    break;
                case "год":
                    format = new SimpleDateFormat("yyyy");
                    break;
                case "время":
                    format = new SimpleDateFormat("H:mm:ss");
                    break;
                case "час":
                    format = new SimpleDateFormat("H");
                    break;
                case "минуты":
                    format = new SimpleDateFormat("m");
                    break;
                case "секунды":
                    format = new SimpleDateFormat("s");
                    break;
            }

            if (format != null)
                sendTextMessage("Информация для " + senderName + ": " + format.format(Calendar.getInstance().getTime()));
        }
    }
}
