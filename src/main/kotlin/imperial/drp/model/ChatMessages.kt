package imperial.drp.model

data class UserDetail(val name: String = "", val status: String = "")

data class ChatMessage(val content: String = "",
                       val sender: String = "",
                       val recipient: String = "",
                       val time: String = "")