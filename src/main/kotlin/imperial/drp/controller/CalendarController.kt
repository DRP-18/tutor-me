package imperial.drp.controller

import com.fasterxml.jackson.databind.ObjectMapper
import imperial.drp.dao.PersonRepository
import imperial.drp.dao.SessionRepository
import imperial.drp.dto.PostResponseDto
import imperial.drp.entity.Session
import imperial.drp.entity.Tutee
import imperial.drp.entity.Tutor
import imperial.drp.model.SessionMessage
import imperial.drp.model.SimpleMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.http.HttpServletResponse

@Controller
class CalendarController {

    @Autowired
    private val personRepository: PersonRepository? = null

    @Autowired
    private val sessionRepository: SessionRepository? = null

    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
    val jsonObject = ObjectMapper()

    @RequestMapping("/calendar")
    fun calendar(): String {
        return "calendar"
    }

    @PostMapping("/addSession")
    fun addSession(@RequestBody message: SessionMessage,
                   response: HttpServletResponse): ResponseEntity<PostResponseDto> {
        val tutorOpt = personRepository!!.findById(message.tutor.toLong())
        if (tutorOpt.isPresent && tutorOpt.get() is Tutor) {
            val tutor = tutorOpt.get() as Tutor
            val tutee = personRepository.findByName(message.tutees)
            if (tutee.isNotEmpty() && tutee[0] is Tutee) {
                if (tutor.tutees?.contains(tutee[0])!!) {
                    val startTime = GregorianCalendar()
                    startTime.time = sdf.parse(message.startTime)

                    val endTime = GregorianCalendar()
                    endTime.time = sdf.parse(message.endTime)
                    val tutees = mutableListOf<Tutee>()
                    tutees.add(tutee[0] as Tutee)
                    val session = Session(tutor,
                            startTime,
                            endTime,
                            tutees)

                    val alreadyExistingSesh = sessionRepository!!.findByTutorAndStartTimeAndEndTime(tutor, startTime, endTime)
                    if (alreadyExistingSesh.isNotEmpty()) {
                        for (sesh: Session in alreadyExistingSesh) {
                            if (sesh.equals(session)) {
                                return ResponseEntity(PostResponseDto("Session at this time already exists with ${message.tutees}"), HttpStatus.METHOD_NOT_ALLOWED)
                            }
                        }
                    }
                    sessionRepository.save(session)
                    return ResponseEntity(PostResponseDto(), HttpStatus.OK)
                }
                return ResponseEntity(PostResponseDto("${message.tutees} is not one of your tutees"), HttpStatus.METHOD_NOT_ALLOWED)
            }
            return ResponseEntity(PostResponseDto("No tutee with name ${message.tutees} was found"), HttpStatus.METHOD_NOT_ALLOWED)
        }
        return ResponseEntity(PostResponseDto("Not a tutor, only tutors can organise sessions"), HttpStatus.METHOD_NOT_ALLOWED)
    }


    @PostMapping("/removeSession")
    fun removeSession(@RequestBody message: SessionMessage,
                      response: HttpServletResponse): ResponseEntity<PostResponseDto> {
        val tutorOpt = personRepository!!.findById(message.tutor.toLong())
        if (tutorOpt.isPresent && tutorOpt.get() is Tutor) {
            val tutor = tutorOpt.get() as Tutor
            val startTime = GregorianCalendar()
            startTime.time = sdf.parse(message.startTime)
            val endTime = GregorianCalendar()
            endTime.time = sdf.parse(message.endTime)
            val session = sessionRepository!!.findByTutorAndStartTimeAndEndTime(tutor, startTime, endTime)
            if (session.isNotEmpty()) {
                sessionRepository.delete(session[0])
                return ResponseEntity(PostResponseDto(), HttpStatus.OK)
            }
            return ResponseEntity(PostResponseDto("Session does not exist"), HttpStatus.METHOD_NOT_ALLOWED)
        }
        return ResponseEntity(PostResponseDto("Not a tutor, only tutors can organise sessions"), HttpStatus.METHOD_NOT_ALLOWED)
    }

    @PostMapping("/getSessions")
    @ResponseBody
    fun getSessions(@CookieValue(value = "user_type") user_type: String,
                    @RequestBody message: SimpleMessage,
                    response: HttpServletResponse): String {
        val tutorOpt = personRepository!!.findById(message.message.toLong())
        val sessions = mutableListOf<Session>()
        val listSessions = mutableListOf<SessionMessage>()
        val isTutor = (user_type == "tutor")
        if (isTutor) {
            if (tutorOpt.isEmpty) {
                return ""
            }
            sessions.addAll(sessionRepository!!.findByTutor(tutorOpt.get() as Tutor))
        } else {
            val tuteeOpt = personRepository!!.findById(message.message.toLong())
            if (tuteeOpt.isEmpty) {
                return ""
            }
            val tutee = tuteeOpt.get() as Tutee
            for (tutor in tutee.tutors!!.toSet()) {
                val tutorSessions = sessionRepository!!.findByTutor(tutor)
                for (session in tutorSessions) {
                    if (session.tutees.contains(tutee)) {
                        sessions.add(session)
                    }
                }
            }
        }
        var myName = ""
        var theirName = ""
        sessions.forEach {
            if (isTutor) {
                myName = it.tutor!!.name.toString()
                theirName = it.tutees[0].name!!
            } else {
                theirName = it.tutor!!.name.toString()
                myName = it.tutees[0].name!!
            }
            listSessions.add(SessionMessage(myName,
                    theirName,
                    convertDate("EEE MMM dd HH:mm:ss Z yyyy",
                            "EEE MMM dd yyyy HH:mm:ss",
                            it.startTime?.time.toString()),
                    convertDate("EEE MMM dd HH:mm:ss Z yyyy",
                            "EEE MMM dd yyyy HH:mm:ss",
                            it.endTime?.time.toString())))
        }
        println("Sent back sessions")
        return jsonObject.writeValueAsString(listSessions)

    }

    private fun convertDate(inputPattern: String, outputPattern: String, providedDate: String): String {
        val dateFormat = SimpleDateFormat(inputPattern)
        val date = dateFormat.parse(providedDate)
        dateFormat.applyPattern(outputPattern)
        return dateFormat.format(date)
    }
}