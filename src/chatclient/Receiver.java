package chatclient;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream inputStream;

    public Receiver(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.socket = serverSocket.accept();
        this.inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void listenForMessages() {
        try {
            while (true) {
                int command = inputStream.readInt();

                if (command == 1) {  // This is a message
                    String message = inputStream.readUTF();
                    System.out.println("Received message: " + message);
                } else if (command == 2) {  // This is a file
                    receiveFile();
                    System.out.println("File received.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error occurred while reading from the socket: " + e.getMessage());
        }
    }

    public void receiveFile() throws IOException {
        String fileName = inputStream.readUTF();
        long fileSize = inputStream.readLong();
        byte[] fileData = new byte[(int) fileSize];
        inputStream.readFully(fileData);

        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(fileData);
        fos.close();
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
