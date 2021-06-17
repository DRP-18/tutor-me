package imperial.drp.controller

import imperial.drp.dao.NoteRepository
import imperial.drp.entity.*
import imperial.drp.model.NoteMessage
import imperial.drp.model.SimpleMessage
import imperial.drp.model.UserDetail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Controller
import javax.transaction.Transactional

@Controller
class NotesController {

    @Autowired
    private val noteRepository: NoteRepository? = null

    @Autowired
    lateinit var messageSender: SimpMessageSendingOperations

    @Transactional
    @MessageMapping("/notes.addNote")
    fun saveNote(@Payload note: NoteMessage) {
        println("Adding Note message received ${note.content} ${note.sender}")
        val userId = note.sender.toLong()
        val noteToSave = Note(userId, note.content)
        noteRepository!!.save(noteToSave)

        // send all notes back via a specified channel
        messageSender.convertAndSend("/topic/notes-${userId}-newNoteId", SimpleMessage(noteToSave.id.toString()))
    }

    @Transactional
    @MessageMapping("/notes.getNotes")
    fun getAllNotes(@Payload message: SimpleMessage) {
        val userId = message.message.toLong()
        val listOfNotes = noteRepository!!.findByUserId(userId)

        messageSender.convertAndSend("/topic/notes-${userId}-receiveNotes", listOfNotes)
    }

    @Transactional
    @MessageMapping("/notes.deleteNote")
    fun deleteNote(@Payload message: UserDetail) {
        println("Delete Note message received: ${message.status}")
        val noteId = message.status.toLong()
        val noteToDelete = noteRepository!!.findById(noteId).get()

        noteRepository.delete(noteToDelete)
    }

    @Transactional
    @MessageMapping("/notes.editNote")
    fun editNote(@Payload note: NoteMessage) {

        val updatedText = note.content
        val noteId = note.sender.toLong()

        val noteInDB = noteRepository!!.findById(noteId).get()
        noteInDB.content = updatedText
        noteRepository.save(noteInDB)
    }

}