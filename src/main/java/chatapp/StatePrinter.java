package chatapp;

import helpers.Sleeper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class StatePrinter implements Runnable{

    private GrpcTopologyCommunicationImpl state;
    private static final Logger logger = LogManager.getLogger(ChatParticipant.class);

    public StatePrinter(GrpcTopologyCommunicationImpl state) {
        this.state = state;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        while(true){
            int interval = 15000;
            logger.info("\n--------------------------TopologyInfo (printed once every "+interval/1000+" seconds)--------------------------\n" +
                    "Node with ID[Address:"+this.state.getSelfCredentials().toString()+"], isLeader:["+this.state.isLeader()+"], \n\tLeft["+
                            this.state.getLeft()+"]\n\tRight["+this.state.getRight()+
            "]\n\t"+"OtherParticipants" +Arrays.toString(state.getAllParticipants().toArray())+""+
                    "\n------------------------------------------------------------------------------------------------\n");
            Sleeper.sleep(15000);
        }
    }
}
