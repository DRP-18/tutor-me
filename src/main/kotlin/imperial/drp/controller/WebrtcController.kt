package imperial.drp.controller

import imperial.drp.dao.PersonRepository
import imperial.drp.model.CallingMessage
import imperial.drp.model.VideoMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CookieValue

@Controller
class WebrtcController {

    @Autowired
    lateinit var sender : SimpMessageSendingOperations

    @Autowired
    private val personRepository: PersonRepository? = null

    @MessageMapping("/video.disconnect")
    fun disconnect(@Payload message: VideoMessage)  {
        // delete user from records
    }

    @MessageMapping("/video.callUser")
    fun callUser(@Payload message: CallingMessage) {
        println("call User message: ${message.callee}, ${message.caller}, ${message.signal}")
        val calleeID = personRepository?.findByName(message.callee)?.get(0)?.id
        println("Callee ID: ${calleeID}")
        sender.convertAndSend("/topic/video/$calleeID/incomingCall", message)
    }


    @MessageMapping("/video.acceptCall")
    fun acceptCall(@Payload message: CallingMessage){
        println("accept Call message: ${message.callee}, ${message.caller}, ${message.signal}")
        val callerID = personRepository?.findByName(message.caller)?.get(0)?.id
        print("Caller ID: ${callerID}}}")
        sender.convertAndSend("/topic/video/$callerID/callAccepted", message)
    }

}