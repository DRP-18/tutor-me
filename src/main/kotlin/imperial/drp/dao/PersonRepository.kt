package imperial.drp.dao

import imperial.drp.entity.Person
import imperial.drp.entity.Tutee
import imperial.drp.entity.Tutor
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

interface PersonRepository : CrudRepository<Person, Long> {
    fun findByName(tutee: String): List<Person>
}

