package imperial.drp.model


data class SimpleMessage(val message: String = "")

data class SessionMessage(val tutor: String = "",
                          val tutees: String = "",
                          val startTime: String = "",
                          val endTime: String = "")

data class NoteMessage(val content: String = "", val sender: String = "")

data class UserProfileMessage(val userId: String, val name: String,
                              val status: String, val avatar: String)