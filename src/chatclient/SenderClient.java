package chatclient;

import java.io.IOException;

public class SenderClient {
    public static void main(String[] args) {
        try {
            
            Client senderClient = new Client();
            senderClient.connect("localhost",6666);

            
            senderClient.sendMessage("Hello, world!");

            String filePath = "file.txt"; 
            senderClient.sendFile(filePath);

            
            senderClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
