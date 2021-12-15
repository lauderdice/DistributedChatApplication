package chatapp;

import com.google.protobuf.Empty;
import cz.prespjan.topology_communication.ChatMessage;
import cz.prespjan.topology_communication.ChatMessageReceivedResponse;
import cz.prespjan.topology_communication.ChatServiceGrpc;
import cz.prespjan.topology_communication.*;
import helpers.ChatParticipantCredentials;
import helpers.Sleeper;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GrpcTopologyCommunicationImpl extends TopologyCommunicationServiceGrpc.TopologyCommunicationServiceImplBase {


    private static final Logger logger = LogManager.getLogger(ChatParticipant.class);
    private List<ChatParticipantCredentials> allParticipants;
    private final ChatParticipantCredentials selfCredentials;
    private boolean isLeader = false;
    private boolean topologyOK = false;
    private ChatParticipantCredentials leader;
    private ChatParticipantCredentials left = null;
    private ChatParticipantCredentials right = null;
    private final GrpcTopologyClient client;
    private int messageCounter = 0;
    private final StatePrinter topologyStatePrinter;
    private final UUID guid;

    public GrpcTopologyCommunicationImpl(ChatParticipantCredentials credentials) {
        this.selfCredentials = credentials;
        this.guid = credentials.getGuid();
        this.client = new GrpcTopologyClient(credentials);
        this.allParticipants = new ArrayList<>();
        this.topologyStatePrinter = new StatePrinter(this);
        Thread th = new Thread(this.topologyStatePrinter);
        th.setDaemon(true);
        th.start();
        this.connectToRing(selfCredentials);

    }

    private void connectToRing(ChatParticipantCredentials selfCredentials) {
        if (selfCredentials.connectingToMyself()) {
            this.connectToMyself(selfCredentials);
        } else {
            try{
                logger.info("Joining the ring");
                ChatParticipantCredentials connectingTo = new ChatParticipantCredentials(
                        this.selfCredentials.getRemoteAddress(),
                        this.selfCredentials.getRemotePort());
                ChatParticipantCredentials leftFromConnectingTo = client.getRemoteLeft(connectingTo);
                ChatParticipantCredentials me = new ChatParticipantCredentials(
                        this.selfCredentials.getLocalAddress(),
                        this.selfCredentials.getPort());
                client.change_remote_left(connectingTo, me);
                client.change_remote_right(leftFromConnectingTo, me);
                this.setRight(connectingTo);
                this.setLeft(leftFromConnectingTo);
                this.startElection();
            } catch (io.grpc.StatusRuntimeException e){
                logger.info("It was not possible to connect to the remote node. Creating a separate ring with myself as the leader");
                this.connectToMyself(selfCredentials);
            }

        }
    }

    private void connectToMyself(ChatParticipantCredentials selfCredentials) {
        logger.info("Connecting to myself - I am the first in the ring..");
        ChatParticipantCredentials left_right = new ChatParticipantCredentials(selfCredentials.getLocalAddress(),
                selfCredentials.getPort());
        this.setLeft(left_right);
        this.setRight(left_right);
        this.isLeader = true;
        this.allParticipants.add(selfCredentials);
        this.setTopologyOK(true);
    }

    private void startElection() {
        logger.info("Starting Leader Election");
        TopoMessage topoMessage = TopoMessage.newBuilder()
                .setMessageType(TopoMessageType.ELECTION)
                .setGuid(this.guid.toString()).build();
        this.sendRight(topoMessage);
    }

    @Override
    public void setRemoteLeft(NodeIdentifier request, StreamObserver<TCResponse> responseObserver) {
        ChatParticipantCredentials left = new ChatParticipantCredentials(request.getAddress(), request.getPort());
        this.setLeft(left);
        responseObserver.onNext(TCResponse.newBuilder().setReceived(true).build());
        responseObserver.onCompleted();
    }

    public List<ChatParticipantCredentials> getAllParticipants() {
        return allParticipants;
    }

    public ChatParticipantCredentials getSelfCredentials() {
        return selfCredentials;
    }

    public boolean isLeader() {
        return isLeader;
    }

    private void setLeader(ChatParticipantCredentials leader) {
        this.leader = leader;
    }

    public ChatParticipantCredentials getLeft() {
        return left;
    }

    public void setLeft(ChatParticipantCredentials credentials) {
        this.left = credentials;
    }

    public ChatParticipantCredentials getRight() {
        return right;
    }

    public void setRight(ChatParticipantCredentials credentials) {
        this.right = credentials;
    }

    public UUID getGuid() {
        return guid;
    }

    @Override
    public void setRemoteRight(NodeIdentifier request, StreamObserver<TCResponse> responseObserver) {
        ChatParticipantCredentials right = new ChatParticipantCredentials(request.getAddress(), request.getPort());
        this.setRight(right);
        responseObserver.onNext(TCResponse.newBuilder().setReceived(true).build());
        responseObserver.onCompleted();
    }


    public void setTopologyOK(boolean ok) {
        this.topologyOK = ok;
    }

    @Override
    public void setRemoteTopologyOKtoOK(NodeIdentifier request, StreamObserver<TCResponse> responseObserver) {
        this.topologyOK = true;
        responseObserver.onNext(TCResponse.newBuilder().setReceived(true).build());
        responseObserver.onCompleted();
    }




    @Override
    public void getRemoteLeft(Empty request, StreamObserver<NodeIdentifier> responseObserver) {
        responseObserver.onNext(this.left.toNodeIdentifier());
        responseObserver.onCompleted();
    }


    @Override
    public void getRemoteRight(Empty request, StreamObserver<NodeIdentifier> responseObserver) {
        responseObserver.onNext(this.right.toNodeIdentifier());
        responseObserver.onCompleted();
    }

    public void sendRight(TopoMessage message) {
        client.callOnMessage(this.right, message);
    }


    public void sendLeft(TopoMessage message) {
        client.callOnMessage(this.left, message);
    }


    @Override
    public void onMessage(TopoMessage request, StreamObserver<TCResponse> responseObserver) {
        responseObserver.onNext(TCResponse.newBuilder().setReceived(true).build());
        switch (request.getMessageType()) {
            case ELECTION:
                logger.info("Received ELECTION message..");
                this.allParticipants.clear();
                this.isLeader = false;
                TopoMessage newMessage;
                if (request.getGuid().equals(this.guid.toString())) {
                    logger.info("The ELECTION message has circled around and I am elected! Sending ELECTED message..");
                    newMessage = TopoMessage.newBuilder()
                            .setMessageType(TopoMessageType.ELECTED)
                            .setNode(this.selfCredentials.toNodeIdentifier())
                            .setGuid(this.guid.toString()).build();
                } else {
                    int currentHighest = request.getGuid().hashCode();
                    int myHash = this.guid.toString().hashCode();

                    if (myHash > currentHighest) {
                        newMessage = TopoMessage.newBuilder()
                                .setMessageType(TopoMessageType.ELECTION)
                                .setGuid(this.getGuid().toString()).build();
                    } else {
                        newMessage = request;
                    }
                    logger.info("Passing ELECTION message to the right..");
                }
                responseObserver.onCompleted();
                this.sendRight(newMessage);
                break;
            case ELECTED:
                if (request.getGuid().equals(this.guid.toString())) {
                    logger.info("The ELECTED message has circled, everyone knows I am the leader..");
                    synchronized (this) {
                        this.isLeader = true;
                        this.allParticipants.add(selfCredentials);
                        for (ChatParticipantCredentials cr :
                                allParticipants) {
                            client.setTopologyOktoOK(cr);
                        }
                    }
                    responseObserver.onCompleted();
                    break;
                } else {
                    this.leader = new ChatParticipantCredentials(request.getNode().getAddress(), request.getNode().getPort());
                    client.addNodeToNodesParticipantsList(this.leader, this.selfCredentials.toNodeIdentifier());
                    logger.info("Passing ELECTED message to the right and informing leader about myself..");
                    responseObserver.onCompleted();
                    this.sendRight(request);
                }
                break;
            case CHECK_RIGHT:
                try {
                    responseObserver.onCompleted();
                    this.sendRight(request);
                } catch (Exception e) {
                    logger.info("CHECK_RIGHT call was not successful, this is the place where the ring is broken, going back..");
                    newMessage = TopoMessage.newBuilder()
                            .setNode(selfCredentials.toNodeIdentifier())
                            .setMessageType(TopoMessageType.CHECK_LEFT).build();
                    try {
                        sendLeft(newMessage);
                    } catch (Exception e2) {
                        logger.info("Immediate CHECK_LEFT was not successful as well -> I am alone in the ring, assigning myself as the leader..");
                        synchronized (this) {
                            this.isLeader = true;
                            this.leader = null;
                            this.allParticipants.clear();
                            this.allParticipants.add(selfCredentials);
                            this.setLeft(new ChatParticipantCredentials(selfCredentials.getLocalAddress(), selfCredentials.getPort()));
                            this.setRight(new ChatParticipantCredentials(selfCredentials.getLocalAddress(), selfCredentials.getPort()));
                        }
                    }
                }
                break;
            case CHECK_LEFT:
                try {
                    responseObserver.onCompleted();
                    sendLeft(request);
                } catch (Exception e) {
                    logger.info("CHECK_LEFT was not successful. Repairing the ring with known information..");
                    try {
                        ChatParticipantCredentials otherEnd = new ChatParticipantCredentials(request.getNode().getAddress(),
                                request.getNode().getPort());
                        client.change_remote_right(otherEnd, selfCredentials);
                        synchronized (this) {
                            this.left = otherEnd;
                        }
                        this.startElection();
                    } catch (Exception e2) {
                        logger.info("Unexpected problem in repairing the ring, trying again...");
                        this.repairTopology();
                    }
                    break;
                }
        }


    }


    public void repairTopology() {
        TopoMessage topoMessage = TopoMessage.newBuilder()
                .setMessageType(TopoMessageType.CHECK_RIGHT)
                .setNode(selfCredentials.toNodeIdentifier())
                .build();
        client.callOnMessage(selfCredentials, topoMessage);
    }


    @Override
    public void addToOtherParticipants(NodeIdentifier request, StreamObserver<TCResponse> responseObserver) {
        responseObserver.onNext(TCResponse.newBuilder().setReceived(true).build());
        synchronized (this) {
            this.allParticipants.add(new ChatParticipantCredentials(request.getAddress(), request.getPort()));

        }
        responseObserver.onCompleted();
    }


    @Override
    public void ping(TCMessage request, StreamObserver<TCResponse> responseObserver) {
        responseObserver.onNext(TCResponse.newBuilder().setReceived(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void sendMessageToAll(TCMessage request, StreamObserver<TCResponse> responseObserver) {
        while (!this.topologyOK) {
            Sleeper.sleep(500);
            logger.info("Waiting until topology will be completely connected before proceeding with the message..");
        }
        int attempts = 0;
        int maxAttempts = 2;
        while (attempts < maxAttempts) {
            try {
                attempts++;
                if (this.isLeader) {
                    this.distributeMessageToAll(request.getBody(), request.getSender());
                    this.messageCounter++;
                } else {
                    client.pingNode(leader);
                    client.callSendMessageToAll(request, leader);
                }
                responseObserver.onNext(TCResponse.newBuilder().setReceived(true).build());
                responseObserver.onCompleted();
                break;
            } catch (Exception e) {
                logger.info("Failed to send the message, the topology will be repaired..");
                this.setTopologyOK(false);
                this.repairTopology();
                while (!this.topologyOK) {
                    Sleeper.sleep(500);
                    logger.info("Waiting until topology will be completely connected before proceeding with new attempt..");
                }
            }
        }
    }


    @Override
    public void startElectionRemote(Empty request, StreamObserver<TCResponse> responseObserver) {
        responseObserver.onNext(TCResponse.newBuilder().setReceived(true).build());
        responseObserver.onCompleted();
        this.startElection();
    }

    @Override
    public void disconnect(DisconnectMessage request, StreamObserver<TCResponse> responseObserver) {
        try {
            if (this.isLeader()) {
                ArrayList<ChatParticipantCredentials> new_participants = new ArrayList<>();
                synchronized (this) {
                    for (ChatParticipantCredentials cr :
                            allParticipants) {
                        if (!(request.getNode().getAddress().equals(cr.getLocalAddress()) && request.getNode().getPort().equals(cr.getPort()))) {
                            new_participants.add(cr);
                        }
                    }
                    allParticipants = new_participants;
                }
                ChatParticipantCredentials toDelete = new ChatParticipantCredentials(request.getNode().getAddress(), request.getNode().getPort());
                ChatParticipantCredentials rightFromToDelete = client.getRemoteRight(toDelete);
                ChatParticipantCredentials leftFromToDelete = client.getRemoteLeft(toDelete);
                client.change_remote_right(leftFromToDelete, rightFromToDelete);
                client.change_remote_left(rightFromToDelete, leftFromToDelete);
                if (request.getVoteLeader() == true) {
                    client.startElection(this.right);
                }
                responseObserver.onNext(TCResponse.newBuilder().setReceived(true).build());
                responseObserver.onCompleted();
            } else {
                DisconnectMessage disconnectMessage = DisconnectMessage.newBuilder()
                        .setNode(request.getNode())
                        .setVoteLeader(false).build();
                client.disconnectRemote(leader, disconnectMessage);
                responseObserver.onNext(TCResponse.newBuilder().setReceived(true).build());
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            e.printStackTrace();
            repairTopology();
        }
    }

    public void distributeMessageToAll(String message, NodeIdentifier nodeIdentifier) {
        for (ChatParticipantCredentials cr :
                allParticipants) {
            boolean response = client.pingNode(cr);
            if (!response) {
                throw new RuntimeException();
            }
        }
        for (ChatParticipantCredentials cr :
                allParticipants) {
            if (!(nodeIdentifier.getAddress().equals(cr.getLocalAddress()) && nodeIdentifier.getPort().equals(cr.getPort()))) {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(
                        cr.getLocalAddress(),
                        cr.getIntPort()).usePlaintext().build();
                ChatServiceGrpc.ChatServiceBlockingStub stub = ChatServiceGrpc.newBlockingStub(channel);
                ChatMessageReceivedResponse receivedResponse = stub.receiveChatMessage(ChatMessage.newBuilder()
                        .setBody(message)
                        .setMessageCounter(this.messageCounter).build());
                channel.shutdown();
            }
        }
    }
}
