package imperial.drp.model

data class ChatMessage(val type: MessageType, val content: String, val sender: String, val time: String) {
}