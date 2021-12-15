import chatapp.ChatParticipant;
import helpers.ChatParticipantCredentials;

public class AppStart {
    public static void main(String[] args) {
        if (args.length > 5) {
            System.out.println("Error: too many arguments\n");
            System.out.println("Usage examples:");
            System.out.println("YourIP YourPort FriendIP FriendPort YourName");
            System.out.println(" 127.0.1.1 2010");
            System.out.println(" 127.0.1.1 2011 127.0.1.1 2010");
            System.out.println(" 127.0.1.1 2012 127.0.1.1 2011 Hump");
            return;
        }
        String localAddress="127.0.0.1";
        String localPort = "420";
        String remoteAddress=null;
        String remotePort = null;
        String participantName="Participant_1";
        localAddress = (args.length>=1) ? localAddress=args[0] : localAddress;
        localPort = (args.length>=2) ? localPort=args[1] : localPort;
        remoteAddress = (args.length>=3) ? remoteAddress=args[2] : remoteAddress;
        remotePort = (args.length>=4) ? remotePort=args[3] : remotePort;
        participantName = (args.length>=5) ? participantName=args[4] : participantName;
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
