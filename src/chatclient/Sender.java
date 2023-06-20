
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

    public void sendFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("File does not exist or is not a regular file.");
            return;
        }

        try {
            // Send file transfer request
            writer.write("FILE_TRANSFER_REQUEST " + file.getName()); // Only send the file name, not the path
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
                os.flush();
                System.out.println("Sent " + bytesRead + " bytes to server.");
            }

            // After closing the input streams and ensuring all data has been written to the output stream,
            // inform the server that the file transfer is complete
            writer.write("FILE_TRANSFER_COMPLETE");
            writer.newLine();
            writer.flush();
            
            // Close input streams
            bis.close();
            fis.close();

            // Shutdown output stream of the socket
            socket.shutdownOutput();
            
        } catch (IOException e) {
            System.err.println("Error occurred while sending file: " + e.getMessage());
        }
    }




    public void sendMessage(String message) throws IOException {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 9999);
            Sender sender = new Sender(socket);
            sender.sendFile("C:\\JavaProjects\\chatclient\\src\\chatclient\\test.txt"); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
