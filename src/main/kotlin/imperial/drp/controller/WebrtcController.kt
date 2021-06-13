package imperial.drp.controller

import imperial.drp.dao.PersonRepository
import imperial.drp.entity.Person
import imperial.drp.model.CallingMessage
import imperial.drp.model.CallingMessageWithName
import imperial.drp.model.SimpleMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller
import java.util.*


@Controller
class WebrtcController {

    @Autowired
    lateinit var sender: SimpMessageSendingOperations

    @Autowired
    private val personRepository: PersonRepository? = null

    @MessageMapping("/video.endCall")
    fun endCall(@Payload message: SimpleMessage) {
        println("The disconnecting message is $message")
        println("The disconnecting message 2 ${message.message}")


        sender.convertAndSend("/topic/video/${message.message}/endCall", object {})
    }

    @MessageMapping("/video.getAllUsers")
    fun getAllUsers(@Payload message: SimpleMessage) {

        val people = mutableMapOf<Long, String>()
        personRepository!!.findAll().forEach {
            people.put(it.id!!, it.name!!)
        }
        sender.convertAndSend("/topic/video/${message.message}/username", object {
            val data = people
        })
    }

    @MessageMapping("/video.disconnect")
    fun disconnect(@Payload message: SimpleMessage) {
        println("The disconnecting message is $message")
        sender.convertAndSend("/topic/video/${message.message}/endCall", object {})
    }

    @MessageMapping("/video.callUser")
    fun callUser(@Payload message: CallingMessage) {
        println("User message: ${message.callee}, ${message.caller}")
        val calleeID = message.callee
        val callerName = personRepository?.findById(message.caller.toLong())!!.get().name!!
        sender.convertAndSend("/topic/video/$calleeID/incomingCall",
                CallingMessageWithName(message.callee, message.caller, callerName, message.signal))
    }


    @MessageMapping("/video.acceptCall")
    fun acceptCall(@Payload message: CallingMessage) {
        println("accept Call message: ${message.callee}, ${message.caller}")
        val callerID = message.caller
        sender.convertAndSend("/topic/video/$callerID/callAccepted", message)
    }

}