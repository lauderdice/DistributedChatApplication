# Distributed Chat Application using Java and gRPC (https://grpc.io)


To start a new ring as the first node, navigate to root directory and start with:

    java -Dfile.encoding=UTF-8 -jar DistributedChatApplicationExampleBuild.jar my_name my_address my_port

To join an existing ring through a friend node:
     
    java -Dfile.encoding=UTF-8 -jar DistributedChatApplicationExampleBuild.jar my_name my_address my_port friend_address friend_port

When testing multiple-node setup locally, the setup could be:

    java -Dfile.encoding=UTF-8 -jar DistributedChatApplicationExampleBuild.jar Node1 127.0.0.1 8888
    
    java -Dfile.encoding=UTF-8 -jar DistributedChatApplicationExampleBuild.jar Node2 127.0.0.1 9999 127.0.0.1 8888
    
##  Description

This application works as a distributed messaging app, where each participant in the chat works both as a separate server and client, meaning that if one server goes down, some othe participant replaces him.

To join the chat, the participant needs to know  the address and port of a node that is already a part of the ring. The topology of the nodes is set up as a ring, where each participant knows the addresses of 2 direct neighbors (left and right) and the Leader node. 

The leader node knows the addresses of 2 direct neighbors (left and right) + he knows the addresses of all chat participants.

To communicate between nodes, the gRPC framework is used along with message and service definitions in Protocol Buffers (found in src/main/proto)

###  Message sending

To send a message, a node first sends it to the leader (if he himself is not already the leader node) and the leader then distributes it to all other participants, which ensures the message ordering.

###  Failover detection

If a message is not sent successfuly, there is probably an issue that some node is not online. In that case, the topology is repaired and the messages are sent again. The leader coordinates this process.

###  Logout

A participant can leave the chat by typing:

    !exit
    
In this case the leader is informed and he initiates the reconnection of the ring in the spot where that particular node goes offline. If the leader goes offline he initiates the election process as well.