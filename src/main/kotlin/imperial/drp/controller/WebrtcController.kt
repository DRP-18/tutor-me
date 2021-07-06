package imperial.drp.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Token
import com.twilio.type.IceServer
import imperial.drp.DrpApplication
import imperial.drp.dao.PersonRepository
import imperial.drp.entity.Person
import imperial.drp.entity.Tutee
import imperial.drp.entity.Tutor
import imperial.drp.model.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CookieValue
import java.lang.System.getenv
import java.util.*


@Controller
class WebrtcController {

    private val log = LoggerFactory.getLogger(WebrtcController::class.java)

    lateinit var iceServers: List<IceServer>

    val currentCalls = mutableMapOf<Long, Boolean>()
    val currentGroupCalls = mutableMapOf<Int, List<Person>>()
    var groupNumber: Sequence<Int> = generateSequence(1) { it + 1 }

    init {
        val ACCOUNT_SID = getenv("TWILIO_ACCOUNT_SID")
        val AUTH_TOKEN = getenv("TWILIO_AUTH_TOKEN")
        if (ACCOUNT_SID != null && AUTH_TOKEN != null) {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
            iceServers = Token.creator().create().iceServers
        } else {
            log.warn("TWILIO_ACCOUNT_SID or TWILIO_AUTH_TOKEN doesn't present!")
        }
    }

    @Autowired
    lateinit var sender: SimpMessageSendingOperations

    @Autowired
    private val personRepository: PersonRepository? = null

    fun getGroupNumber(): Int {
        val number = groupNumber.take(1).toList()
        groupNumber = groupNumber.drop(1)
        return number[0]
    }

    fun replaceGroupNumber(number: Int) {
        groupNumber = sequenceOf(number) + groupNumber
    }

    @MessageMapping("/video.endCall")
    fun endCall(@Payload message: CallerCalleeMessage) {
        println("The disconnecting message is ${message.caller}")
        currentCalls.remove(message.callee.toLong())
        currentCalls.remove(message.caller.toLong())
        sender.convertAndSend("/topic/video/${message.caller}/endCall", object {})
    }

    @MessageMapping("/video.getAllUsers")
    fun getAllUsers(
            @Payload message: SimpleMessage) {
        val peopleDetails = mutableMapOf<Long, UserDetail>()
        val personOpt = personRepository!!.findById(message.message.toLong())
        if (personOpt.isPresent) {
            val person = personOpt.get()
            if (person is Tutor) {
                for (tutee in person.tutees!!) {
                    val detail = UserDetail(tutee.name!!, tutee.status, tutee.avatar.toString())
                    peopleDetails[tutee.id!!] = detail
                }
            } else if (person is Tutee) {
                for (tutor in person.tutors!!) {
                    val detail = UserDetail(tutor.name!!, tutor.status, tutor.avatar.toString())
                    peopleDetails[tutor.id!!] = detail
                }
            }
        }
        val jsonObject = ObjectMapper()
        val json = jsonObject.writeValueAsString(peopleDetails)
        sender.convertAndSend("/topic/video/${message.message}/userDetails", object {
            val details = json
        })
    }

    @MessageMapping("/video.callUser")
    fun callUser(@Payload message: CallingMessage) {
        println("User message: ${message.callee}, ${message.caller}")
        val calleeID = message.callee
        if (!currentCalls.contains(message.callee.toLong())) {
            val callerName = personRepository?.findById(message.caller.toLong())!!.get().name!!
            sender.convertAndSend("/topic/video/$calleeID/incomingCall",
                    CallingMessageWithName(message.callee, message.caller, callerName, message.signal))
        } else {
            sender.convertAndSend("/topic/video/${message.caller}/alreadyInCall", object {})
        }
    }


    @MessageMapping("/video.acceptCall")
    fun acceptCall(@Payload message: CallingMessage) {
        println("accept Call message: ${message.callee}, ${message.caller}")
        val callerID = message.caller
        currentCalls[message.callee.toLong()] = true
        currentCalls[message.caller.toLong()] = true
        sender.convertAndSend("/topic/video/$callerID/callAccepted", message)
    }

    @MessageMapping("/video.iceCandidates")
    fun getIceCandidates(@Payload message: SimpleMessage) {
        print("Sending back to ${message.message}" + iceServers)
        currentCalls.remove(message.message.toLong())
        sender.convertAndSend("/topic/video/${message.message}/iceCandidates", iceServers)
    }

    //    Group calls
    @MessageMapping("/video.getGroupId")
    fun getGroupId(@Payload message: SimpleMessage) {
        val number = getGroupNumber()
        sender.convertAndSend("/topic/video/${message.message}/groupId", number)
    }
}
