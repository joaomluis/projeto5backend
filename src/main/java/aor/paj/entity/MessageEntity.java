package aor.paj.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name="message")
public class MessageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @Column(name = "sender", nullable = false, unique = false, updatable = true)
    private String sender;

    @Column(name = "recipient", nullable = false, unique = false, updatable = true)
    private String recipient;

    @Column(name = "content", nullable = false, unique = false, updatable = true)
    private String content;

    public MessageEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
}