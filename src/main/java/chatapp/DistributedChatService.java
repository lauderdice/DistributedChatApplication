package chatapp;

import helpers.ChatParticipantCredentials;

public class DistributedChatService {

    private ChatParticipantCredentials credentials;
    public DistributedChatService(ChatParticipantCredentials credentials) {
        this.credentials = credentials;
    }

    public void startService(){
        GrpcServer server = new GrpcServer(this.credentials);
        Thread serverThread = new Thread(server);
        serverThread.setDaemon(true);
        serverThread.start();
    }

}