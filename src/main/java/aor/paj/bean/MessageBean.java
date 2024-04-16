package aor.paj.bean;

import aor.paj.dao.MessageDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.Message;
import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;

import java.time.LocalDateTime;

@Singleton
public class MessageBean {

    @EJB
    private MessageDao messageDao;
    @EJB
    private UserDao userDao;

    public MessageBean() {
    }

    public void createMessage(String content, String sender, String recipient) {

        UserEntity senderEntity = userDao.findUserByUsername(sender);
        UserEntity recipientEntity = userDao.findUserByUsername(recipient);


        MessageEntity message = new MessageEntity();
        message.setSender(senderEntity);
        message.setRecipient(recipientEntity);
        message.setContent(content);
        message.setSentTimestamp(LocalDateTime.now());
        messageDao.persist(message);
    }

    private MessageEntity convertToEntity(Message message) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setId(message.getId());
        messageEntity.setSentTimestamp(message.getSentTimestamp());
        messageEntity.setContent(message.getContent());
        messageEntity.setRead(message.isRead());
        return messageEntity;
    }
}
