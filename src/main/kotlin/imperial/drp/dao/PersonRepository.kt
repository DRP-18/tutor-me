package imperial.drp.dao

import imperial.drp.entity.Person
import org.springframework.data.repository.CrudRepository

interface PersonRepository: CrudRepository<Person, Long> {
    fun findByName(tutee: String): List<Person>
}
