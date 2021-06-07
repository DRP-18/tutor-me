package imperial.drp.controller

import imperial.drp.dao.TaskRepository
import imperial.drp.dao.PersonRepository
import imperial.drp.entity.Task
import imperial.drp.entity.Tutee
import imperial.drp.entity.Tutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@Controller
class DrpController {
    @Autowired
    private val taskRepository: TaskRepository? = null

    @Autowired
    private val personRepository: PersonRepository? = null

    @RequestMapping("/app")
    fun app(@CookieValue(value = "user_id", required = false) userId: Long?, model: Model): String {
        if (userId != null) {
            var userOpt = personRepository!!.findById(userId)
            if (userOpt.isPresent) {
                var user = userOpt.get()
                model.addAttribute("username", user.name!!)
                when (user) {
                    is Tutor -> {
                        val tasks =
                            taskRepository!!.findByTutor(personRepository!!.findByName(user.name!!)[0])
                        val tuteeTasksMap = HashMap<Tutee, MutableList<Task>>()
                        user.tutees!!.forEach {
                            tuteeTasksMap[it] = ArrayList()
                        }
                        tasks.forEach {
                            tuteeTasksMap[it.tutee!!]!!.add(it)
                        }
                        model.addAttribute("tuteeTasksMap", tuteeTasksMap)
                        return "tutorHome"
                    }
                    is Tutee -> {
                        val tasks =
                            taskRepository!!.findByTutee(personRepository!!.findByName(user.name!!)[0])
                        model.addAttribute("tasks", tasks)
                        return "tuteeHome"
                    }
                }
            }
        }
        return "login"
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
        }
        return "redirect"
    }

    @GetMapping("/signup")
    fun signupGet(
        response: HttpServletResponse,
        model: Model
    ): String {
        return "signup"
    }

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
                response.addCookie(cookie)
            }
        }
        return "redirect"
    }

    @RequestMapping("/logout")
    fun logout(response: HttpServletResponse, model: Model): String {
        val cookie = Cookie("user_id", null)
        cookie.maxAge = 0
        response.addCookie(cookie)
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
                var matchingPersons = personRepository!!.findByName(tuteeName)
                if (matchingPersons.isNotEmpty()) {
                    val person = matchingPersons[0]
                    if (person is Tutee) {
                        var newTutees = ArrayList<Tutee>(it.tutees)
                        newTutees.add(person)
                        it.tutees = newTutees
                        personRepository.save(it)
                    }
                }
            }
        }
        return "redirect"
    }

    @PostMapping("/deletetask")
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
}
