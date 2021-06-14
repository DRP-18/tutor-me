package imperial.drp.dao

import imperial.drp.entity.Conversation
import imperial.drp.entity.Message
import org.springframework.data.repository.CrudRepository
import java.util.*

interface NoteRepository : CrudRepository<String, Long> {
    fun findByContent(content: String): List<Long>

    fun findAllById(id: Long): List<String>
}