package imperial.drp.entity

import java.util.*
import javax.persistence.*

@Entity
class File(
    var filename: String? = null,
    @field:ManyToOne var uploader: Person? = null,
    @field:Column(columnDefinition = "TIMESTAMP WITH TIME ZONE") var uploadTime: Calendar? = null,
    @field:Lob var content: ByteArray? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null
}
