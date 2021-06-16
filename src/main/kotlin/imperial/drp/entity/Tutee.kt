package imperial.drp.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Entity
import javax.persistence.ManyToMany

@Entity
class Tutee(name: String? = null,
            @JsonIgnore @field:ManyToMany var tutors: MutableList<Tutor>? = mutableListOf()
) : Person(name) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Tutee) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return tutors?.hashCode() ?: 0
    }
}