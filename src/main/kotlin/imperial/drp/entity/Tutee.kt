package imperial.drp.entity

import javax.persistence.Entity

@Entity
class Tutee(name: String? = null) : Person(name) {
}