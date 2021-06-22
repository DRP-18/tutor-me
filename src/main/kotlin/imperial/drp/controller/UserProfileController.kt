package imperial.drp.controller

import imperial.drp.dao.PersonRepository
import imperial.drp.entity.Tutee
import imperial.drp.entity.Tutor
import imperial.drp.model.TuteeInfoMessage
import imperial.drp.model.UserProfileMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CookieValue
import javax.transaction.Transactional

@Controller
class UserProfileController {

    @Autowired
    private val personRepository: PersonRepository? = null


    @Transactional
    @MessageMapping("/user.updateProfile")
    fun updateProfile(
            userMessage: UserProfileMessage) {
        val personOpt = personRepository?.findById(userMessage.userId.toLong())!!
        if (personOpt.isPresent) {
            val person = personOpt.get()
            person.name = userMessage.name
            person.status = userMessage.status
            person.avatar = userMessage.avatar.toInt()
            personRepository.save(person)
        }
    }

    @Transactional
    @MessageMapping("/user.removeTutee")
    fun removeTutee(tuteeInfoMessage: TuteeInfoMessage) {
        val userOpt = personRepository!!.findById(tuteeInfoMessage.tutorId.toLong())
        if (userOpt.isPresent) {
            val user = userOpt.get()
            if (user is Tutor) {
                val tuteeOpt = personRepository.findById(tuteeInfoMessage.tuteeId.toLong())
                if (tuteeOpt.isPresent) {
                    val tutee = tuteeOpt.get() as Tutee
                    user.tutees!!.remove(tutee)
                    tutee.tutors!!.remove(user)
                    personRepository.save(user)
                    personRepository.save(tutee)
                }
            }
        }
    }
}