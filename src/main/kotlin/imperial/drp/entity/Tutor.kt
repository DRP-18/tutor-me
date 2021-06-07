package imperial.drp.entity

import javax.persistence.Entity
import javax.persistence.ManyToMany

@Entity
class Tutor(
    name: String? = null,
    @field:ManyToMany var tutees: List<Tutee>? = null
) : Person(name) {
}
