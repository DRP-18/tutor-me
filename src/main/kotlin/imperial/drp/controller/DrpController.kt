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

    @RequestMapping("/")
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
        return "homepage"
    }

    @PostMapping("/login")
    fun login(
        @RequestParam(value = "username") username: String,
        response: HttpServletResponse,
        model: Model
    ): String {
        println("got login post request")
        var matchingUsers = personRepository!!.findByName(username)
        if (matchingUsers.isNotEmpty()) {
            val userId = matchingUsers[0].id
            val cookie = Cookie("user_id", userId.toString())
            response.addCookie(cookie)
            var userType = "tutor"
            if (matchingUsers[0] is Tutor) {
                userType = "tutor"
            } else if (matchingUsers[0] is Tutee) {
                userType = "tutee"
            }
            response.addCookie(Cookie("user_type", userType))
            println("cookie added")
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
}
