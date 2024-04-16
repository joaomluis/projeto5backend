package aor.paj.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;
import java.util.Date;

@XmlRootElement
public class Message {

    @XmlElement
    private long id;
    @XmlElement
    private User sender;
    @XmlElement
    private LocalDateTime sentTimestamp;
    @XmlElement
    private User recipient;
    @XmlElement
    private String content;
    @XmlElement
    private boolean isRead;

    public Message (User sender, LocalDateTime sentTimestamp, User recipient, String content) {

        Date idTime=new Date();
        this.id = idTime.getTime();
        this.sender = sender;
        this.sentTimestamp = sentTimestamp;
        this.recipient = recipient;
        this.content = content;
        this.isRead = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getSentTimestamp() {
        return sentTimestamp;
    }

    public void setSentTimestamp(LocalDateTime sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }
}