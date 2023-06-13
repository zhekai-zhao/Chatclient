package chatclient;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final int PACKET_SIZE = 1024;
    private static final int MAX_RETRY_COUNT = 3;
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;

    public void connect(String serverIP, int serverPort) throws IOException {
        socket = new Socket(serverIP, serverPort);
        System.out.println("Connected to the server.");
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void disconnect() throws IOException {
        socket.close();
        System.out.println("Connection closed.");
    }

    public void sendFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("File does not exist or is not a regular file.");
            return;
        }

        // Send file transfer request
        writer.write("FILE_TRANSFER_REQUEST " + filePath);
        writer.newLine();
        writer.flush();

        // Send file
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        OutputStream os = socket.getOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = bis.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        bis.close();
        fis.close();

        // Receive transfer result
        String result = reader.readLine();
        if (result != null && result.equals("SUCCESS")) {
            System.out.println("File transfer completed.");
        } else {
            System.out.println("File transfer failed.");
        }
    }

    public void listenForMessages() throws IOException {
        String message;
        while ((message = reader.readLine()) != null) {
            if (message.equals("SUCCESS")) {
                System.out.println("File transfer completed.");
            } else if (message.equals("ERROR_FILE_NOT_FOUND")) {
                System.out.println("File not found.");
            } else if (message.equals("ERROR_FILE_TRANSFER")) {
                System.out.println("File transfer error.");
            }
        }
    }

    public boolean receiveFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());

        byte[] buffer = new byte[PACKET_SIZE];
        int bytesRead;
        int retryCount = 0;

        while ((bytesRead = bis.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
            fos.flush();

            if (!receiveConfirmation()) {
                retryCount++;
                if (retryCount > MAX_RETRY_COUNT) {
                    System.out.println("Exceeded the maximum retry count. File transfer failed.");
                    fos.close();
                    bis.close();
                    file.delete();
                    return false;
                }
                System.out.println("Failed to receive confirmation. Retrying...");
                continue;
            }

            retryCount = 0;
        }

        fos.close();
        bis.close();

        if (retryCount == 0) {
            System.out.println("File received and saved");
            return true;
        } else {
            file.delete();
            return false;
        }
    }
    
    private boolean receiveConfirmation() throws IOException {
        String confirmation = reader.readLine();
        return confirmation != null && confirmation.equals("SUCCESS");
    }

    
    public void sendMessage(String message) throws IOException {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }
    
    public String receiveMessage() throws IOException {
        return reader.readLine();
    }


    public static void main(String[] args) throws IOException {
        // Create and start client
        Client client = new Client();
        client.connect("localhost", 7777);

        // Send file
        client.sendFile("/path/to/file.txt");
    }
}
