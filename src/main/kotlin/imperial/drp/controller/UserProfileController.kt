package imperial.drp.controller

import imperial.drp.dao.PersonRepository
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

}