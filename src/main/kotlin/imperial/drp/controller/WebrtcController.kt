package imperial.drp.controller

import imperial.drp.dao.PersonRepository
import imperial.drp.model.CallingMessage
import imperial.drp.model.SimpleMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller

@Controller
class WebrtcController {

    @Autowired
    lateinit var sender : SimpMessageSendingOperations

    @Autowired
    private val personRepository: PersonRepository? = null

    @MessageMapping("/video.getUsername")
    fun getName(@Payload message: SimpleMessage) {
        val id = message.message
        println("This is the username message $message")
        println("This is the id converted to a long ${id.toLong()}")
        val name = personRepository?.findById(id.toLong())?.get()?.name!!
        println("Returning $name to $id")
        sender.convertAndSend("/topic/video/${id}/username", SimpleMessage(name))
    }

    @MessageMapping("/video.disconnect")
    fun disconnect(@Payload message: SimpleMessage)  {
        // delete user from records
    }

    @MessageMapping("/video.callUser")
    fun callUser(@Payload message: CallingMessage) {
        println("User message: ${message.callee}, ${message.caller}")
//        val calleeID = personRepository?.findByName(message.callee)?.get(0)?.id
//        println("Callee ID: ${calleeID}")
        val calleeID = message.callee
        sender.convertAndSend("/topic/video/$calleeID/incomingCall", message)
    }


    @MessageMapping("/video.acceptCall")
    fun acceptCall(@Payload message: CallingMessage){
        println("accept Call message: ${message.callee}, ${message.caller}")
//        val callerID = personRepository?.findByName(message.caller)?.get(0)?.id
//        print("Caller ID: ${callerID}}}")
        val callerID = message.caller

        sender.convertAndSend("/topic/video/$callerID/callAccepted", message)
    }

}