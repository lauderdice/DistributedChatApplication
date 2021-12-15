package chatapp;

import helpers.ChatParticipantCredentials;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class GrpcServer implements Runnable {

    private final ChatParticipantCredentials credentials;
    private static final Logger logger = LogManager.getLogger(ChatParticipant.class);
    public GrpcServer(ChatParticipantCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public void run() {
        Server server = ServerBuilder
                .forPort(this.credentials.getIntPort())
                .addService(new GrpcChatServiceImpl(this.credentials))
                .addService(new GrpcTopologyCommunicationImpl(this.credentials)).build();

        try {
            server.start();
            logger.warn("GRPC Server was started..");
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("Address "+this.credentials.getLocalAddress()+":"+this.credentials.getPort()+
                    " probably already in use, choose a different one..");
            System.exit(0);
        }
        try {
            server.awaitTermination();
            logger.warn("GRPC Server was terminated..");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
