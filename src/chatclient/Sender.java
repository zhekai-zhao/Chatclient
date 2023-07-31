package chatclient;

import java.io.*;
import java.net.Socket;

public class Sender {
    private Socket socket;
    private DataOutputStream outputStream;
    private static final int BUFFER_SIZE = 4096;


    public Sender(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
        this.outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }
    
    public void openConnection() throws IOException {
        // Send command 0 to indicate the start of a connection
        outputStream.writeInt(0);
        outputStream.writeUTF("Hello from Sender!");
        outputStream.flush();
    }

    public void closeConnection() throws IOException {
        // Send command -1 to indicate the end of a connection
        outputStream.writeInt(-1);
        outputStream.writeUTF("Goodbye from Sender!");
        outputStream.flush();
    }

    public void sendFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);

        // Send command 2 to indicate this is a file
        outputStream.writeInt(2);
        outputStream.writeUTF(file.getName());
        outputStream.writeLong(file.length());

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        long startTime = System.currentTimeMillis(); // Start!

        while ((bytesRead = fis.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        long endTime = System.currentTimeMillis(); // End!

        fis.close();
        outputStream.flush();

        long duration = endTime - startTime; // Calculate time
        System.out.println("File sent in: " + duration + " milliseconds.");
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
            sender.openConnection();
            sender.sendMessage("Hello!");
            sender.sendFile("C:\\JavaProjects\\chatclient\\src\\chatclient\\test.zip");
            sender.closeConnection();
            sender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
