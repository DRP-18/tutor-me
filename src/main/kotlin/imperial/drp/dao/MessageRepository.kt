package imperial.drp.dao

import imperial.drp.entity.Message
import org.springframework.data.repository.CrudRepository

interface MessageRepository : CrudRepository<Message, Long> {
    fun findByConversationId(id: Long): List<Message>
}