package imperial.drp.controller

import com.fasterxml.jackson.databind.ObjectMapper
import imperial.drp.dao.ConversationRepository
import imperial.drp.dao.MessageRepository
import imperial.drp.dao.PersonRepository
import imperial.drp.entity.*
import imperial.drp.model.ChatMessage
import imperial.drp.model.UserDetail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller
import java.util.*
import javax.transaction.Transactional

@Controller
class NotesController {
    @Transactional
    @MessageMapping("/chat.send")
    fun sendMessage(@Payload chatMessage: ChatMessage) {
        println("Chat message received ${chatMessage.content} ${chatMessage.sender} ${chatMessage.recipient} ${chatMessage.time}")
//        val sender = personRepository!!.findById(chatMessage.sender.toLong()).get()
//        val recipient = personRepository!!.findById(chatMessage.recipient.toLong()).get()
        val convList = mutableListOf<Conversation>()
//        convList.addAll(conversationRepository!!.findAllByUser1AndUser2(sender, recipient))
//        convList.addAll(conversationRepository!!.findAllByUser1OrUser2(recipient, sender))
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
//        if (!convSet) {
//            conv = Conversation(sender, recipient)
//            conversationRepository.save(conv)
//        }
//        val message = Message(conv, sender, chatMessage.content, GregorianCalendar())
//        messageRepository!!.save(message)
//        messageSender.convertAndSend("/topic/chat-${chatMessage.recipient}-receiveMessage", message)

// send all notes back via a specified channel 
    }}