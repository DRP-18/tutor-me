package imperial.drp.entity

import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
class Message(
        @field:ManyToOne var conversation: Conversation? = null,
        @field:Column(columnDefinition = "TIMESTAMP WITH TIME ZONE") var time: Calendar? = null,
        var message: String? = null
) {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val message_id: Long? = null


}