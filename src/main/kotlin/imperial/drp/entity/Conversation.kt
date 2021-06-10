package imperial.drp.entity

import javax.persistence.*

@Entity
class Conversation(
        @field:ManyToOne var user1: Person? = null,
        @field:ManyToOne var user2: Person? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null
}