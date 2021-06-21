package imperial.drp.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinTable
import javax.persistence.ManyToMany

@Entity
class Tutor(
        name: String? = null,
        @JsonIgnore @field:ManyToMany(fetch = FetchType.EAGER) var tutees: MutableList<Tutee>? = mutableListOf()
) : Person(name) {
}
