package aor.paj.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="message")
public class MessageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender", nullable = false, unique = false, updatable = true)
    private UserEntity sender;

    @Column (name = "sent_timestamp", nullable = false, unique = false, updatable = true)
    private LocalDateTime sentTimestamp;

    @ManyToOne
    @JoinColumn(name = "recipient", nullable = false, unique = false, updatable = true)
    private UserEntity recipient;

    @Column(name = "content", nullable = false, unique = false, updatable = true)
    private String content;

    @Column(name = "is_read", nullable = false, unique = false, updatable = true)
    private boolean isRead;

    public MessageEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
    }

    public UserEntity getRecipient() {
        return recipient;
    }

    public void setRecipient(UserEntity recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentTimestamp() {
        return sentTimestamp;
    }

    public void setSentTimestamp(LocalDateTime sentTimestamp) {
        this.sentTimestamp = sentTimestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}