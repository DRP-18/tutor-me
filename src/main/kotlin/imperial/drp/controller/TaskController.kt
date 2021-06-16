package imperial.drp.controller

import imperial.drp.dao.*
import imperial.drp.dto.PostResponseDto
import imperial.drp.dto.TaskMapItemDto
import imperial.drp.entity.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import kotlin.collections.ArrayList

@Controller
class TaskController {
    @Autowired
    private val taskRepository: TaskRepository? = null

    @Autowired
    private val personRepository: PersonRepository? = null

//    @RequestMapping("/")
//    fun app(@CookieValue(value = "user_id", required = false) userId: Long?, model: Model): String {
//        if (userId != null) {
//            val userOpt = personRepository!!.findById(userId)
//            if (userOpt.isPresent) {
//                val user = userOpt.get()
//                model.addAttribute("username", user.name!!)
//                model.addAttribute("nowTime", Calendar.getInstance())
//                model.addAttribute("userType", getUserType(user))
//                when (user) {
//                    is Tutor -> {
//                        val tasks =
//                            taskRepository!!.findByTutorOrderByStartTimeAsc(
//                                personRepository.findByName(
//                                    user.name!!
//                                )[0]
//                            )
//                        val tuteeTasksMap = TreeMap<Tutee, MutableList<Task>>()
//                        user.tutees!!.forEach {
//                            tuteeTasksMap[it] = ArrayList()
//                        }
//                        tasks.forEach {
//                            tuteeTasksMap[it.tutee!!]!!.add(it)
//                        }
//                        model.addAttribute("tuteeTasksMap", tuteeTasksMap)
//                    }
//                    is Tutee -> {
//                        val tasks =
//                            taskRepository!!.findByTuteeOrderByStartTimeAsc(
//                                personRepository.findByName(
//                                    user.name!!
//                                )[0]
//                            )
//                        model.addAttribute("tasks", tasks)
//                    }
//                }
//            }
//        }
//        return "homepage"
//    }





    fun getUserType(person: Person): String {
        return (
                when (person) {
                    is Tutor -> {
                        "tutor"
                    }
                    is Tutee -> {
                        "tutee"
                    }
                    else -> {
                        ""
                    }
                })
    }

    @PostMapping("/login")
    fun login(
        @RequestParam(value = "username") username: String,
        response: HttpServletResponse,
        model: Model
    ): String {
        val matchingUsers = personRepository!!.findByName(username)
        if (matchingUsers.isNotEmpty()) {
            val userId = matchingUsers[0].id
            val cookie = Cookie("user_id", userId.toString())
            response.addCookie(cookie)
            val userType = getUserType(matchingUsers[0])
            response.addCookie(Cookie("user_type", userType))
        }
        return "redirect"
    }

    @PostMapping("/signup")
    fun signupPost(
        @RequestParam(value = "username") username: String,
        @RequestParam(value = "userType", required = false) userType: String?,
        response: HttpServletResponse,
        model: Model
    ): String {
        if (userType != null) {
            val matchingUsers = personRepository!!.findByName(username)
            if (matchingUsers.isEmpty()) {
                val user = when (userType) {
                    "tutor" -> {
                        Tutor(username, mutableListOf())
                    }
                    "tutee" -> {
                        Tutee(username)
                    }
                    else -> {
                        throw IllegalArgumentException("unknown user type")
                    }
                }
                personRepository.save(user)
                val userId = user.id
                val cookie = Cookie("user_id", userId.toString())
                val typeCookie = Cookie("user_type", userType)
                response.addCookie(cookie)
                response.addCookie(typeCookie)
            }
        }
        return "redirect"
    }

    @RequestMapping("/logout")
    fun logout(response: HttpServletResponse, model: Model): String {
        val cookie = Cookie("user_id", null)
        cookie.maxAge = 0
        response.addCookie(cookie)
        val cookie2 = Cookie("user_type", null)
        cookie2.maxAge = 0
        response.addCookie(cookie2)
        return "redirect"
    }

