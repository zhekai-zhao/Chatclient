package chatclient;

import java.io.IOException;

public class ReceiverClient {
    public static void main(String[] args) {
        try {
            
            Client receiverClient = new Client();
            receiverClient.connect("localhost", 6666);

          
            String message = receiverClient.receiveMessage();
            System.out.println("Received message: " + message);

            
            String savePath = "file.txt"; 
            boolean success = receiverClient.receiveFile(savePath);
            if (success) {
                System.out.println("File received and saved.");
            } else {
                System.out.println("File transfer failed.");
            }

            
            receiverClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
