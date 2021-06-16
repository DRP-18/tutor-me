package imperial.drp.dao

import imperial.drp.entity.Session
import imperial.drp.entity.Tutor
import org.springframework.data.repository.CrudRepository
import java.util.*

interface SessionRepository : CrudRepository<Session, Long> {
    fun findByTutor(tutor: Tutor): List<Session>
    fun findByTutorAndDateTime(tutor: Tutor, dateTime: Calendar): List<Session>
}