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
    private String sender;
    @XmlElement
    private LocalDateTime sentTimestamp;
    @XmlElement
    private String recipient;
    @XmlElement
    private String content;
    @XmlElement
    private boolean isRead;

    public Message () {

        Date idTime=new Date();
        this.id = idTime.getTime();
        this.sender = null;
        this.sentTimestamp = null;
        this.content = null;
        this.isRead = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
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
