package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.Message;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import aor.paj.websocket.NotificationsWebsocket;
import aor.paj.websocket.Notifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
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
    @EJB
    Notifier notifier;
    @EJB
    NotificationsWebsocket notificationsWebsocket;

    @Inject
    UserBean userBean;

    public MessageBean() {
    }

    public void handleMessage(Session session, String content, String sender, String recipient) {

        UserEntity senderEntity = userDao.findUserByUsername(sender);
        UserEntity recipientEntity = userDao.findUserByUsername(recipient);

        if (senderEntity == null || recipientEntity == null) {
            System.out.println("Error: sender or recipient not found");
            return;
        }

        userBean.refreshUserToken(senderEntity.getUsername());

        // Verifica se o outro user tmb tem o chat aberto
        String recipientToken = recipient + "-" + sender;
        Session recipientSession = notifier.getSessionByToken(recipientToken);
        boolean isRead = recipientSession != null;

        MessageEntity message = createMessage(senderEntity, recipientEntity, content, isRead);
        Message messageDto = convertToDto(message);

        try  {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String messageJson = mapper.writeValueAsString(messageDto);

            session.getBasicRemote().sendText(messageJson);


            // Se sim manda a mensagem pelo ws
            if (recipientSession != null) {

                recipientSession.getBasicRemote().sendText(messageJson);
            }
        } catch (IOException e) {
            System.out.println("Error sending message back to client: " + e.getMessage());
        }
    }

    public void markMessagesAsRead(String sender, String recipient) {
        List<MessageEntity> messages = messageDao.findMessagesBetweenTwoUsers(sender, recipient);
        for (MessageEntity message : messages) {
            if (!message.isRead() && message.getRecipient().getUsername().equals(recipient)) {
                message.setRead(true);
                message.setNotification(false);
                messageDao.merge(message);
            }
        }
    }

    public List<Message> getUnreadMessages(String recipient) {
        UserEntity recipientEntity = userDao.findUserByUsername(recipient);

        List<MessageEntity> unreadMessages = messageDao.getUnreadMessages(recipientEntity);

        List<Message> unreadMessagesDto = new ArrayList<>();

        for (MessageEntity message : unreadMessages) {
            unreadMessagesDto.add(convertToDto(message));
        }

        return unreadMessagesDto;
    }

    public void markNotificationsAsRead(String recipient) {
        List<MessageEntity> messages = messageDao.findNotificationsByRecipient(recipient);
        for (MessageEntity message : messages) {
            if (message.isNotification() && message.getRecipient().getUsername().equals(recipient)) {
                message.setNotification(false);
                messageDao.merge(message);
            }
        }
    }

    private MessageEntity createMessage (UserEntity sender, UserEntity recipient, String content,boolean isRead) {

        Date idTime = new Date();

        MessageEntity message = new MessageEntity();
        message.setSentTimestamp(LocalDateTime.now());
        message.setContent(content);
        message.setRead(isRead);
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setId(idTime.getTime());
        message.setNotification(true);

        messageDao.persist(message);


        String recipientToken = recipient.getUsername() + "-" + sender.getUsername();
        Session chatRecipientSenderSession = notifier.getSessionByToken(recipientToken);
        Session recipientSession = notificationsWebsocket.getSessionByToken(recipient.getUsername());
        if (recipientSession != null && chatRecipientSenderSession == null) {

            try {
                JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
                JsonObject jsonObject = Json.createObjectBuilder()
                        .add("sender", sender.getUsername())
                        .add("timestamp", message.getSentTimestamp().toString())
                        .add("not_read", message.isNotification())
                        .build();
                jsonArrayBuilder.add(jsonObject);
                JsonArray jsonArray = jsonArrayBuilder.build();

                recipientSession.getBasicRemote().sendText(jsonArray.toString());
            } catch (IOException e) {
                System.out.println("Error sending notification: " + e.getMessage());
            }
        }

        return message;
    }


    private Message convertToDto(MessageEntity messageEntity) {
        Message message = new Message();
        message.setId(messageEntity.getId());
        message.setSender(messageEntity.getSender().getUsername());
        message.setSentTimestamp(messageEntity.getSentTimestamp());
        message.setRecipient(messageEntity.getRecipient().getUsername());
        message.setContent(messageEntity.getContent());
        message.setRead(messageEntity.isRead());
        message.setNotification(messageEntity.isNotification());
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
