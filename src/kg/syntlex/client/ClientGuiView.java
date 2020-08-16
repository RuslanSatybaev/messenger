package kg.syntlex.client;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ClientGuiView {
    private final ClientGuiController controller2;

    // path of the wav file
    private final File wavFile = new File("D:/test_sound/RecordSound.wav");


    // format of audio file
    private final AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    int counter = 10;
    Boolean isIt = false;


    private final JFrame frame = new JFrame("Chat");// Создаем фрейм
    private final JTextArea messages = new JTextArea(10, 30);

    Icon iconAttach = new ImageIcon("src/kg/syntlex/client/images/attach_file.png");
    Icon iconRecord = new ImageIcon("src/kg/syntlex/client/images/microphone.png");

    private final JPanel panelEast = new JPanel();
    private final JList<String> list = new JList<String>();
    private final JTextArea users = new JTextArea(20, 20);
    private final JLabel timerName = new JLabel("Timer");
    private final JLabel countdown = new JLabel("00 : 30 : 00");

    private final JPanel panelEastSouth = new JPanel();
    private final JButton buttonStart = new JButton("Start");
    private final JButton buttonStop = new JButton("Stop");

    private final JPanel panel = new JPanel();
    private final JTextField textField = new JTextField(30);
    private final JButton buttonDisable = new JButton("Отключиться");
    private final JButton buttonConnect = new JButton("Подключиться");
    private final JButton buttonSend = new JButton("Send");
    private final JButton buttonAttach = new JButton(iconAttach);
    private final JButton buttonRecord = new JButton(iconRecord);
    private final JButton stopBtn = new JButton("Stop");
    private final JButton playBtn = new JButton("Playback");


    public ClientGuiView(ClientGuiController controller2) {
        this.controller2 = controller2;
        initView();
    }

    //метод, инициализирующий графический интерфейс клиентского приложения
    private void initView() {

        timerName.setFont(new Font("Tahoma", Font.PLAIN, 18));

        //   list.addListSelectionListener(new MyListSelectionListener());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(list);
        panelEast.add(theList);
        //   incomingList.setListData(listVector);


        messages.setEditable(false);
        users.setEditable(false);

        frame.getContentPane().add(new JScrollPane(messages), BorderLayout.CENTER);

        panel.add(buttonRecord);
        panel.add(stopBtn);
        panel.add(playBtn);

        panel.add(textField);
        panel.add(buttonSend);
        panel.add(buttonConnect);
        panel.add(buttonDisable);
        panel.add(buttonAttach);

        list.setBackground(Color.LIGHT_GRAY);
        panelEast.add(users);
        panelEast.add(timerName);
        panelEast.add(countdown);
        countdown.setBackground(Color.green);

        panelEast.add(buttonStart);
        panelEast.add(buttonStop);

        panelEast.setLayout(new BoxLayout(panelEast, BoxLayout.Y_AXIS));
        frame.add(panelEast, BorderLayout.EAST);

        panelEastSouth.setLayout(new BoxLayout(panelEastSouth, BoxLayout.X_AXIS));
        frame.add(new JScrollPane(panel), BorderLayout.SOUTH);
        frame.pack();

        // Эта строка завершает работу программы при закрытии окна
        // (если не добавить ее, то приложение будеть висеть вечно)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);// Делаем фрейм видимым

        textField.addActionListener(e -> {// Здесь логика отправка через enter
            controller2.sendTextMessage(textField.getText());
            textField.setText("");

        });

        buttonAttach.addActionListener(new ButtonAttach());

        buttonDisable.addActionListener(e -> controller2.disableClient());

        //---- btnStart ----

        buttonStart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                btnStartMouseClicked(e);
            }
        });

        //---- btnStop ----

        buttonStop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                btnStopMouseClicked(e);
            }
        });

        buttonRecord.addActionListener(
                e -> {
                    buttonRecord.setEnabled(false);
                    stopBtn.setEnabled(true);
                    playBtn.setEnabled(false);
                    //Захват данных
                    // с микрофона
                    //пока не нажата Stop
                    captureAudio();
                }
        );


        stopBtn.addActionListener(
                e -> {
                    buttonRecord.setEnabled(true);
                    stopBtn.setEnabled(false);
                    playBtn.setEnabled(true);
                    //Остановка захвата
                    // информации с микрофона

                    stopCapture = true;
                }
        );


        playBtn.addActionListener(
                e -> {
                    //Проигрывание данных
                    // которые были записаны

                    playAudio();
                }
        );


    }


        private void btnStartMouseClicked(MouseEvent evt) {//GEN-FIRST:event_btnStartMouseClicked
            Timer timer = new Timer(); //new timer
            counter = 50; //setting the counter to 10 sec
            TimerTask task = new TimerTask() {
                public void run() {
                    countdown.setText(Integer.toString(counter)); //the timer lable to counter.
                    counter--;

                    if (counter == -1) {
                        timer.cancel();
                        controller2.disableClient();
                    } else if (isIt) {
                        timer.cancel();
                        isIt = false;
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 1000, 1000); // =  timer.scheduleAtFixedRate(task, delay, period);
        }//GEN-LAST:event_btnStartMouseClicked

        private void btnStopMouseClicked(MouseEvent evt) {//GEN-FIRST:event_btnStopMouseClicked
            isIt = true; // changing the boolean isIt to true, which will stop the timer.
        }//GEN-LAST:event_btnStopMouseClicked


    //метод обновляющий список имен подлючившихся пользователей
    protected void refreshListUsers(Set<String> listUsers) {
        users.setText("");
        if (controller2.isConnect()) {
            StringBuilder text = new StringBuilder("Список пользователей:\n");
            for (String user : listUsers) {
                text.append(user).append("\n");
            }
            users.append(text.toString());
        }
    }

    public String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Введите адрес сервера:",
                "Конфигурация клиента",
                JOptionPane.QUESTION_MESSAGE);
    }

    public int getServerPort() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame,
                    "Введите порт сервера:",
                    "Конфигурация клиента",
                    JOptionPane.QUESTION_MESSAGE);
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Был введен некорректный порт сервера. Попробуйте еще раз.",
                        "Конфигурация клиента",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public String getUserName() {
        return JOptionPane.showInputDialog(
                frame,
                "Введите ваше имя:",
                "Конфигурация клиента",
                JOptionPane.QUESTION_MESSAGE);
    }

    public void notifyConnectionStatusChanged(boolean clientConnected) {
        textField.setEditable(clientConnected);
        if (clientConnected) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Соединение с сервером установлено",
                    "Чат",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Клиент не подключен к серверу",
                    "Чат",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    public void refreshMessages() {
        messages.append(controller2.getModel().getNewMessage() + "\n");
    }

    public void refreshUsers() {
        ClientGuiModel model = controller2.getModel();
        StringBuilder sb = new StringBuilder();
        for (String userName : model.getAllUserNames()) {
            sb.append(userName).append("\n");
        }
        users.setText(sb.toString());
    }

    //вызывает окно ошибки с заданным текстом
    protected void errorDialogWindow(String text) {
        JOptionPane.showMessageDialog(
                frame, text,
                "Notification", JOptionPane.INFORMATION_MESSAGE
        );
    }


    boolean stopCapture = false;
    ByteArrayOutputStream byteArrayOutputStream;
    AudioFormat audioFormat;
    TargetDataLine targetDataLine;
    AudioInputStream audioInputStream;
    SourceDataLine sourceDataLine;

    //Этот метод захватывает аудио
    // с микрофона и сохраняет
    // в объект ByteArrayOutputStream
    private void captureAudio() {
        try {
            //Установим все для захвата

            audioFormat = getAudioFormat();
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            targetDataLine.open(audioFormat);
            targetDataLine.start();

            //Создаем поток для захвата аудио
            // и запускаем его
            //он будет работать
            //пока не нажмут кнопку
            Thread captureThread = new Thread(
                    new CaptureThread());
            captureThread.start();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    //Этот метод проигрывает аудио
    // данные, которые были сохранены
    // в ByteArrayOutputStream
    private void playAudio() {
        try {
            //Устанавливаем всё
            //для проигрывания

            byte[] audioData = byteArrayOutputStream.toByteArray();

            InputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
            AudioFormat audioFormat = getAudioFormat();
            audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat,
                    audioData.length / audioFormat.
                            getFrameSize());
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine)
                    AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            //Создаем поток для проигрывания
            // данных и запускаем его
            // он будет работать пока
            // все записанные данные не проиграются

            Thread playThread = new Thread(new PlayThread());
            playThread.start();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    //Этот метод создает и возвращает
    // объект AudioFormat

    private AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(
                sampleRate,
                sampleSizeInBits,
                channels,
                signed,
                bigEndian);
    }
//===================================//

    //Внутренний класс для захвата
// данных с микрофона
    class CaptureThread extends Thread {

        byte[] tempBuffer = new byte[10000];

        public void run() {
            byteArrayOutputStream =
                    new ByteArrayOutputStream();
            stopCapture = false;
            try {
                while (!stopCapture) {
                    int cnt = targetDataLine.read(
                            tempBuffer,
                            0,
                            tempBuffer.length);
                    if (cnt > 0) {
                        //Сохраняем данные в выходной поток

                        byteArrayOutputStream.write(
                                tempBuffer, 0, cnt);
                    }
                }
                byteArrayOutputStream.close();
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }

    //===================================//
//Внутренний класс  для
// проигрывания сохраненных аудио данных
    class PlayThread extends Thread {
        byte[] tempBuffer = new byte[10000];

        public void run() {
            try {
                int cnt;
                // цикл пока не вернется -1

                while ((cnt = audioInputStream.
                        read(tempBuffer, 0,
                                tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        //Пишем данные во внутренний
                        // буфер канала
                        // откуда оно передастся
                        // на звуковой выход
                        sourceDataLine.write(
                                tempBuffer, 0, cnt);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmmss'.wav'");
                        String fileName = dateFormat.format(new Date());
                        AudioSystem.write(audioInputStream, fileType, wavFile);
                    }
                }

                sourceDataLine.drain();
                sourceDataLine.close();
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    }


}
