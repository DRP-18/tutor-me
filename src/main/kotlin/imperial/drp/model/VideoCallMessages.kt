package imperial.drp.model

data class CallingMessage(val callee: String = "",
                          val caller: String = "",
                          val signal: SignalObject)

data class CallingMessageWithName(val callee: String = "",
                                  val caller: String = "",
                                  val callerName: String = "",
                                  val signal: SignalObject)

data class SignalObject(val type: String, val sdp: String)