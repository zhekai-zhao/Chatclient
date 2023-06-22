
package chatclient;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class Receiver {
    private Socket socket;
    private InputStream inputStream;

    public Receiver(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
    }

    public void listenForMessages() {
        String message;
        try {
            while ((message = readLine()) != null) {
                if (message.startsWith("FILE_TRANSFER_REQUEST")) {
                    String[] parts = message.split(" ");
                    String fileName = parts[1];
                    long fileSize = Long.parseLong(parts[2]);
                    receiveFile(fileName, fileSize);
                    System.out.println("File received.");
                    String confirmationMessage = readLine();
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
    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        String endOfChunk = "END_OF_CHUNK";
        int endIndex = 0;
        while ((ch = inputStream.read()) != -1) {
            sb.append((char) ch);
            if ((char) ch == endOfChunk.charAt(endIndex)) {
                endIndex++;
                if (endIndex == endOfChunk.length()) {
                    // Found the end of chunk, remove it from the result and return
                    sb.setLength(sb.length() - endIndex);
                    break;
                }
            } else {
                endIndex = 0;
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    public void receiveFile(String fileName, long fileSize) throws IOException {
        File file = new File(fileName);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        long totalRead = 0;
        long remaining = fileSize;

        String line;
        while(remaining > 0 && (line = reader.readLine()) != null) {
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
            Socket socket = new Socket("localhost", 6665);
            Receiver receiver = new Receiver(socket);
            receiver.listenForMessages(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
