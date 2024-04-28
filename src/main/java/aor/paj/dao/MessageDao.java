package aor.paj.dao;

import aor.paj.entity.MessageEntity;
import aor.paj.entity.UserEntity;
import jakarta.ejb.Stateless;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class MessageDao extends AbstractDao<MessageEntity> {

    private static final long serialVersionUID = 1L;

    public MessageDao() {
        super(MessageEntity.class);
    }



    public ArrayList<MessageEntity> findMessagesBetweenTwoUsers(String username1, String username2) {
        try {
            ArrayList<MessageEntity> messageEntities = (ArrayList<MessageEntity>) em.createNamedQuery("Message.findMessagesBetweenTwoUsers")
                    .setParameter("username1", username1)
                    .setParameter("username2", username2)
                    .getResultList();
            return messageEntities;
        } catch (Exception e) {
            return null;
        }
    }

    public List<MessageEntity> getUnreadMessages(UserEntity recipientUsername) {

        String query = "SELECT m FROM MessageEntity m WHERE m.recipient = :recipient AND m.isRead = false ORDER BY m.sentTimestamp DESC";
        return em.createQuery(query, MessageEntity.class)
                .setParameter("recipient", recipientUsername)
                .getResultList();
    }

    public List<MessageEntity> findNotificationsByRecipient(String recipient) {
        String query = "SELECT m FROM MessageEntity m WHERE m.recipient.username = :recipient AND m.notification = true";
        return em.createQuery(query, MessageEntity.class)
                .setParameter("recipient", recipient)
                .getResultList();
    }

    public List<MessageEntity> findMessagesByUser(String username) {
        String query = "SELECT m FROM MessageEntity m WHERE m.sender.username = :username OR m.recipient.username = :username";
        return em.createQuery(query, MessageEntity.class)
                .setParameter("username", username)
                .getResultList();
    }
}
