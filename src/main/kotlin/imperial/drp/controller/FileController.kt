package imperial.drp.controller

import imperial.drp.dao.FileRepository
import imperial.drp.entity.File
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.util.*


@Controller
@RequestMapping("/file")
class FileController {
    @Autowired
    private val fileRepository: FileRepository? = null

    @PostMapping
    fun uploadFile(@RequestParam(value = "file") multipartFile: MultipartFile): ResponseEntity<Long> {
        val dbFile = File()
        dbFile.filename = multipartFile.originalFilename
        dbFile.uploadTime = Calendar.getInstance()
        dbFile.content = multipartFile.bytes
        return ResponseEntity(fileRepository!!.save(dbFile).id, HttpStatus.OK)
    }

    @GetMapping(value = ["/{fileId}/{filename}"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun downloadFile(@PathVariable fileId: Long, @PathVariable filename: String): ResponseEntity<Resource> {
        val fileOpt = fileRepository!!.findById(fileId)
        if (fileOpt.isPresent) {
            val file = fileOpt.get()
            if (file.filename == filename) {
                return ResponseEntity(ByteArrayResource(file.content!!), HttpStatus.OK)
            }
        }
        return ResponseEntity(null, HttpStatus.NOT_FOUND)
    }
}
