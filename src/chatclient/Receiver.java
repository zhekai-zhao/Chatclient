package chatclient;

import java.io.*;
import java.net.Socket;

public class Receiver {
    private Socket socket;
    private BufferedReader reader;

    public Receiver(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

        System.out.println("File received: " + file.getName());
    }
}
