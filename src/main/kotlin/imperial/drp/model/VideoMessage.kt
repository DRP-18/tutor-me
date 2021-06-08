package imperial.drp.model

data class VideoMessage(val message: String = "") {}

data class CallingMessage(val callee: String = "", val caller: String = "", val signal: String = "")