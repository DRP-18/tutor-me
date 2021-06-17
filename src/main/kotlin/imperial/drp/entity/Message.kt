package imperial.drp.entity

import java.util.*
import javax.persistence.*

@Entity
class Message(
        @field:ManyToOne var conversation: Conversation? = null,
        @field:ManyToOne var sender: Person? = null,
        var message: String? = null,
        @field:Column(columnDefinition = "TIMESTAMP WITH TIME ZONE") var time: Date = Date()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val message_id: Long? = null


}