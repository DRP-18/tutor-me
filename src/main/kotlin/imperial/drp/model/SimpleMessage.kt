package imperial.drp.model


data class SimpleMessage(val message: String = "")

data class SessionMessage(val tutor: String = "",
                          val tutees: String = "",
                          val dateTime: String = "",
                          val duration: String = "")

data class NoteMessage(val content: String = "", val sender: String = "")