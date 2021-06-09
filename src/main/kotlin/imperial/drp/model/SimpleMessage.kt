package imperial.drp.model

import java.util.*

data class SimpleMessage(val message: String = "") {}

data class CallingMessage(val callee: String = "", val caller: String = "", val signal: SignalObject)

data class SignalObject(val type: String, val sdp: String)