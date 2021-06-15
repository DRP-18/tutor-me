package imperial.drp.entity

import java.util.*

interface FileWithoutContent {
    fun getFilename(): String
    fun getUploader(): Person
    fun getUploadTime(): Calendar
}