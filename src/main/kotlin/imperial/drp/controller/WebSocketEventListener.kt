package imperial.drp.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent


@Component
class WebSocketEventListener {

    @Autowired
    lateinit var sendingOperations : SimpMessageSendingOperations

    @EventListener
    fun handleWebSocketConnectListener(event: SessionConnectEvent) {
        println(event.source)
        println("connection established")
    }

    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
//        val headerAccessor = StompHeaderAccessor.wrap(event.message)
//        val username = headerAccessor.sessionAttributes?.get("username") as String
//        val chatMessage = ChatMessage(MessageType.DISCONNECT, "", username, "")
//        sendingOperations.convertAndSend("/topic/public", chatMessage)
    }
}