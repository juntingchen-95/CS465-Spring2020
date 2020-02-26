package Message;

import Chat.ChatNode;

public class MessageNormal extends Message {

    private final String content;
    private final ChatNode.NodeInfo nodeInfo;

    public MessageNormal(String type, String content, ChatNode.NodeInfo nodeInfo) {
        super(type);
        this.content = content;
        this.nodeInfo = nodeInfo;
    }

    public String getContent() {
        return content;
    }

    public ChatNode.NodeInfo getNodeInfo() {
        return nodeInfo;
    }
}
