package imperial.drp.entity

import java.time.LocalTime
import java.util.*
import javax.persistence.*

@Entity
class Session(
        @field:ManyToOne var tutor: Tutor? = null,
        @field:Column(columnDefinition = "TIMESTAMP WITH TIME ZONE") var dateTime: Calendar? = null,
        @field:Column(columnDefinition = "TIME") var duration: LocalTime? = null,
        @field:ManyToMany var tutees: MutableList<Tutee> = mutableListOf()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Session

        if (tutor?.id != other.tutor?.id) return false
        if (dateTime != other.dateTime) return false
        if (duration != other.duration) return false
        if (tutees.size != other.tutees.size) return false
        for (i in 0..(tutees.size - 1)) {
            if (!tutees[i].equals(other.tutees[i])) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = tutor?.hashCode() ?: 0
        result = 31 * result + (dateTime?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + tutees.hashCode()
        result = 31 * result + (id?.hashCode() ?: 0)
        return result
    }
}

///addSession
//POST
//INPUT: {
//    tutor: tutor_id
//    tutees: [tutee_1_id, tutee_2_id, ...]
//    date: some data representation
//    duration: some time representation
//}
//Returns: Nothing
//
///removeSession
//POST
//INPUT: {
//    tutor: tutor_id
//    tutees: [tutee_1_id, tutee_2_id, ...]
//}
//Returns: Success/Failure
//
///getSessions
//POST
//INPUT: {
//    tutor: tutor_id
//}
//Returns JSON array of Session object:
//i.e. [{
//    tutor: tutor_id
//    tutees: [tutee_1_id, tutee_2_id, ...]
//    date: some data representation
//    duration: some time representation
//}]