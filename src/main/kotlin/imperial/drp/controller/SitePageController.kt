package imperial.drp.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class SitePageController {

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

    @RequestMapping("/task/{task_id}")
    fun task(@PathVariable("task_id") taskId: Long): String {
        return "task"
    }
}