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
import java.text.SimpleDateFormat
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

    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
    val jsonObject = ObjectMapper()

    init {
        jsonObject.dateFormat = sdf
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
        }
        if (person is Tutor) {
            addEmptyConversationToPersons(person.tutees!!, recentChatsMap)
        }
        if (person is Tutee) {
            addEmptyConversationToPersons(person.tutors!!, recentChatsMap)
        }

        println("about to send messages back $recentChatsMap")
        val json = jsonObject.writeValueAsString(recentChatsMap)
        println("jsoned ${json}")
        messageSender.convertAndSend("/topic/chat-${chatMessage.sender}-allMessages", object {
            val messages = json
        })
    }

    private fun addEmptyConversationToPersons(people: List<Person>, recentChatsMap: MutableMap<String, List<Message>>) {
        for (person in people) {
            if (person.id.toString() !in recentChatsMap.keys) {
                recentChatsMap[person.id.toString()] = emptyList()
            }
        }
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
                val user = personRepository.findById(otherUser!!.id!!).get()
                val detail = UserDetail(user.name!!, user.status, user.avatar.toString())
                userDetails[otherUser.id!!.toString()] = detail
            }
        }
        if (person is Tutor) {
            addUserDetailsOfPerson(person.tutees!!, userDetails)
        }
        if (person is Tutee) {
            addUserDetailsOfPerson(person.tutors!!, userDetails)
        }
        userDetails[person.id!!.toString()] = UserDetail(person.name!!, person.status, person.avatar.toString())
        val json = jsonObject.writeValueAsString(userDetails)
        messageSender.convertAndSend("/topic/chat-${chatMessage.sender}-allUserDetails", object {
            val details = json
        })
    }

    private fun addUserDetailsOfPerson(people: List<Person>, userDetails: MutableMap<String, UserDetail>) {
        val ids = userDetails.keys
        for (person in people) {
            if (person.id.toString() !in ids) {
                val user = personRepository!!.findById(person.id!!).get()
                userDetails[person.id.toString()] = UserDetail(user.name!!, user.status)
            }
        }
    }

    @Transactional
    @MessageMapping("/chat.send")
    fun sendMessage(@Payload chatMessage: ChatMessage) {
        println("Chat message received ${chatMessage.content} ${chatMessage.sender} ${chatMessage.recipient} ${chatMessage.time}")
        val sender = personRepository!!.findById(chatMessage.sender.toLong()).get()
        val recipient = personRepository.findById(chatMessage.recipient.toLong()).get()
        val convList = mutableListOf<Conversation>()
        convList.addAll(conversationRepository!!.findAllByUser1AndUser2(sender, recipient))
        convList.addAll(conversationRepository.findAllByUser1OrUser2(recipient, sender))
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
        val message = Message(conv, sender, chatMessage.content, Date())
        messageRepository!!.save(message)
        messageSender.convertAndSend("/topic/chat-${chatMessage.recipient}-receiveMessage", message)
    }
}