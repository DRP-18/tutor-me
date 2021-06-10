package imperial.drp.controller

import imperial.drp.dao.PersonRepository
import imperial.drp.dao.TaskRepository
import imperial.drp.entity.*
import org.springframework.beans.factory.annotation.Autowired
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

    @RequestMapping("/")
    fun app(@CookieValue(value = "user_id", required = false) userId: Long?, model: Model): String {
        if (userId != null) {
            val userOpt = personRepository!!.findById(userId)
            if (userOpt.isPresent) {
                val user = userOpt.get()
                model.addAttribute("username", user.name!!)
                model.addAttribute("nowTime", Calendar.getInstance())
                model.addAttribute("userType", getUserType(user))
                when (user) {
                    is Tutor -> {
                        val tasks =
                            taskRepository!!.findByTutorOrderByStartTimeAsc(
                                personRepository.findByName(
                                    user.name!!
                                )[0]
                            )
                        val tuteeTasksMap = TreeMap<Tutee, MutableList<Task>>()
                        user.tutees!!.forEach {
                            tuteeTasksMap[it] = ArrayList()
                        }
                        tasks.forEach {
                            tuteeTasksMap[it.tutee!!]!!.add(it)
                        }
                        model.addAttribute("tuteeTasksMap", tuteeTasksMap)
                    }
                    is Tutee -> {
                        val tasks =
                            taskRepository!!.findByTuteeOrderByStartTimeAsc(
                                personRepository.findByName(
                                    user.name!!
                                )[0]
                            )
                        model.addAttribute("tasks", tasks)
                    }
                }
            }
        }
        return "homepage"
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
                        "tutor"
                    }
                })
    }

    @PostMapping("/login")
    fun login(
        @RequestParam(value = "username") username: String,
        response: HttpServletResponse,
        model: Model
    ): String {
        var matchingUsers = personRepository!!.findByName(username)
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
            var matchingUsers = personRepository!!.findByName(username)
            if (matchingUsers.isEmpty()) {
                val user = when (userType) {
                    "tutor" -> {
                        Tutor(username, emptyList())
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
                        val newTutees = ArrayList<Tutee>(it.tutees)
                        newTutees.add(person)
                        it.tutees = newTutees
                        personRepository.save(it)
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
                var tutee = personRepository.findById(tuteeId).get()
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
        @CookieValue(value = "task_id") taskId: Long,
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
                    tasks = tasks.filter { it.tutee!!.name == tuteeName!! }
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
                resp = tutor.tutees!!.map { """{"name":"${it.name}","id":${it.id}}""" }.joinToString { it }
            }
        };

        return resp
    }
}