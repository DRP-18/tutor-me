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