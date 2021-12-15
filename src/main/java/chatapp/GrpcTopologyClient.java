package chatapp;

import com.google.protobuf.Empty;
import cz.prespjan.topology_communication.*;
import helpers.ChatParticipantCredentials;
import helpers.Sleeper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.soap.Node;
import java.util.concurrent.TimeUnit;

public class GrpcTopologyClient {

    private ChatParticipantCredentials credentials;
    private static final Logger logger = LogManager.getLogger(ChatParticipant.class);

    public GrpcTopologyClient(ChatParticipantCredentials credentials) {
        this.credentials = credentials;
    }

    public void change_remote_left(ChatParticipantCredentials remoteCredentials, ChatParticipantCredentials newLeft){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                remoteCredentials.getLocalAddress(),
                remoteCredentials.getIntPort()).usePlaintext().build();

        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        TCResponse receivedResponse = stub.setRemoteLeft(newLeft.toNodeIdentifier());
        channel.shutdown();
    }
    public void change_remote_right(ChatParticipantCredentials remoteCredentials, ChatParticipantCredentials newRight){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                remoteCredentials.getLocalAddress(),
                remoteCredentials.getIntPort()).usePlaintext().build();

        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        TCResponse receivedResponse = stub.setRemoteRight(newRight.toNodeIdentifier());
        channel.shutdown();
    }

    public ChatParticipantCredentials getRemoteLeft(ChatParticipantCredentials node) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                node.getLocalAddress(),
                node.getIntPort()).usePlaintext().build();

        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        NodeIdentifier receivedResponse = stub.getRemoteLeft(Empty.getDefaultInstance());
        channel.shutdown();
        return new ChatParticipantCredentials(receivedResponse);
    }

    public ChatParticipantCredentials getRemoteRight(ChatParticipantCredentials node) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                node.getLocalAddress(),
                node.getIntPort()).usePlaintext().build();

        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        NodeIdentifier receivedResponse = stub.getRemoteRight(Empty.getDefaultInstance());
        channel.shutdown();
        return new ChatParticipantCredentials(receivedResponse);
    }


    public void callOnMessage(ChatParticipantCredentials credentials, TopoMessage message) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                credentials.getLocalAddress(),
                credentials.getIntPort()).usePlaintext().build();
        Sleeper.sleep(100);
        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        TCResponse receivedResponse = stub.onMessage(message);
        channel.shutdown();
    }

    public void addNodeToNodesParticipantsList(ChatParticipantCredentials node, NodeIdentifier nodeToAdd) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                node.getLocalAddress(),
                node.getIntPort()).usePlaintext().build();

        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        TCResponse receivedResponse = stub.addToOtherParticipants(nodeToAdd);
        channel.shutdown();
    }
    public boolean pingNode(ChatParticipantCredentials credentials){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                credentials.getLocalAddress(),
                credentials.getIntPort()).usePlaintext().build();

        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        TCResponse receivedResponse = stub.ping(TCMessage.getDefaultInstance());
        channel.shutdown();
        return receivedResponse.getReceived();
    }

    public void callSendMessageToAll(TCMessage request, ChatParticipantCredentials node) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                node.getLocalAddress(),
                node.getIntPort()).usePlaintext().build();
        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        TCResponse receivedResponse = stub.sendMessageToAll(TCMessage.newBuilder()
                .setBody(request.getBody())
                .setSender(request.getSender()).build());
        channel.shutdown();
    }

    public void setTopologyOktoOK(ChatParticipantCredentials cr) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                cr.getLocalAddress(),
                cr.getIntPort()).usePlaintext().build();
        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        TCResponse receivedResponse = stub.setRemoteTopologyOKtoOK(NodeIdentifier.newBuilder()
                .setAddress(cr.getLocalAddress())
                .setPort(cr.getPort()).build());
        channel.shutdown();
    }

    public void startElection(ChatParticipantCredentials node) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                node.getLocalAddress(),
                node.getIntPort()).usePlaintext().build();
        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        TCResponse receivedResponse = stub.startElectionRemote(Empty.getDefaultInstance());
        channel.shutdown();
    }

    public void disconnectRemote(ChatParticipantCredentials node,DisconnectMessage disconnectMessage) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                node.getLocalAddress(),
                node.getIntPort()).usePlaintext().build();
        TopologyCommunicationServiceGrpc.TopologyCommunicationServiceBlockingStub stub = TopologyCommunicationServiceGrpc.newBlockingStub(channel);
        TCResponse receivedResponse = stub.disconnect(disconnectMessage);
        channel.shutdown();
    }
}