package chatapp;

import cz.prespjan.topology_communication.*;
import helpers.ChatParticipantCredentials;
import helpers.Sleeper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcChatClient {

    private ChatParticipantCredentials credentials;
    public GrpcChatClient(ChatParticipantCredentials credentials) {
        this.credentials = credentials;
    }

    public void sendMessageToOthers(String message){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                this.credentials.getLocalAddress(),
                Integer.parseInt(this.credentials.getPort())).usePlaintext().build();

        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        TCResponse receivedResponse = stub.sendMessageToAll(TCMessage.newBuilder()
                .setBody(message)
                .setSender(credentials.toNodeIdentifier())
                .build());
        channel.shutdown();
    }

    public void startChatExit() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                this.credentials.getLocalAddress(),
                this.credentials.getIntPort()).usePlaintext().build();

        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        TCResponse receivedResponse = stub.disconnect(DisconnectMessage.newBuilder()
                .setVoteLeader(true)
                .setNode(this.credentials.toNodeIdentifier()).build());
        Sleeper.sleep(1000);
        channel.shutdown();
    }
}