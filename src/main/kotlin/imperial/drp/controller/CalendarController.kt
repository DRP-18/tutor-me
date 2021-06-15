package imperial.drp.controller

import imperial.drp.dto.PostResponseDto
import imperial.drp.model.SessionMessage
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletResponse

@Controller
class CalendarController {

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

        println("This is the session message: ")
        println(message.tutor)
        println(message.tutees)
        println(message.dateTime)
        println(message.duration)
        return ResponseEntity(PostResponseDto(), HttpStatus.OK)
    }


}