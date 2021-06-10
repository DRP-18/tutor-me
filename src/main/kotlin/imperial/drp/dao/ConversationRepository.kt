package imperial.drp.dao

import imperial.drp.entity.Conversation
import org.springframework.data.repository.CrudRepository

interface ConversationRepository : CrudRepository<Conversation, Long> {
    fun findByUserId(id: Long): List<Conversation>
}