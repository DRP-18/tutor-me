package imperial.drp.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Entity
import javax.persistence.JoinTable
import javax.persistence.ManyToMany

@Entity
class Tutor(
        name: String? = null,
        @JsonIgnore @field:ManyToMany var tutees: MutableList<Tutee>? = mutableListOf()
) : Person(name) {
}
