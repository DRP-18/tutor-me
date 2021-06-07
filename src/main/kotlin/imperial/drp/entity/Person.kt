package imperial.drp.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
open class Person(
    open var name: String? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open val id: Long? = null

    override fun toString(): String {
        return "$name"
    }
}
