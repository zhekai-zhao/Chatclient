
package chatclient;

import java.io.*;
import java.net.Socket;


public class Client {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;

    public void connect(String serverIP, int serverPort) throws IOException {
        socket = new Socket(serverIP, serverPort);
        socket.setSoTimeout(5000);  // Set a timeout of 5 seconds
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
            if (message.startsWith("FILE_AVAILABLE")) {
                String[] parts = message.split(" ");
                String fileName = parts[1];
                long fileSize = Long.parseLong(parts[2]);
                receiveFile(fileName, fileSize);
                System.out.println("File received.");
            } else if (message.equals("ERROR_FILE_NOT_FOUND")) {
                System.out.println("File not found.");
            } else if (message.equals("ERROR_FILE_TRANSFER")) {
                System.out.println("File transfer error.");
            } else {
                System.out.println("Received message: " + message);
            }
        }
    }

 
    public void receiveFile(String fileName, long fileSize) throws IOException {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        byte[] buffer = new byte[1024];
        int bytesRead;
        long totalBytesRead = 0;
        InputStream is = socket.getInputStream();
        while (totalBytesRead < fileSize && (bytesRead = is.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
            bos.flush();
        }

        bos.close();
        fos.close();

        sendMessage("File received: " + file.getName());
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
        client.connect("localhost", 6666);

        // Send file
        client.sendFile("/path/to/file.txt");
    }

}
