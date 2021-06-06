package imperial.drp

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class DrpController {
    @Autowired
    private val homeworkRepository: HomeworkRepository? = null
    @RequestMapping("/app")
    fun app(@CookieValue(value = "user", required = false) user: String, model: Model): String {
        model.addAttribute("user", user)
        val homeworks = homeworkRepository!!.findByTutee(user)
        model.addAttribute("homeworks", homeworks)
        return "app"
    }
}
