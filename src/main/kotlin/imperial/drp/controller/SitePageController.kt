package imperial.drp.controller

import imperial.drp.dao.PersonRepository
import imperial.drp.entity.Message
import imperial.drp.entity.Person
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@Controller
class SitePageController {

    // @RequestMapping("/")
    // fun homePage() : String {
    //     return "homepage"
    // }

    //    @RequestMapping("/calls_page")
//    fun videoCallPage(@CookieValue(value = "user_id", required = false) userId: String, response: HttpServletResponse) {
//        response.setHeader("Location", "http://localhost:3000/")
//        response.status = 302
//        response.addCookie(Cookie("user_id", userId))
//        println("redirected, this was the cookie $userId")
//    }


}