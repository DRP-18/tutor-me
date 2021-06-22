package imperial.drp.controller

import imperial.drp.dao.FileRepository
import imperial.drp.dao.PersonRepository
import imperial.drp.dao.TaskRepository
import imperial.drp.dto.PostResponseDto
import imperial.drp.entity.File
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.view.RedirectView
import java.util.*


@Controller
class FileController {
    @Autowired
    private val fileRepository: FileRepository? = null

    @Autowired
    private val personRepository: PersonRepository? = null

    @Autowired
    private val taskRepository: TaskRepository? = null

    @PostMapping("/upload")
    fun uploadFile(
            @CookieValue(value = "user_id") userId: Long,
            @RequestParam(value = "file") multipartFile: MultipartFile
    ): ResponseEntity<Long> {
        var userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            var user = userOpt.get()
            var dbFile = File(
                    multipartFile.originalFilename,
                    user,
                    Calendar.getInstance(),
                    multipartFile.bytes
            )
            dbFile = fileRepository!!.save(dbFile)
            return ResponseEntity(dbFile.id, HttpStatus.OK)
        }
        return ResponseEntity(null, HttpStatus.FORBIDDEN)
    }

    @PostMapping("/uploadtaskfile")
    fun uploadTaskFile(
            @CookieValue(value = "user_id") userId: Long,
            @RequestParam(value = "task_id") taskId: Long,
            @RequestParam(value = "file") multipartFile: MultipartFile
    ): RedirectView {
        var userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            var user = userOpt.get()
            var taskOpt = taskRepository!!.findById(taskId)
            if (taskOpt.isPresent) {
                var task = taskOpt.get()
                if (task.tutor == user || task.tutee == user) {
                    var dbFile = File(
                            multipartFile.originalFilename,
                            user,
                            Calendar.getInstance(),
                            multipartFile.bytes
                    )
                    dbFile = fileRepository!!.save(dbFile)
                    task.attachments!!.add(dbFile)
                    taskRepository.save(task)
                }
            }
        }
        val redirectView = RedirectView()
        redirectView.url = "/task/${taskId}"
        return redirectView
    }

    @GetMapping("/taskfiles")
    fun taskFiles(
            @CookieValue(value = "user_id") userId: Long,
            @RequestParam(value = "task_id") taskId: Long
    ): ResponseEntity<List<File>> {
        var userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            var user = userOpt.get()
            var taskOpt = taskRepository!!.findById(taskId)
            if (taskOpt.isPresent) {
                var task = taskOpt.get()
                if (task.tutor == user || task.tutee == user) {
                    return ResponseEntity(task.attachments, HttpStatus.OK)
                }
            }
        }
        return ResponseEntity(null, HttpStatus.FORBIDDEN)
    }

    @GetMapping(
            value = ["/file/{fileId}/{filename}"],
            produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun downloadFile(
            @PathVariable fileId: Long,
            @PathVariable filename: String
    ): ResponseEntity<Resource> {
        val fileOpt = fileRepository!!.findById(fileId)
        if (fileOpt.isPresent) {
            val file = fileOpt.get()
            if (file.filename == filename) {
                return ResponseEntity(ByteArrayResource(file.content!!), HttpStatus.OK)
            }
        }
        return ResponseEntity(null, HttpStatus.FORBIDDEN)
    }
}
