package imperial.drp.dao

import imperial.drp.entity.Session
import imperial.drp.entity.Tutor
import org.springframework.data.repository.CrudRepository
import java.util.*

interface SessionRepository : CrudRepository<Session, Long> {
    fun findByTutor(tutor: Tutor): List<Session>
    fun findByTutorAndStartTime(tutor: Tutor, startTime: Calendar): List<Session>
    fun findByTutorAndStartTimeAndEndTime(tutor: Tutor, startTime: Calendar, endTime: Calendar): List<Session>

}