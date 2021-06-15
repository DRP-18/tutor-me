package imperial.drp.dao

import imperial.drp.entity.Session
import org.springframework.data.repository.CrudRepository

interface SessionRepository : CrudRepository<Session, Long> {
}