package chatapp;

import cz.prespjan.topology_communication.ChatMessage;
import cz.prespjan.topology_communication.ChatMessageReceivedResponse;
import cz.prespjan.topology_communication.ChatServiceGrpc;
import cz.prespjan.topology_communication.TCResponse;
import helpers.ChatParticipantCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class GrpcChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    private final ChatParticipantCredentials credentials;
    private static final Logger logger = LogManager.getLogger(ChatParticipant.class);

    public GrpcChatServiceImpl(ChatParticipantCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public void receiveChatMessage(ChatMessage request, StreamObserver<ChatMessageReceivedResponse> responseObserver) {
        logger.info("Received message (logical time " + request.getMessageCounter()+ "): "+request.getBody());
        responseObserver.onNext(ChatMessageReceivedResponse.newBuilder().setReceived(true).build());
        responseObserver.onCompleted();
    }
}
