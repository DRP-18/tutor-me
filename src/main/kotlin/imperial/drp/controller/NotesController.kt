package imperial.drp.controller

import com.fasterxml.jackson.databind.ObjectMapper
import imperial.drp.dao.ConversationRepository
import imperial.drp.dao.MessageRepository
import imperial.drp.dao.NoteRepository
import imperial.drp.dao.PersonRepository
import imperial.drp.entity.*
import imperial.drp.model.ChatMessage
import imperial.drp.model.NoteMessage
import imperial.drp.model.SimpleMessage
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
        println("Note message received ${note.content} ${note.sender}")

        val noteToSave = Note(note.sender.toLong(), note.content)
        noteRepository!!.save(noteToSave)

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

    @Transactional
    @MessageMapping("/notes.getNotes")
    fun getAllNotes(@Payload message: SimpleMessage) {

        val userId = message.message.toLong()
        val listOfNotes = noteRepository!!.findByUserId(userId)

        messageSender.convertAndSend("/topic/notes-${userId}-receiveNotes", listOfNotes)
//
//        '/notes-' + userId + '-receiveNotes'

    //        val userId = notes[1].sender.toLong()
//        val person = personRepository!!.findById(userId).get()
//        val convs = conversationRepository!!.findAllByUser1OrUser2(person, person)
//        val recentChatsMap = mutableMapOf<String, List<Message>>()
//        if (convs.isNotEmpty()) {
//            for (conv in convs) {
//                var otherUser = conv.user1!!.id
//                if (conv.user1!!.id == userId) {
//                    otherUser = conv.user2!!.id
//                }
//                val messages = messageRepository!!.findByConversation(conv)
//                println("messages $messages ${messages.size}")
//                recentChatsMap[otherUser!!.toString()] = messages
//            }
//        }
//        if (person is Tutor) {
//            addEmptyConversationToPersons(person.tutees!!, recentChatsMap)
//        }
//        if (person is Tutee) {
//            addEmptyConversationToPersons(person.tutors!!, recentChatsMap)
//        }
//
//        println("about to send messages back $recentChatsMap")
//        val jsonObject = ObjectMapper()
//        val json = jsonObject.writeValueAsString(recentChatsMap)
//        println("jsoned ${json}")
//        messageSender.convertAndSend("/topic/chat-${chatMessage.sender}-allMessages", object {
//            val messages = json
//        })

//        '/notes-' + userId + '-receiveNotes'
    }
}