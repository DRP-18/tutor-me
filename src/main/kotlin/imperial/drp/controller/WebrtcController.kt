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
    val currentGroupCalls = mutableMapOf<Int, MutableList<Person>>() //Map of group number to group members
    val groupNumberAllocations = mutableMapOf<Long, Int>() //Map of Person Id to group number
    var groupNumber: Sequence<Int> = generateSequence(1) { it + 1 }

    init {
        val ACCOUNT_SID = getenv("TWILIO_ACCOUNT_SID")
        val AUTH_TOKEN = getenv("TWILIO_AUTH_TOKEN")
        if (ACCOUNT_SID != null && AUTH_TOKEN != null) {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN)
            iceServers = Token.creator().create().iceServers
        } else {
            log.warn("TWILIO_ACCOUNT_SID or TWILIO_AUTH_TOKEN isn't present!")
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
            val detail = UserDetail(person.name!!, person.status, person.avatar.toString())
            peopleDetails[person.id!!] = detail
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
        val person = personRepository?.findById(message.message.toLong())!!.get()!!
        if (currentGroupCalls.containsKey(number)) {
            currentGroupCalls[number]?.add(person)
        } else {
            currentGroupCalls[number] = mutableListOf(person)
        }
        groupNumberAllocations[person.id!!] = number
        sender.convertAndSend("/topic/video/${message.message}/groupId", number)
    }

    @MessageMapping("/video.groupCallUser")
    fun groupCallUser(@Payload message: CallerCalleeMessage) {
        val calleeID = message.callee
        val callerName = personRepository?.findById(message.caller.toLong())!!.get().name!!
        sender.convertAndSend("/topic/video/$calleeID/groupIncomingCall",
                CallingMessageWithName(message.callee, message.caller, callerName, SignalObject("", "")))
    }

    @MessageMapping("/video.groupAcceptCall")
    fun groupAcceptCall(@Payload message: CallerCalleeMessage) {
        val calleePerson = personRepository?.findById(message.callee.toLong())!!.get()!!
        val groupNum: Int = groupNumberAllocations[message.caller.toLong()]!!

        var sent = false
        if (groupNumberAllocations.containsKey(calleePerson.id)) {
            val groupNum = groupNumberAllocations[calleePerson.id]!!
            val members = currentGroupCalls[groupNum]
            if (members?.size!! > 1) {

                //Remove caller's group number
                val callerPerson = personRepository.findById(message.caller.toLong()).get()
                val callerGroupNum = groupNumberAllocations[message.caller.toLong()]!!
                currentGroupCalls[callerGroupNum]?.remove(callerPerson)
                if (currentGroupCalls[callerGroupNum]?.isEmpty()!!) {
                    currentGroupCalls.remove(callerGroupNum)
                    replaceGroupNumber(callerGroupNum)
                }

                sender.convertAndSend("/topic/video/${message.caller}/peersInRoom", object {
                    val peers = members
                })
                groupNumberAllocations[message.caller.toLong()] = groupNum
                currentGroupCalls[groupNum]?.add(callerPerson)
                sent = true
            } else {
                //Remove callee's group number in preparation for changing their group
                members.remove(calleePerson)
                if (members.isEmpty()) {
                    replaceGroupNumber(groupNum)
                }
            }
        }
        if (!sent) {
            sender.convertAndSend("/topic/video/${message.callee}/peersInRoom", object {
                val peers = currentGroupCalls[groupNum]!!
            })
            groupNumberAllocations[message.callee.toLong()] = groupNum
            currentGroupCalls[groupNum]?.add(calleePerson)
        }
    }

    @MessageMapping("/video.sendSignalToPeer")
    fun signalToNewPeer(@Payload message: CallingMessage) {
        val name = personRepository?.findById(message.caller.toLong())?.get()!!.name!!
        sender.convertAndSend("/topic/video/${message.callee}/newPeer",
                CallingMessageWithName(message.callee, message.caller, name, message.signal))
    }

    @MessageMapping("/video.returnToNewPeer")
    fun returningSignalToNewPeer(@Payload message: CallingMessage) {
        sender.convertAndSend("/topic/video/${message.caller}/returningSignal", message)
    }

    @MessageMapping("/video.leaveGroupCall")
    fun leaveGroupCall(@Payload message: SimpleMessage) {
        val personId = message.message.toLong()
        val groupNum = groupNumberAllocations[personId]!!
        groupNumberAllocations.remove(personId)
        val personToRemove = personRepository?.findById(personId)?.get()
        currentGroupCalls[groupNum]!!.remove(personToRemove)
        for (person in currentGroupCalls[groupNum]!!) {
            sender.convertAndSend("/topic/video/${person.id}/removePeer", message)
        }
    }
}
