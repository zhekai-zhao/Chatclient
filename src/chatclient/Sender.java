package chatclient;

import java.io.*;
import java.net.Socket;

public class Sender {
    private Socket socket;
    private DataOutputStream outputStream;

    public Sender(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void sendFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        
        // Send command 2 to indicate this is a file
        outputStream.writeInt(2);
        outputStream.writeUTF(file.getName());
        outputStream.writeLong(file.length());

        byte[] buffer = new byte[4096];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        fis.close();
        outputStream.flush();
    }

    public void sendMessage(String message) throws IOException {
        // Send command 1 to indicate this is a message
        outputStream.writeInt(1);  
        outputStream.writeUTF(message);
        outputStream.flush();
    }

    public void close() throws IOException {
        outputStream.close();
        socket.close();
    }

    public static void main(String[] args) {
        try {
            Sender sender = new Sender("localhost", 1234);
            sender.sendMessage("Hello, world!");
            sender.sendFile("C:\\JavaProjects\\chatclient\\src\\chatclient\\test.zip");
            sender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
