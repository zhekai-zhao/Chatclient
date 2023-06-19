package chatclient;

import java.io.*;
import java.net.Socket;

public class Sender {
    private static final int PACKET_SIZE = 1024;
    private Socket socket;
    private BufferedWriter writer;

    public Sender(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
        byte[] buffer = new byte[PACKET_SIZE];
        int bytesRead;
        while ((bytesRead = bis.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        bis.close();
        fis.close();
    }

    public void sendMessage(String message) throws IOException {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }
}
