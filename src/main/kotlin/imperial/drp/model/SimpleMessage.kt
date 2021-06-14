package imperial.drp.model

import imperial.drp.entity.Message
import java.util.*

data class SimpleMessage(val message: String = "")

data class UserDetail(val name: String = "", val status: String = "")

data class ChatMessage(val content: String = "", val sender: String = "", val recipient: String = "", val time: String = "")

data class NoteMessage(val content: String = "", val sender: String = "")

data class CallingMessage(val callee: String = "", val caller: String = "", val signal: SignalObject)

data class CallingMessageWithName(val callee: String = "", val caller: String = "", val callerName: String = "", val signal: SignalObject)

data class SignalObject(val type: String, val sdp: String)