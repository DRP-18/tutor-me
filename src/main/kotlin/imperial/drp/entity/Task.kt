package imperial.drp.entity

import java.text.DateFormat
import java.text.SimpleDateFormat
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

//TODO: Use @JsonProperty
fun toJsonString(t: Task): String {
    return """{"start_time": "${t.startTime!!.time}","end_time": "${t.endTime!!.time}","content": "${t.content}", "id": ${t.id}}"""
}
