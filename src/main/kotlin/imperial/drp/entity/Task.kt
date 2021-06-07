package imperial.drp.entity

import java.util.*
import javax.persistence.*

@Entity
class Task(
    @field:Column(columnDefinition = "TIMESTAMP WITH TIME ZONE") var startTime: Calendar? = null,
    @field:Column(columnDefinition = "TIMESTAMP WITH TIME ZONE") var endTime: Calendar? = null,
    @field:ManyToOne var tutor: Tutor? = null,
    @field:ManyToOne var tutee: Tutee? = null,
    var content: String? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null
}
