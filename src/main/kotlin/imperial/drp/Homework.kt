package imperial.drp

import java.util.*
import javax.persistence.*

@Entity
class Homework(
    @field:Column(columnDefinition = "TIMESTAMP WITH TIME ZONE") var due: Calendar? = null,
    var tutor: String? = null,
    var tutee: String? = null,
    var content: String? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null
}
