package imperial.drp.controller

import imperial.drp.dao.ConversationRepository
import imperial.drp.dao.MessageRepository
import imperial.drp.dao.PersonRepository
import imperial.drp.dao.TaskRepository
import imperial.drp.dto.TaskMapItemDto
import imperial.drp.entity.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList

@Controller
class DrpController {
    @Autowired
    private val taskRepository: TaskRepository? = null

    @Autowired
    private val personRepository: PersonRepository? = null

    @Autowired
    private val conversationRepository: ConversationRepository? = null

    @Autowired
    private val messageRepository: MessageRepository? = null

//    @RequestMapping("/")
//    fun app(@CookieValue(value = "user_id", required = false) userId: Long?, model: Model): String {
//        if (userId != null) {
//            val userOpt = personRepository!!.findById(userId)
//            if (userOpt.isPresent) {
//                val user = userOpt.get()
//                model.addAttribute("username", user.name!!)
//                model.addAttribute("nowTime", Calendar.getInstance())
//                model.addAttribute("userType", getUserType(user))
//                when (user) {
//                    is Tutor -> {
//                        val tasks =
//                            taskRepository!!.findByTutorOrderByStartTimeAsc(
//                                personRepository.findByName(
//                                    user.name!!
//                                )[0]
//                            )
//                        val tuteeTasksMap = TreeMap<Tutee, MutableList<Task>>()
//                        user.tutees!!.forEach {
//                            tuteeTasksMap[it] = ArrayList()
//                        }
//                        tasks.forEach {
//                            tuteeTasksMap[it.tutee!!]!!.add(it)
//                        }
//                        model.addAttribute("tuteeTasksMap", tuteeTasksMap)
//                    }
//                    is Tutee -> {
//                        val tasks =
//                            taskRepository!!.findByTuteeOrderByStartTimeAsc(
//                                personRepository.findByName(
//                                    user.name!!
//                                )[0]
//                            )
//                        model.addAttribute("tasks", tasks)
//                    }
//                }
//            }
//        }
//        return "homepage"
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

    fun getUserType(person: Person): String {
        return (
                when (person) {
                    is Tutor -> {
                        "tutor"
                    }
                    is Tutee -> {
                        "tutee"
                    }
                    else -> {
                        ""
                    }
                })
    }

    @PostMapping("/login")
    fun login(
        @RequestParam(value = "username") username: String,
        response: HttpServletResponse,
        model: Model
    ): String {
        val matchingUsers = personRepository!!.findByName(username)
        if (matchingUsers.isNotEmpty()) {
            val userId = matchingUsers[0].id
            val cookie = Cookie("user_id", userId.toString())
            response.addCookie(cookie)
            val userType = getUserType(matchingUsers[0])
            response.addCookie(Cookie("user_type", userType))
        }
        return "redirect"
    }

//    @GetMapping("/signup")
//    fun signupGet(
//        response: HttpServletResponse,
//        model: Model
//    ): String {
//        return "signup"
//    }

    @PostMapping("/signup")
    fun signupPost(
        @RequestParam(value = "username") username: String,
        @RequestParam(value = "userType", required = false) userType: String?,
        response: HttpServletResponse,
        model: Model
    ): String {
        if (userType != null) {
            val matchingUsers = personRepository!!.findByName(username)
            if (matchingUsers.isEmpty()) {
                val user = when (userType) {
                    "tutor" -> {
                        Tutor(username, mutableListOf())
                    }
                    "tutee" -> {
                        Tutee(username)
                    }
                    else -> {
                        throw IllegalArgumentException("unknown user type")
                    }
                }
                personRepository.save(user)
                val userId = user.id
                val cookie = Cookie("user_id", userId.toString())
                val typeCookie = Cookie("user_type", userType)
                response.addCookie(cookie)
                response.addCookie(typeCookie)
            }
        }
        return "redirect"
    }

    @RequestMapping("/logout")
    fun logout(response: HttpServletResponse, model: Model): String {
        val cookie = Cookie("user_id", null)
        cookie.maxAge = 0
        response.addCookie(cookie)
        val cookie2 = Cookie("user_type", null)
        cookie2.maxAge = 0
        response.addCookie(cookie2)
        return "redirect"
    }

