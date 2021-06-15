package imperial.drp.controller

import imperial.drp.dao.*
import imperial.drp.entity.Message
import imperial.drp.entity.Person
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class SitePageController {

    @Autowired
    private val taskRepository: TaskRepository? = null

    @Autowired
    private val personRepository: PersonRepository? = null

    @Autowired
    private val conversationRepository: ConversationRepository? = null

    @Autowired
    private val messageRepository: MessageRepository? = null

    @Autowired
    private val sessionRepository: SessionRepository? = null

    // @RequestMapping("/")
    // fun homePage() : String {
    //     return "homepage"
    // }

//        @RequestMapping("/calls_page")
//    fun videoCallPage(@CookieValue(value = "user_id", required = false) userId: String, response: HttpServletResponse) {
//        response.setHeader("Location", "http://localhost:3000/")
//        response.status = 302
//        response.addCookie(Cookie("user_id", userId))
//        println("redirected, this was the cookie $userId")
//    }


    @RequestMapping("/chats_page")
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
            if (convs.isNotEmpty()) {
                val recentChatsMap = mutableMapOf<Person, List<Message>>()

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
                model.addAttribute("recentChatsMap", recentChatsMap)
            }
        }
        return "chats_page"
    }


    @RequestMapping("/calls_page")
    fun videoCallPage(): String {
        return "videoCallsPage/index"
    }

    @RequestMapping("/task")
    fun task(): String {
        return "task"
    }


}