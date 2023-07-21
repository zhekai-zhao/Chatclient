package chatclient;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream inputStream;
    private static final int BUFFER_SIZE = 4096;

    public Receiver(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.socket = serverSocket.accept();
        this.inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void listenForMessages() {
        try {
            while (true) {
                int command = inputStream.readInt();

                switch (command) {
                    case 0:  // Connection established
                        String helloMessage = inputStream.readUTF();
                        System.out.println("Connection message: " + helloMessage);
                        break;

                    case 1:  // This is a message
                        String message = inputStream.readUTF();
                        System.out.println("Received message: " + message);
                        break;
                    
                    case 2:  // This is a file
                        receiveFile();
                        System.out.println("File received.");
                        break;

                    case -1:  // Connection terminated
                        String goodbyeMessage = inputStream.readUTF();
                        System.out.println("Termination message: " + goodbyeMessage);
                        return;  // Exit the loop on receiving termination command

                    default:
                        System.err.println("Unknown command received: " + command);
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error occurred while reading from the socket: " + e.getMessage());
        }
    }


    public void receiveFile() throws IOException {
        String fileName = inputStream.readUTF();
        long fileSize = inputStream.readLong();

        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);

        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytesRead = 0;

        long startTime = System.currentTimeMillis(); // Start

        while (totalBytesRead < fileSize) {
            int bytesRead = inputStream.read(buffer);
            fos.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }

        long endTime = System.currentTimeMillis(); // End

        fos.close();

        long duration = endTime - startTime; // Calculate time
        System.out.println("File received in: " + duration + " milliseconds.");
    }




    public void close() throws IOException {
        inputStream.close();
        socket.close();
        serverSocket.close();
    }

    public static void main(String[] args) {
        try {
            Receiver receiver = new Receiver(1234);
            receiver.listenForMessages();
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
