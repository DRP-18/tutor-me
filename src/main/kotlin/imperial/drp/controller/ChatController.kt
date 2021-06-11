package imperial.drp.controller

import com.fasterxml.jackson.databind.ObjectMapper
import imperial.drp.dao.ConversationRepository
import imperial.drp.dao.MessageRepository
import imperial.drp.dao.PersonRepository
import imperial.drp.entity.Message
import imperial.drp.entity.Person
import imperial.drp.entity.Tutor
import imperial.drp.model.ChatMessage
import net.minidev.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CookieValue
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
    lateinit var sender: SimpMessageSendingOperations

    @MessageMapping("/chat.send")
    @SendTo("/topic/chat")
    fun sendMessage(@Payload chatMessage: ChatMessage): ChatMessage {
        println("Chat message recived ${chatMessage.content}")
        return chatMessage
    }

    @MessageMapping("/chat.newUser")
    @SendTo("/topic/chat")
    fun newUser(@Payload chatMessage: ChatMessage, headerAccessor: SimpMessageHeaderAccessor): ChatMessage {
        headerAccessor.sessionAttributes?.put("username", chatMessage.sender)
        return chatMessage
    }

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
            if (person is Tutor) {
                for (tutee in person.tutees!!) {
                    if (tutee.id.toString() !in recentChatsMap.keys) {
                        recentChatsMap[tutee!!.id.toString()] = emptyList()
                    }
                }
            }
        }
        println("about to send messages back $recentChatsMap")
        val jsonObject = ObjectMapper()
        val json = jsonObject.writeValueAsString(recentChatsMap)
        println("jsoned ${json}")
        sender.convertAndSend("/topic/chat-${chatMessage.sender}-allMessages", object {
            val messages = json
        })
    }

    @MessageMapping("/chat.existingUser")
    fun existingUser(@Payload chatMessage: ChatMessage) {
        val id = chatMessage.sender.toLong()
        var userOptional = personRepository!!.findById(id)
        var user = ""
        if (userOptional.isPresent) {
            user = userOptional.get().name!!
        }
        sender.convertAndSend("/topic/chat-${chatMessage.sender}", ChatMessage(chatMessage.content, user, chatMessage.time))
    }
}