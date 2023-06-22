package chatclient;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;

public class Sender {
    private static final int PACKET_SIZE = 1024;
    private Socket socket;
    private OutputStream outputStream;

    public Sender(Socket socket) throws IOException {
        this.socket = socket;
        this.outputStream = socket.getOutputStream();
    }

    public void sendFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("File does not exist or is not a regular file.");
            return;
        }

        // Send file transfer request
        String fileInfo = "FILE_TRANSFER_REQUEST " + file.getName() + " " + file.length() + "\n"; 
        outputStream.write(fileInfo.getBytes());
        // Send file
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[PACKET_SIZE];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            // Convert the file chunk to a Base64 string and send
            String data = Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, length)) + "\n"; 
            outputStream.write(data.getBytes());
        }

        fis.close();
        outputStream.flush();

        // Add a delay before closing the connection
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Close the connection
        socket.close();
    }

    public void sendMessage(String message) throws IOException {
        String completeMessage = message + "\n"; 
        outputStream.write(completeMessage.getBytes());
        outputStream.flush();
    }
    
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 6667);
            Sender sender = new Sender(socket);
            sender.sendFile("C:\\JavaProjects\\chatclient\\src\\chatclient\\test.zip"); 
            try {
                Thread.sleep(5000);  // Wait for a while before closing the connection.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
