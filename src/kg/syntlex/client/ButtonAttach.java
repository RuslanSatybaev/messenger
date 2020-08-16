package kg.syntlex.client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ButtonAttach implements ActionListener {
    private JFrame frame;
    private Socket socket;

    public void actionPerformed(ActionEvent ev) {
        System.out.println("OpenMenu");
        JFileChooser fileOpen = new JFileChooser();
        fileOpen.showOpenDialog(frame);
        attachFile(fileOpen.getSelectedFile());
    }

    private void attachFile(File selectedFile) {
        System.out.println("attachFile");
        try {
            String str = "Send photo";
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(str);
            out.flush();
            DataOutputStream outF = new DataOutputStream(socket.getOutputStream());
            FileInputStream inF = new FileInputStream(selectedFile);
            byte[] bytes = new byte[5 * 1024];
            int count;
            long lengths = selectedFile.length();
            outF.writeLong(lengths);
            while ((count = inF.read(bytes)) > -1) {
                outF.write(bytes, 0, count);
            }
            inF.close();

            System.out.println("SendToServer");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Unfortunately");
        }
    }
}