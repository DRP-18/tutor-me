package imperial.drp.controller

import imperial.drp.dao.TaskRepository
import imperial.drp.dao.PersonRepository
import imperial.drp.entity.Task
import imperial.drp.entity.Tutee
import imperial.drp.entity.Tutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
                        tasks.forEach {
                            if (!tuteeTasksMap.containsKey(it.tutee)) {
                                tuteeTasksMap[it.tutee!!] = ArrayList()
                            }
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

    @RequestMapping("/logout")
    fun logout(response: HttpServletResponse, model: Model): String {
        val cookie = Cookie("user_id", null)
        cookie.maxAge = 0
        response.addCookie(cookie)
        return "redirect"
    }
}
