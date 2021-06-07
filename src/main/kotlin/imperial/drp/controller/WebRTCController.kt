//package imperial.drp.controller
//
//import imperial.drp.model.VideoMessage
//import org.springframework.messaging.handler.annotation.MessageMapping
//import org.springframework.messaging.handler.annotation.Payload
//import org.springframework.messaging.handler.annotation.SendTo
//import org.springframework.stereotype.Controller
//
//@Controller
//class WebRTCController {
//
//    @MessageMapping("/video.message")
//    @SendTo("/topic/video")
//    fun sendMessage(@Payload videoMessage: VideoMessage) : VideoMessage {
//        return videoMessage
//    }
//
//}