    @PostMapping("/addtutee")
    fun addtutee(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "tutee_name") tuteeName: String,
        response: HttpServletResponse
    ): ResponseEntity<PostResponseDto> {
        var userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            var user = userOpt.get()
            if (user is Tutor) {
                val matchingPersons = personRepository.findByName(tuteeName)
                if (matchingPersons.isNotEmpty()) {
                    val person = matchingPersons[0]
                    if (person is Tutee) {
                        user.tutees!!.add(person)
                        personRepository.save(user)
                        person.tutors!!.add(user)
                        personRepository.save(person)
                        return ResponseEntity(PostResponseDto(), HttpStatus.OK)
                    }
                    return ResponseEntity(
                        PostResponseDto(error = "the person you try to add isn't a tutee"),
                        HttpStatus.NOT_FOUND
                    )
                }
                return ResponseEntity(
                    PostResponseDto(error = "tutee doesn't exist"),
                    HttpStatus.NOT_FOUND
                )
            }
            return ResponseEntity(
                PostResponseDto(error = "you're not a tutor"),
                HttpStatus.NOT_FOUND
            )
        }
        return ResponseEntity(PostResponseDto(error = "you're not a user"), HttpStatus.NOT_FOUND)
    }

    @PostMapping("/addtask")
    fun addtask(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "start_time") startTime: String,
        @RequestParam(value = "end_time") endTime: String,
        @RequestParam(value = "content") content: String,
        @RequestParam(value = "tutee_id") tuteeId: Long,
        response: HttpServletResponse,
    ): ResponseEntity<PostResponseDto> {
        var userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            var user = userOpt.get()
            if (user is Tutor) {
                val tutee = personRepository.findById(tuteeId).get()
                if (tutee is Tutee) {
                    var sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                    try {
                        var startCalendar = GregorianCalendar()
                        startCalendar.time = sdf.parse(startTime)

                        var endCalendar = GregorianCalendar()
                        endCalendar.time = sdf.parse(endTime)

                        if (startCalendar <= endCalendar) {
                            taskRepository!!.save(
                                Task(
                                    startCalendar,
                                    endCalendar,
                                    user,
                                    tutee,
                                    content
                                )
                            )
                            return ResponseEntity(PostResponseDto(), HttpStatus.OK)
                        }
                        return ResponseEntity(
                            PostResponseDto(error = "the end time cannot be earlier than the start time"),
                            HttpStatus.NOT_FOUND
                        )
                    } catch (e: ParseException) {
                        return ResponseEntity(
                            PostResponseDto(error = "failed to parse the start time or the end time"),
                            HttpStatus.NOT_FOUND
                        )
                    }
                }
                return ResponseEntity(
                    PostResponseDto(error = "the person to assign task to is not a tutee"),
                    HttpStatus.NOT_FOUND
                )
            }
            return ResponseEntity(
                PostResponseDto(error = "you're not a tutor"),
                HttpStatus.NOT_FOUND
            )
        }
        return ResponseEntity(PostResponseDto(error = "you're not a user"), HttpStatus.NOT_FOUND)
    }

    @PostMapping("/deletetask")
    fun deletetask(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "task_id") taskId: Long,
        response: HttpServletResponse
    ): ResponseEntity<PostResponseDto> {
        var userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            var user = userOpt.get()
            if (user is Tutor) {
                var taskOpt = taskRepository!!.findById(taskId)
                if (taskOpt.isPresent) {
                    var task = taskOpt.get()
                    if (task.tutor == user) {
                        taskRepository.delete(task)
                        return ResponseEntity(PostResponseDto(), HttpStatus.OK)
                    }
                    return ResponseEntity(
                        PostResponseDto(error = "you don't own this task"),
                        HttpStatus.NOT_FOUND
                    )
                }
                return ResponseEntity(
                    PostResponseDto(error = "task doesn't exist"),
                    HttpStatus.NOT_FOUND
                )
            }
            return ResponseEntity(
                PostResponseDto(error = "you're not a tutor"),
                HttpStatus.NOT_FOUND
            )
        }
        return ResponseEntity(PostResponseDto(error = "you're not a user"), HttpStatus.NOT_FOUND)
    }

    private val s: String
        get() {
            return "/"
        }

    @PostMapping("/viewtask")
    @ResponseBody
    fun viewtask(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "tutee_name", required = false) tuteeName: String?,
        response: HttpServletResponse,
    ): String {
        val userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            val user = userOpt.get()
            when (user) {
                is Tutor -> {
                    var tasks =
                        taskRepository!!.findByTutorOrderByStartTimeAsc(
                            personRepository.findByName(
                                user.name!!
                            )[0]
                        )
                    print(tasks)
                    tasks = tasks.filter { it.tutee!!.name == tuteeName!! }
                    print(tasks)
                    return tasks.map { toJsonString(it) }.toString()

                }
                is Tutee -> {
                    val tasks =
                        taskRepository!!.findByTuteeOrderByStartTimeAsc(
                            personRepository.findByName(
                                user.name!!
                            )[0]
                        )

                    return tasks.map { toJsonString(it) }.toString()
                }
            }
        }
        return ""
    }

    @GetMapping("viewtutees")
    @ResponseBody
    fun viewtutees(
        @CookieValue(value = "user_id") userId: Long,
        response: HttpServletResponse,
    ): String {
        var resp = ""
        personRepository!!.findById(userId).ifPresent() { tutor ->
            if (tutor is Tutor) {
                resp = tutor.tutees!!.map { """{"name":"${it.name}","id":${it.id}}""" }.toString()
            }
        };

        return resp
    }

    @RequestMapping("/donetask")
    fun donetask(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "task_id") taskId: Long,
        response: HttpServletResponse,
        model: Model
    ): ResponseEntity<PostResponseDto> {
        var userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            var user = userOpt.get()
            var taskOpt = taskRepository!!.findById(taskId)
            if (taskOpt.isPresent) {
                var task = taskOpt.get()
                if (task.tutor == user || task.tutee == user) {
                    task.done = true
                    taskRepository.save(task)
                    return ResponseEntity(PostResponseDto(), HttpStatus.OK)
                }
                return ResponseEntity(
                    PostResponseDto(error = "you have not access to the task"),
                    HttpStatus.NOT_FOUND
                )
            }
            return ResponseEntity(
                PostResponseDto(error = "task doesn't exist"),
                HttpStatus.NOT_FOUND
            )
        }
        return ResponseEntity(PostResponseDto(error = "you're not a user"), HttpStatus.NOT_FOUND)
    }

    @GetMapping("/tutortasks")
    fun tutortasks(@CookieValue(value = "user_id") userId: Long): ResponseEntity<Map<Long, TaskMapItemDto>> {
        val userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            val user = userOpt.get()
            if (user is Tutor) {
                val tasks =
                    taskRepository!!.findByTutorOrderByStartTimeAsc(user)
                val tuteeTasksMap = TreeMap<Long, TaskMapItemDto>()
                user.tutees!!.forEach {
                    tuteeTasksMap[it.id!!] = TaskMapItemDto(it.name!!, ArrayList())
                }
                tasks.forEach {
                    tuteeTasksMap[it.tutee!!.id]?.tasks?.add(it)
                }
                return ResponseEntity(tuteeTasksMap, HttpStatus.OK)
            }
        }
        return ResponseEntity(null, HttpStatus.NOT_FOUND)
    }

    @GetMapping("/tuteetasks")
    fun tuteetasks(@CookieValue(value = "user_id") userId: Long): ResponseEntity<List<Task>> {
        val userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            val user = userOpt.get()
            if (user is Tutee) {
                val tasks =
                    taskRepository!!.findByTuteeOrderByStartTimeAsc(
                        personRepository.findByName(
                            user.name!!
                        )[0]
                    )
                return ResponseEntity(tasks, HttpStatus.OK)
            }
        }
        return ResponseEntity(null, HttpStatus.NOT_FOUND)
    }

    @GetMapping("/userinfo")
    fun userinfo(
        @CookieValue(value = "user_id", required = false) myId: Long?,
        @RequestParam(value = "user_id", required = false) otherId: Long?
    ): ResponseEntity<Person> {
        var userId = -1L
        if (otherId != null) {
            userId = otherId
        } else if (myId != null) {
            userId = myId
        }

        val userOpt = personRepository!!.findById(userId)
        return if (userOpt.isPresent) {
            ResponseEntity(userOpt.get(), HttpStatus.OK)
        } else {
            ResponseEntity(null, HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping("removemytutee")
    fun removemytutee(
        @CookieValue(value = "user_id") userId: Long,
        @RequestParam(value = "tutee_id") tuteeId: Long
    ): ResponseEntity<PostResponseDto> {
        val userOpt = personRepository!!.findById(userId)
        if (userOpt.isPresent) {
            val user = userOpt.get()
            if (user is Tutor) {
                var tuteeOpt = personRepository.findById(tuteeId)
                if (tuteeOpt.isPresent) {
                    var tutee = tuteeOpt.get()
                    user.tutees!!.remove(tutee)
                    personRepository.save(user)
                    return ResponseEntity(PostResponseDto(), HttpStatus.OK)
                }
                return ResponseEntity(
                    PostResponseDto(error = "the person is not your tutee"),
                    HttpStatus.NOT_FOUND
                )
            }
            return ResponseEntity(
                PostResponseDto(error = "you're not a tutor"),
                HttpStatus.NOT_FOUND
            )
        }
        return ResponseEntity(PostResponseDto(error = "you're not a user"), HttpStatus.NOT_FOUND)
    }
}
