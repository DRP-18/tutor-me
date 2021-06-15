package imperial.drp.entity

import java.time.LocalTime
import java.util.*

class Session(
        var tutor: Tutor? = null,
        var tutees: MutableList<Tutee>? = mutableListOf(),
        var date: Calendar? = null,
        var duration: LocalTime? = null
) {
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