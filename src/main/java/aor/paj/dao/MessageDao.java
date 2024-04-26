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

        String query = "SELECT m FROM MessageEntity m WHERE m.recipient = :recipient AND m.isRead = false";
        return em.createQuery(query, MessageEntity.class)
                .setParameter("recipient", recipientUsername)
                .getResultList();
    }
}
