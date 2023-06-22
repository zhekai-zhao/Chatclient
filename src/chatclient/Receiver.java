package chatclient;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class Receiver {
    private Socket socket;
    private BufferedReader bufferedReader;

    public Receiver(Socket socket) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void listenForMessages() {
        String message;
        try {
            while ((message = bufferedReader.readLine()) != null) {
                if (message.startsWith("FILE_TRANSFER_REQUEST")) {
                    String[] parts = message.split(" ");
                    String fileName = parts[1];
                    long fileSize = Long.parseLong(parts[2]);
                    receiveFile(fileName, fileSize);
                    System.out.println("File received.");
                    String confirmationMessage = bufferedReader.readLine();
                    if ("FILE_TRANSFER_COMPLETE".equals(confirmationMessage)) {
                        System.out.println("File transfer complete.");
                    }
                } else {
                    System.out.println("Received message: " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("Error occurred while reading from the socket: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error occurred while closing the socket: " + e.getMessage());
            }
        }
    }

    public void receiveFile(String fileName, long fileSize) throws IOException {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);

        long totalRead = 0;
        long remaining = fileSize;

        String line;
        while(remaining > 0 && (line = bufferedReader.readLine()) != null) {
            if ("FILE_TRANSFER_COMPLETE".equals(line)) {
                System.out.println("File transfer complete.");
                break;
            }
            byte[] decodedBytes = Base64.getDecoder().decode(line);
            int decodedLength = decodedBytes.length;
            totalRead += decodedLength;
            remaining -= decodedLength;
            System.out.println("read " + totalRead + " bytes.");
            fos.write(decodedBytes);
        }

        fos.close();
    }

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 6667);
            Receiver receiver = new Receiver(socket);
            receiver.listenForMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
