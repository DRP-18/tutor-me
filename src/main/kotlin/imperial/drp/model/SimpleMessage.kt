package imperial.drp.model

import imperial.drp.entity.Message
import java.util.*

data class SimpleMessage(val message: String = "") {}

data class AllMessages(val messages: Map<Long, List<Message>>) {}

data class CallingMessage(val callee: String = "", val caller: String = "", val signal: SignalObject)

data class CallingMessageWithName(val callee: String = "", val caller: String = "", val callerName: String = "", val signal: SignalObject)

data class SignalObject(val type: String, val sdp: String)