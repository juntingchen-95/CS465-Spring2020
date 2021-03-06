package Message;

import Chat.ChatNode;

public class MessageUtility extends Message {

    // nodeInfo recode message sender's information
    private final ChatNode.NodeInfo nodeInfo;

    public MessageUtility(String type, ChatNode.NodeInfo nodeInfo) {
        super(type);
        this.nodeInfo = nodeInfo;
    }

    public ChatNode.NodeInfo getNodeInfo() {
        return nodeInfo;
    }
}
