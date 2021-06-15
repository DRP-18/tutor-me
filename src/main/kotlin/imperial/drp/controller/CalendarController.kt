package imperial.drp.controller

import imperial.drp.dao.PersonRepository
import imperial.drp.dao.SessionRepository
import imperial.drp.dto.PostResponseDto
import imperial.drp.entity.Session
import imperial.drp.entity.Tutee
import imperial.drp.entity.Tutor
import imperial.drp.model.SessionMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import javax.servlet.http.HttpServletResponse

@Controller
class CalendarController {

    @Autowired
    private val personRepository: PersonRepository? = null

    @Autowired
    private val sessionRepository: SessionRepository? = null

    val sdf = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss")

    @RequestMapping("/calendar")
    fun calendar(): String {
//        val tutor = personRepository!!.findById(1).get() as Tutor
//        val tutes: MutableList<Tutee> = mutableListOf()
//        tutes.add(personRepository.findById(2).get() as Tutee)
//        tutes.add(personRepository.findById(31).get() as Tutee)
//
//        val session = Session(tutor, GregorianCalendar(), LocalTime.of(0,5), tutes)
//        sessionRepository!!.save(session)
        return "calendar"
    }

    @PostMapping("/addSession")
    fun addSession(@RequestBody message: SessionMessage,
                   response: HttpServletResponse): ResponseEntity<PostResponseDto> {
        val tutorOpt = personRepository!!.findById(message.tutor.toLong())
        if (tutorOpt.isPresent) {
            val tutor = tutorOpt.get() as Tutor
            val tutee = personRepository!!.findByName(message.tutees)
            if (tutee.isNotEmpty() || tutee[0] !is Tutee) {
                if (tutor.tutees?.contains(tutee[0])!!) {
                    val startTime = GregorianCalendar()
                    startTime.time = sdf.parse(message.dateTime)
                    val tutees = mutableListOf<Tutee>()
                    tutees.add(tutee[0] as Tutee)
                    val session = Session(tutor,
                            startTime,
                            LocalTime.of(0, message.duration.toInt()),
                            tutees)
                    sessionRepository!!.save(session)
                    return ResponseEntity(PostResponseDto(), HttpStatus.OK)
                }
                return ResponseEntity(PostResponseDto("${message.tutees} is not one of your tutees"), HttpStatus.NOT_FOUND)
            }
            return ResponseEntity(PostResponseDto("No tutee with name ${message.tutees} is found"), HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(PostResponseDto("Not a tutor, only tutors can organise session"), HttpStatus.NOT_FOUND)
    }


    @PostMapping("/removeSession")
    fun removeSession(@RequestBody message: SessionMessage,
                      response: HttpServletResponse): ResponseEntity<PostResponseDto> {
        val tutorOpt = personRepository!!.findById(message.tutor.toLong())
        if (tutorOpt.isPresent) {
            val tutor = tutorOpt.get() as Tutor
            val startTime = GregorianCalendar()
            startTime.time = sdf.parse(message.dateTime)
            val session = sessionRepository!!.findByTutorAndDateTime(tutor, startTime)
            if (session.isNotEmpty()) {
                sessionRepository.delete(session[0])
                return ResponseEntity(PostResponseDto(), HttpStatus.OK)
            }
            return ResponseEntity(PostResponseDto("Session does not exist"), HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(PostResponseDto("Not a tutor, only tutors can organise session"), HttpStatus.NOT_FOUND)
    }

}