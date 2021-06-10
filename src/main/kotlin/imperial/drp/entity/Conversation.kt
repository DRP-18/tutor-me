package imperial.drp.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
class Conversation(

        @JsonIgnore @field:ManyToOne var user1: Person? = null,
        @JsonIgnore @field:ManyToOne var user2: Person? = null,
) {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null
}