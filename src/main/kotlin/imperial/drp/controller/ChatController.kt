package imperial.drp.controller

import com.fasterxml.jackson.databind.ObjectMapper
import imperial.drp.dao.ConversationRepository
import imperial.drp.dao.MessageRepository
import imperial.drp.dao.PersonRepository
import imperial.drp.entity.Conversation
import imperial.drp.entity.Message
import imperial.drp.entity.Tutor
import imperial.drp.model.ChatMessage
import imperial.drp.model.SimpleMessage
import imperial.drp.model.UserDetail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller
import java.util.*
import javax.transaction.Transactional

@Controller
class ChatController {

    @Autowired
    private val personRepository: PersonRepository? = null

    @Autowired
    private val conversationRepository: ConversationRepository? = null

    @Autowired
    private val messageRepository: MessageRepository? = null

    @Autowired
    lateinit var messageSender: SimpMessageSendingOperations

    @Transactional
    @MessageMapping("/chat.getMessages")
    fun getAllMessage(@Payload chatMessage: ChatMessage) {
        val userId = chatMessage.sender.toLong()
        val person = personRepository!!.findById(userId).get()
        val convs = conversationRepository!!.findAllByUser1OrUser2(person, person)
        val recentChatsMap = mutableMapOf<String, List<Message>>()
        if (convs.isNotEmpty()) {
            for (conv in convs) {
                var otherUser = conv.user1!!.id
                if (conv.user1!!.id == userId) {
                    otherUser = conv.user2!!.id
                }
                val messages = messageRepository!!.findByConversation(conv)
                println("messages $messages ${messages.size}")
                recentChatsMap[otherUser!!.toString()] = messages
            }
        }
        if (person is Tutor) {
            for (tutee in person.tutees!!) {
                if (tutee.id.toString() !in recentChatsMap.keys) {
                    recentChatsMap[tutee!!.id.toString()] = emptyList()
                }
            }
        }

        println("about to send messages back $recentChatsMap")
        val jsonObject = ObjectMapper()
        val json = jsonObject.writeValueAsString(recentChatsMap)
        println("jsoned ${json}")
        messageSender.convertAndSend("/topic/chat-${chatMessage.sender}-allMessages", object {
            val messages = json
        })
    }

    @Transactional
    @MessageMapping("/chat.getUsersDetails")
    fun getUsersDetails(@Payload chatMessage: ChatMessage) {
        val userId = chatMessage.sender.toLong()
        val person = personRepository!!.findById(userId).get()
        val convs = conversationRepository!!.findAllByUser1OrUser2(person, person)
        val userDetails = mutableMapOf<String, UserDetail>()
        if (convs.isNotEmpty()) {
            for (conv in convs) {
                var otherUser = conv.user1
                if (conv.user1!!.id == userId) {
                    otherUser = conv.user2
                }
                val user = personRepository!!.findById(otherUser!!.id!!).get()
                val detail = UserDetail(user.name!!, user.status)
                userDetails[otherUser!!.id!!.toString()] = detail
            }
        }
        if (person is Tutor) {
            for (tutee in person.tutees!!) {
                if (tutee.id.toString() !in userDetails.keys) {
                    val user = personRepository!!.findById(tutee.id!!).get()
                    userDetails[tutee!!.id.toString()] = UserDetail(user.name!!, user.status)
                }
            }
        }
        val jsonObject = ObjectMapper()
        val json = jsonObject.writeValueAsString(userDetails)
        println("jsoned ${json}")
        messageSender.convertAndSend("/topic/chat-${chatMessage.sender}-allUserDetails", object {
            val details = json
        })
    }

    @Transactional
    @MessageMapping("/chat.send")
    fun sendMessage(@Payload chatMessage: ChatMessage) {
        println("Chat message received ${chatMessage.content} ${chatMessage.sender} ${chatMessage.recipient} ${chatMessage.time}")
        val sender = personRepository!!.findById(chatMessage.sender.toLong()).get()
        val recipient = personRepository!!.findById(chatMessage.recipient.toLong()).get()
        var convList = mutableListOf<Conversation>()
        convList.addAll(conversationRepository!!.findAllByUser1AndUser2(sender, recipient))
        convList.addAll(conversationRepository!!.findAllByUser1OrUser2(recipient, sender))
        lateinit var conv: Conversation
        var convSet = false
        for (conversation in convList) {
            if ((conversation.user1!!.id == chatMessage.sender.toLong() && conversation.user2!!.id == chatMessage.recipient.toLong())
                    || (conversation.user2!!.id == chatMessage.sender.toLong() && conversation.user1!!.id == chatMessage.recipient.toLong())) {
                conv = conversation
                convSet = true
                break
            }
        }
        if (!convSet) {
            conv = Conversation(sender, recipient)
            conversationRepository.save(conv)
        }
        val message = Message(conv, sender, chatMessage.content, GregorianCalendar())
        messageRepository!!.save(message)
        messageSender.convertAndSend("/topic/chat-${chatMessage.recipient}-receiveMessage", message)
    }


//    @MessageMapping("/chat.newUser")
//    @SendTo("/topic/chat")
//    fun newUser(@Payload chatMessage: ChatMessage, headerAccessor: SimpMessageHeaderAccessor): ChatMessage {
//        headerAccessor.sessionAttributes?.put("username", chatMessage.sender)
//        return chatMessage
//    }
//
//
//
//    @MessageMapping("/chat.existingUser")
//    fun existingUser(@Payload chatMessage: ChatMessage) {
//        val id = chatMessage.sender.toLong()
//        var userOptional = personRepository!!.findById(id)
//        var user = ""
//        if (userOptional.isPresent) {
//            user = userOptional.get().name!!
//        }
//        sender.convertAndSend("/topic/chat-${chatMessage.sender}", ChatMessage(chatMessage.content, user, chatMessage.time))
//    }
}