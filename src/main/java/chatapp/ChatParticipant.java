package chatapp;

import helpers.ChatParticipantCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ChatParticipant{

    private DistributedChatService chatService;
    private ChatParticipantCredentials credentials;
    private GrpcChatClient messageClient;
    private static final Logger logger = LogManager.getLogger(ChatParticipant.class);
    public ChatParticipant(ChatParticipantCredentials credentials) {
        this.credentials = credentials;
        this.chatService = new DistributedChatService(credentials);
        this.messageClient = new GrpcChatClient(credentials);
    }

    public void joinChat(){
        this.chatService.startService();
        this.listenForUserInput();
    }
    public void postMessageToChat(String message){
        this.messageClient.sendMessageToOthers(message);
    }
    public void leaveChat(){
        this.messageClient.startChatExit();
    }

    public void listenForUserInput() {
        final BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line;
            try {
                line = br.readLine();
                if ("!exit".equals(line)) {
                    logger.info(credentials.getParticipantName()+" is leaving the chatroom..");
                    leaveChat();
                    System.exit(0);
                }
                if (line != null) {
                    logger.info(credentials.getParticipantName()+" published message to chat: "+line);
                    postMessageToChat(line);
                }
            } catch (IOException e) {
                System.out.println("Exception:(");
            }

        }
    }
}
