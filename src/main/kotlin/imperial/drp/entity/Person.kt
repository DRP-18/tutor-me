package imperial.drp.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
open class Person(open var name: String? = null, open var status: String = "Hey there!", open var avatar: Int = 1) : Comparable<Person> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open val id: Long? = null

    override fun toString(): String {
        return "$name"
    }

    override fun compareTo(other: Person): Int {
        return this.name!!.compareTo(other.name!!)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Person
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

}
