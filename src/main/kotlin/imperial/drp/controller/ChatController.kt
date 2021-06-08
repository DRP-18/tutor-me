package imperial.drp.controller

import imperial.drp.dao.PersonRepository
import imperial.drp.model.ChatMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CookieValue

@Controller
class ChatController {
    @Autowired
    private val personRepository: PersonRepository? = null

    @Autowired
    lateinit var sender: SimpMessageSendingOperations

    @MessageMapping("/chat.send")
    @SendTo("/topic/chat")
    fun sendMessage(@Payload chatMessage: ChatMessage): ChatMessage {
        return chatMessage
    }

    @MessageMapping("/chat.newUser")
    @SendTo("/topic/chat")
    fun newUser(@Payload chatMessage: ChatMessage, headerAccessor: SimpMessageHeaderAccessor): ChatMessage {
        headerAccessor.sessionAttributes?.put("username", chatMessage.sender)
        return chatMessage
    }

    @MessageMapping("/chat.existingUser")
    fun existingUser(@Payload chatMessage: ChatMessage){
        print(chatMessage.sender)
        val id = chatMessage.sender.toLong()
        print(id)
        var userOptional = personRepository!!.findById(id)
        print(userOptional)
        var user = ""
        if (userOptional.isPresent) {
            user = userOptional.get().name!!

//            headerAccessor.sessionAttributes?.put("username", user.name)

        }

        sender.convertAndSend("/topic/chat-${chatMessage.sender}", ChatMessage(chatMessage.type, chatMessage.content, user, chatMessage.time))
    }
}