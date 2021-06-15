package imperial.drp.dao


import imperial.drp.entity.Note
import org.springframework.data.repository.CrudRepository

interface NoteRepository : CrudRepository<Note, Long> {

    fun findByUserId(userId: Long): List<Note>
}