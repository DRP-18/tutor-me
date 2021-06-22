package imperial.drp.controller

import imperial.drp.dao.*
import imperial.drp.entity.Message
import imperial.drp.entity.Person
import imperial.drp.entity.Tutee
import imperial.drp.entity.Tutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class SitePageController {

    @Autowired
    private val personRepository: PersonRepository? = null

    @Autowired
    private val conversationRepository: ConversationRepository? = null

    @Autowired
    private val messageRepository: MessageRepository? = null

    // Redirects call page to local host 3000 for testing purposes
//        @RequestMapping("/calls_page")
//    fun videoCallPage(@CookieValue(value = "user_id", required = false) userId: String, response: HttpServletResponse) {
//        response.setHeader("Location", "http://localhost:3000/")
//        response.status = 302
//        response.addCookie(Cookie("user_id", userId))
//        println("redirected, this was the cookie $userId")
//    }

    @RequestMapping(path = ["/", "index", "dashboard"])
    fun homepage(
            @CookieValue(value = "user_id", required = false) userId: Long?,
            model: Model): String {
        if (userId != null) {
            model.addAttribute("name", personRepository!!.findById(userId).get().name)
        }
        return "dashboard"
    }

    @RequestMapping("/homework")
    fun homeworkPage(): String {
        return "homework"
    }

    @RequestMapping("/notes")
    fun notesPage(): String {
        return "notes"
    }

    @RequestMapping("/user")
    fun userProfilePage(
            @CookieValue(value = "user_id", required = false) userId: Long?,
            model: Model
    ): String {
        if (userId != null) {
            val person = personRepository!!.findById(userId!!).get()
            model.addAttribute("person", person)
            if (person is Tutor) {
                model.addAttribute("personList", person.tutees?.toSet())
                model.addAttribute("tutee", false)
            } else if (person is Tutee) {
                model.addAttribute("personList", person.tutors?.toSet())
                model.addAttribute("tutee", true)
            }
        }
        return "user"
    }

    @RequestMapping("/task/{task_id}")
    fun task(@PathVariable("task_id") taskId: Long,
             @CookieValue(value = "user_id", required = false) userId: Long?,
             model: Model
    ): String {
        if (userId != null) {
            val person = personRepository!!.findById(userId).get()
            model.addAttribute("tutor", person is Tutor)
        } else {
            model.addAttribute("tutor", false)
        }
        return "task"
    }

    @RequestMapping("/chats")
    fun textChatPage(
            @CookieValue(value = "user_id", required = false) userId: Long?,
            model: Model
    ): String {
        if (userId != null) {
            /* Conversations with this user_id */
            val person = personRepository!!.findById(userId).get()
            println("$person ${person.name} ${person.id}")
            val convs = conversationRepository!!.findAllByUser1OrUser2(person, person)
            println("Conversations $convs")
            val recentChatsMap = mutableMapOf<Person, List<Message>>()
            if (convs.isNotEmpty()) {
                for (conv in convs) {
                    var otherUser = conv.user1
                    if (conv.user1!!.id == userId) {
                        otherUser = conv.user2
                    }
                    println("Other user for $conv ${conv.id} ${conv.user1} ${conv.user2} is $otherUser")

                    var messages = messageRepository!!.findByConversationOrderByTimeAsc(conv)
                    println("Before sorted $messages")
                    messages = messages.sortedBy { it.time }
                    println("After sorting $messages")
                    recentChatsMap[otherUser!!] = messages
                }
            }
            model.addAttribute("recentChatsMap", recentChatsMap)
            val emptyChatList = mutableListOf<Person>()
            if (person is Tutor) {
                addEmptyConversations(person.tutees!!, recentChatsMap.keys, emptyChatList)
            }
            if (person is Tutee) {
                addEmptyConversations(person.tutors!!, recentChatsMap.keys, emptyChatList)
            }
            model.addAttribute("emptyChatList", emptyChatList)
            model.addAttribute("person", person)
        }
        return "chats"
    }

    private fun addEmptyConversations(people: List<Person>, existingChatKeys: MutableSet<Person>, emptyChatList: MutableList<Person>) {
        for (person in people) {
            if (person !in existingChatKeys) {
                val user = personRepository!!.findById(person.id!!).get()
                emptyChatList.add(user)
            }
        }
    }

    @RequestMapping("/calls")
    fun videoCallPage(): String {
        return "calls"
    }

    @RequestMapping("/calls_page")
    fun videoCallPage2(): String {
        return "videoCallsPage/index"
    }

    @RequestMapping("/task")
    fun task(): String {
        return "task2"
    }


}