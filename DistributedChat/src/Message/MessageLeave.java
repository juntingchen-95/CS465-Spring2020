package Message;

import Chat.ChatNode;

public class MessageLeave extends MessageUtility {

    private final ChatNode.NodeInfo successorNodeInfo;

    public MessageLeave(String type, ChatNode.NodeInfo nodeInfo, ChatNode.NodeInfo successorNodeInfo) {
        super(type, nodeInfo);
        this.successorNodeInfo = successorNodeInfo;
    }

    public ChatNode.NodeInfo getSuccessorNodeInfo() {
        return successorNodeInfo;
    }
}
