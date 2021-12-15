package helpers;

import cz.prespjan.topology_communication.NodeIdentifier;

import java.util.Objects;
import java.util.UUID;

public class ChatParticipantCredentials {

    private String localAddress;
    private String port;
    private String remoteAddress = null;
    private String participantName = null;
    private String remotePort = null;
    private UUID guid = null;
    public ChatParticipantCredentials(String localAddress, String port, String remoteAddress,
                                      String remotePort, String participantName) {
        // Connecting to someone

        this.localAddress = localAddress;
        this.port = port;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.participantName = participantName;
        this.guid = UUID.randomUUID();
        if (this.remoteAddress == null){
            this.remoteAddress = this.localAddress;
        }
        if (this.remotePort == null){
            this.remotePort = this.port;
        }
    }

    public ChatParticipantCredentials(String localAddress, String port) {
        // Creating foreign profile
        this.localAddress = localAddress;
        this.port = port;
    }

    @Override
    public String toString() {
        return
                "{Address='" + localAddress + '\'' +
                ", Port='" + port + '\'' +
                '}';
    }

    public ChatParticipantCredentials(NodeIdentifier nodeIdentifier) {
        // Creating foreign profile
        this.localAddress = nodeIdentifier.getAddress();
        this.port = nodeIdentifier.getPort();
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public String getPort() {
        return port;
    }

    public NodeIdentifier toNodeIdentifier(){
        NodeIdentifier nodeIdentifier = NodeIdentifier.newBuilder().setAddress(this.localAddress).setPort(this.port).build();
        return nodeIdentifier;
    }

    public boolean connectingToMyself(){
        if (this.remotePort.equals(this.port) && this.remoteAddress.equals(this.localAddress)){
            return true;
        }
        return false;
    }
    public String getRemotePort(){
        return this.remotePort;
    }
    public void setPort(String port) {
        this.port = port;
    }
    public int getIntPort(){
        return Integer.parseInt(this.port);
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public UUID getGuid() {
        return this.guid;
    }
}
