package imperial.drp.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Entity
import javax.persistence.ManyToMany

@Entity
class Tutee(name: String? = null,
            @JsonIgnore @field:ManyToMany var tutors: MutableList<Tutor>? = mutableListOf()
) : Person(name) {
}