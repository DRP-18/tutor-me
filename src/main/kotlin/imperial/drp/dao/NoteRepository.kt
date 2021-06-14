package imperial.drp.dao


import org.springframework.data.repository.CrudRepository

interface NoteRepository : CrudRepository<String, Long> {
    fun findByContent(content: String): List<Long>

    fun findAllById(id: Long): List<String>
}