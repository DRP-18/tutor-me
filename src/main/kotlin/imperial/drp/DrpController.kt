package imperial.drp

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
    private val homeworkRepository: HomeworkRepository? = null

    @RequestMapping("/app")
    fun app(@CookieValue(value = "username", required = false) username: String?, model: Model): String {
        model.addAttribute("username", username)
        if (username != null) {
            val homeworks = homeworkRepository!!.findByTutee(username)
            model.addAttribute("homeworks", homeworks)
        }
        return "app"
    }

    @PostMapping("/login")
    fun login(@RequestParam(value = "username") username: String, response: HttpServletResponse, model: Model): String {
        val cookie = Cookie("username", username)
        response.addCookie(cookie)
        return "redirect"
    }

    @RequestMapping("/logout")
    fun logout(response: HttpServletResponse, model: Model): String {
        val cookie = Cookie("username", null)
        cookie.maxAge = 0
        response.addCookie(cookie)
        return "redirect"
    }
}
