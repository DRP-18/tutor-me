package imperial.drp.dao

import imperial.drp.entity.Person
import imperial.drp.entity.Task
import org.springframework.data.repository.CrudRepository

interface TaskRepository : CrudRepository<Task, Long> {
    fun findByTutee(tutee: Person): List<Task>
    fun findByTutor(tutor: Person): List<Task>
    fun findByTuteeOrderByStartTimeAsc(tutee: Person): List<Task>
    fun findByTutorOrderByStartTimeAsc(tutor: Person): List<Task>
}
