package imperial.drp.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Person(
    var name: String? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null

    override fun toString(): String {
        return "$name"
    }
}
