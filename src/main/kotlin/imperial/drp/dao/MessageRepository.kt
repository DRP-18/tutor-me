package imperial.drp.dao

import imperial.drp.entity.Conversation
import imperial.drp.entity.Message
import org.springframework.data.repository.CrudRepository

interface MessageRepository : CrudRepository<Message, Long> {
    fun findByConversation(conversation: Conversation): List<Message>

    fun findByConversationOrderByTimeAsc(conversation: Conversation): List<Message>
}