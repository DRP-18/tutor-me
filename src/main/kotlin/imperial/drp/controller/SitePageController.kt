package imperial.drp.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class SitePageController {

    @RequestMapping("/")
    fun homePage() : String {
        return "index"
    }

    @RequestMapping("/videoCall")
    fun videoCallPage() : String {
        return "videoCall"
    }

    @RequestMapping("/voiceCall")
    fun voiceCallPage() : String {
        return "voiceCall"
    }
    @RequestMapping("/textChat")
    fun textChatPage() : String {
        return "textChat"
    }




}