    @PostMapping("/addtutee")
    fun addtutee(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "tutee_name") tuteeName: String,
        response: HttpServletResponse,
        model: Model
    ): String {
        personRepository!!.findById(userId).ifPresent {
            if (it is Tutor) {
                val matchingPersons = personRepository.findByName(tuteeName)
                if (matchingPersons.isNotEmpty()) {
                    val person = matchingPersons[0]
                    if (person is Tutee) {
                        it.tutees!!.add(person)
                        personRepository.save(it)
                        person.tutors!!.add(it)
                        personRepository.save(person)
                    }
                }
            }
        }
        return "redirect"
    }

    @PostMapping("/addtask")
    fun addtask(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "start_time") startTime: String,
        @RequestParam(value = "end_time") endTime: String,
        @RequestParam(value = "content") content: String,
        @RequestParam(value = "tutee_id") tuteeId: Long,
        response: HttpServletResponse,
        model: Model
    ): String {
        personRepository!!.findById(userId).ifPresent { person ->
            if (person is Tutor) {
                val tutee = personRepository.findById(tuteeId).get()
                if (tutee is Tutee) {
                    var sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")

                    var startCalendar = GregorianCalendar()
                    startCalendar.time = sdf.parse(startTime)

                    var endCalendar = GregorianCalendar()
                    endCalendar.time = sdf.parse(endTime)

                    if (startCalendar <= endCalendar) {
                        taskRepository!!.save(
                            Task(
                                startCalendar,
                                endCalendar,
                                person,
                                tutee,
                                content
                            )
                        )
                    }
                }
            }
        }
        return "redirect"
    }

    @RequestMapping("/deletetask")
    fun deletetask(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "task_id") taskId: Long,
        response: HttpServletResponse,
        model: Model
    ): String {
        personRepository!!.findById(userId).ifPresent { person ->
            if (person is Tutor) {
                taskRepository!!.findById(taskId).ifPresent {
                    if (it.tutor == person) {
                        taskRepository.delete(it)
                    }
                }
            }
        }
        return "redirect"
    }

    private val s: String
        get() {
            return "/"
        }

    @PostMapping("/viewtask")
    @ResponseBody
    fun viewtask(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "tutee_name", required = false) tuteeName: String?,
        response: HttpServletResponse,
    ): String {
        val userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            val user = userOpt.get()
            when (user) {
                is Tutor -> {
                    var tasks =
                        taskRepository!!.findByTutorOrderByStartTimeAsc(
                            personRepository.findByName(
                                user.name!!
                            )[0]
                        )
                    print(tasks)
                    tasks = tasks.filter { it.tutee!!.name == tuteeName!! }
                    print(tasks)
                    return tasks.map { toJsonString(it) }.toString()

                }
                is Tutee -> {
                    val tasks =
                        taskRepository!!.findByTuteeOrderByStartTimeAsc(
                            personRepository.findByName(
                                user.name!!
                            )[0]
                        )

                    return tasks.map { toJsonString(it) }.toString()
                }
            }
        }
        return ""
    }

    @GetMapping("viewtutees")
    @ResponseBody
    fun viewtutees(
        @CookieValue(value = "user_id") userId: Long,
        response: HttpServletResponse,
    ): String {
        var resp = ""
        personRepository!!.findById(userId).ifPresent() { tutor ->
            if (tutor is Tutor) {
                resp = tutor.tutees!!.map { """{"name":"${it.name}","id":${it.id}}""" }.toString()
            }
        };

        return resp
    }

    @RequestMapping("/donetask")
    fun donetask(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "task_id") taskId: Long,
        response: HttpServletResponse,
        model: Model
    ): String {
        personRepository!!.findById(userId).ifPresent { person ->
            taskRepository!!.findById(taskId).ifPresent {
                if (it.tutor == person || it.tutee == person) {
                    it.done = true
                    taskRepository.save(it)
                }
            }
        }
        return "redirect"
    }

    @GetMapping("/tutortasks")
    fun tutortasks(@CookieValue(value = "user_id") userId: Long): ResponseEntity<Map<Long, TaskMapItemDto>> {
        val userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            val user = userOpt.get()
            if (user is Tutor) {
                val tasks =
                    taskRepository!!.findByTutorOrderByStartTimeAsc(user)
                val tuteeTasksMap = TreeMap<Long, TaskMapItemDto>()
                user.tutees!!.forEach {
                    tuteeTasksMap[it.id!!] = TaskMapItemDto(it.name!!, ArrayList())
                }
                tasks.forEach {
                    tuteeTasksMap[it.tutee!!.id]!!.tasks.add(it)
                }
                return ResponseEntity(tuteeTasksMap, HttpStatus.OK)
            }
        }
        return ResponseEntity(null, HttpStatus.NOT_FOUND)
    }

    @GetMapping("/tuteetasks")
    fun tuteetasks(@CookieValue(value = "user_id") userId: Long): ResponseEntity<List<Task>> {
        val userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            val user = userOpt.get()
            if (user is Tutee) {
                val tasks =
                    taskRepository!!.findByTuteeOrderByStartTimeAsc(
                        personRepository.findByName(
                            user.name!!
                        )[0]
                    )
                return ResponseEntity(tasks, HttpStatus.OK)
            }
        }
        return ResponseEntity(null, HttpStatus.NOT_FOUND)
    }

    @GetMapping("/userinfo")
    fun userinfo(@CookieValue(value = "user_id", required = false) myId: Long?,
                 @RequestParam(value = "user_id", required = false) otherId: Long?): ResponseEntity<Person> {
        var userId = -1L;
        if (otherId != null) {
            userId = otherId
        } else if (myId != null) {
            userId = myId
        }

        val userOpt = personRepository!!.findById(userId)
        return if (userOpt.isPresent) {
            ResponseEntity(userOpt.get(), HttpStatus.OK)
        } else {
            ResponseEntity(null, HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping("removemytutee")
    fun removemytutee(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "tutee_id") tuteeId: Long,
        model: Model
    ): String {
        val userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            val user = userOpt.get()
            if (user is Tutor) {
                personRepository.findById(tuteeId).ifPresent {
                    user.tutees!!.remove(it)
                    personRepository.save(user)
                }
            }
        }
        return "redirect"
    }
}