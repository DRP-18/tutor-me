package imperial.drp.controller

import imperial.drp.dao.ConversationRepository
import imperial.drp.dao.MessageRepository
import imperial.drp.dao.NoteRepository
import imperial.drp.dao.PersonRepository
import imperial.drp.entity.*
import imperial.drp.model.ChatMessage
import imperial.drp.model.NoteMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller
import javax.transaction.Transactional

@Controller
class NotesController {

    @Autowired
    private val personRepository: PersonRepository? = null

    @Autowired
    private val conversationRepository: ConversationRepository? = null

    @Autowired
    private val messageRepository: MessageRepository? = null


    @Autowired
    private val noteRepository: NoteRepository? = null

    @Autowired
    lateinit var messageSender: SimpMessageSendingOperations

    @Transactional
    @MessageMapping("/notes.addNote")
    fun saveNote(@Payload note: NoteMessage) {
//        println("Chat message received ${chatMessage.content} ${chatMessage.sender} ${chatMessage.recipient} ${chatMessage.time}")
        val userId = note.sender.toLong();
        val content = note.content

        noteRepository!!.save(content)
//        val sender = personRepository!!.findById(userId).get()
//        val noteContentList = mutableListOf<String>()
//
//        val savedNote = noteRepository.findAllById(userId)
//        if (savedNote.isNotEmpty()) {
//            noteContentList.addAll(savedNote)
//        }
//


//        convList.addAll(conversationRepository!!.findAllByUser1AndUser2(sender, recipient))
//        convList.addAll(conversationRepository!!.findAllByUser1OrUser2(recipient, sender))
//        lateinit var conv: Conversation
//        var convSet = false
//        for (conversation in convList) {
//            if ((conversation.user1!!.id == note.sender.toLong() && conversation.user2!!.id == note.recipient.toLong())
//                    || (conversation.user2!!.id == note.sender.toLong() && conversation.user1!!.id == note.recipient.toLong())) {
//                conv = conversation
//                convSet = true
//                break
//            }
//        }
//        if (!convSet) {
//            conv = Conversation(sender, recipient)
//            conversationRepository.save(conv)
//        }
//        val message = Message(conv, sender, chatMessage.content, GregorianCalendar())
//        messageRepository!!.save(message)
//        messageSender.convertAndSend("/topic/chat-${chatMessage.recipient}-receiveMessage", message)

// send all notes back via a specified channel 
    }
}