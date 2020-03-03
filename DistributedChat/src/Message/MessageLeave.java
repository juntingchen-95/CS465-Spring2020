package Message;

import Chat.ChatNode;

public class MessageLeave extends MessageUtility {

    // nodeInfo recode message sender's successor's information
    private final ChatNode.NodeInfo successorNodeInfo;

    public MessageLeave(String type, ChatNode.NodeInfo nodeInfo, ChatNode.NodeInfo successorNodeInfo) {
        super(type, nodeInfo);
        this.successorNodeInfo = successorNodeInfo;
    }

    public ChatNode.NodeInfo getSuccessorNodeInfo() {
        return successorNodeInfo;
    }
}
