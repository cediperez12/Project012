package com.coecs.project012;

import java.util.List;

public class Conversation {
    private String coversationId;
    private List<String> usersId;
    private Message lastMessage;
    private String conversationStatus;
    private List<Message> conversationContent;
    private User.Location location;

    public static final String STATUS_SEEN = "READED";
    public static final String STATUS_SENT = "SENT";

    public static final String MESSAGE_TYPE_VIEW_LOCATION = "VIEW_LOCATION";
    public static final String MESSAGE_TYPE_NORMAL = "NORMAL";

    public Conversation() {
    }

    public Conversation(String coversationId, List<String> usersId) {
        this.coversationId = coversationId;
        this.usersId = usersId;
    }

    public String getCoversationId() {
        return coversationId;
    }

    public void setCoversationId(String coversationId) {
        this.coversationId = coversationId;
    }

    public List<String> getUsersId() {
        return usersId;
    }

    public void setUsersId(List<String> usersId) {
        this.usersId = usersId;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getConversationStatus() {
        return conversationStatus;
    }

    public void setConversationStatus(String conversationStatus) {
        this.conversationStatus = conversationStatus;
    }

    public List<Message> getConversationContent() {
        return conversationContent;
    }

    public void setConversationContent(List<Message> conversationContent) {
        this.conversationContent = conversationContent;
    }

    public class Message{
        private String messageContent;
        private String senderUid;

        private String messageStatus;
        private String messageType;

        private long datetimeSent;
        private User.Location location;

        public Message() {
        }

        public Message(String messageContent, String senderUid, long datetimeSent) {
            this.messageContent = messageContent;
            this.senderUid = senderUid;
            this.datetimeSent = datetimeSent;
        }

        //Sending normal Messages
        public Message(String messageContent, String senderUid, String messageStatus, String messageType, long datetimeSent) {
            this.messageContent = messageContent;
            this.senderUid = senderUid;
            this.messageStatus = messageStatus;
            this.messageType = messageType;
            this.datetimeSent = datetimeSent;
        }

        //Sending View Location Messages
        public Message(String senderUid, long datetimeSent, User.Location userLocation){
            this.senderUid = senderUid;
            this.datetimeSent = datetimeSent;
            location = userLocation;
            messageType = MESSAGE_TYPE_VIEW_LOCATION;
        }

        public String getMessageContent() {
            return messageContent;
        }

        public void setMessageContent(String messageContent) {
            this.messageContent = messageContent;
        }

        public String getSenderUid() {
            return senderUid;
        }

        public void setSenderUid(String senderUid) {
            this.senderUid = senderUid;
        }

        public String getMessageStatus() {
            return messageStatus;
        }

        public void setMessageStatus(String messageStatus) {
            this.messageStatus = messageStatus;
        }

        public String getMessageType() {
            return messageType;
        }

        public void setMessageType(String messageType) {
            this.messageType = messageType;
        }

        public long getDatetimeSent() {
            return datetimeSent;
        }

        public void setDatetimeSent(long datetimeSent) {
            this.datetimeSent = datetimeSent;
        }

        public User.Location getLocation() {
            return location;
        }

        public void setLocation(User.Location location) {
            this.location = location;
        }
    }

}
