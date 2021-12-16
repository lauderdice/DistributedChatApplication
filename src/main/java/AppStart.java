import chatapp.ChatParticipant;
import helpers.ChatParticipantCredentials;

public class AppStart {
    public static void main(String[] args) {
        if (args.length > 5) {
            System.out.println("Error: too many arguments\n");
            System.out.println("Usage examples:");
            System.out.println("YourName YourIP YourPort FriendIP FriendPort");
            System.out.println("Martin 127.0.1.1 2010");
            System.out.println("Martin 127.0.1.1 2011 127.0.1.1 2010");
            return;
        }
        String localAddress="127.0.0.1";
        String localPort = "420";
        String remoteAddress=null;
        String remotePort = null;
        String participantName="Participant_1";
        participantName = (args.length>=1) ? participantName=args[0] : participantName;
        localAddress = (args.length>=2) ? localAddress=args[1] : localAddress;
        localPort = (args.length>=3) ? localPort=args[2] : localPort;
        remoteAddress = (args.length>=4) ? remoteAddress=args[3] : remoteAddress;
        remotePort = (args.length>=5) ? remotePort=args[4] : remotePort;
        ChatParticipantCredentials credentials = new ChatParticipantCredentials(
                localAddress,
                localPort,
                remoteAddress,
                remotePort,
                participantName);
        ChatParticipant participant = new ChatParticipant(credentials);
        participant.joinChat();
    }
}
