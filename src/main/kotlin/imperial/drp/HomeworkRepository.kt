package imperial.drp

import org.springframework.data.repository.CrudRepository

interface HomeworkRepository : CrudRepository<Homework, Long> {
    fun findByTutee(tutee: String): List<Homework>
}
