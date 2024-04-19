package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.Message;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.websocket.Session;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Singleton
public class MessageBean {

    @EJB
    MessageDao messageDao;
    @EJB
    UserDao userDao;

    @Inject
    UserBean userBean;

    public MessageBean() {
    }

    public void createMessage(Session session, String content, String sender, String recipient) {

        UserEntity senderEntity = userDao.findUserByUsername(sender);
        UserEntity recipientEntity = userDao.findUserByUsername(recipient);

        Date idTime=new Date();

        MessageEntity message = new MessageEntity();
        message.setSentTimestamp(LocalDateTime.now());
        message.setContent(content);
        message.setRead(false);
        message.setSender(senderEntity);
        message.setRecipient(recipientEntity);
        message.setId(idTime.getTime());

        messageDao.persist(message);

        Message messageDto = convertToDto(message);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String messageJson = mapper.writeValueAsString(messageDto);
            System.out.println("Sending message back to client: " + messageJson);
            session.getBasicRemote().sendText(messageJson);
        } catch (IOException e) {
            System.out.println("Error sending message back to client: " + e.getMessage());
        }
    }

    private MessageEntity convertToEntity(Message message) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setId(message.getId());
        messageEntity.setSentTimestamp(message.getSentTimestamp());
        messageEntity.setContent(message.getContent());
        messageEntity.setRead(message.isRead());
        return messageEntity;
    }

    private Message convertToDto(MessageEntity messageEntity) {
        Message message = new Message();
        message.setId(messageEntity.getId());
        message.setSender(messageEntity.getSender().getUsername());
        message.setSentTimestamp(messageEntity.getSentTimestamp());
        message.setRecipient(messageEntity.getRecipient().getUsername());
        message.setContent(messageEntity.getContent());
        message.setRead(messageEntity.isRead());
        return message;
    }

    public ArrayList<Message> getMessages(String sender, String recipient) {
        List<MessageEntity> messageEntities = messageDao.findMessagesBetweenTwoUsers(sender, recipient);

        ArrayList<Message> messages = new ArrayList<>();

        for (MessageEntity messageEntity : messageEntities) {
            Message messageDto = convertToDto(messageEntity);

            messages.add(messageDto);

        }

        return messages;

    }
}
