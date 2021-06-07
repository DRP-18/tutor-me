package imperial.drp

import imperial.drp.dao.PersonRepository
import imperial.drp.dao.TaskRepository
import imperial.drp.entity.Person
import imperial.drp.entity.Task
import imperial.drp.entity.Tutee
import imperial.drp.entity.Tutor
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.util.*

@SpringBootApplication
class DrpApplication {
    private val log = LoggerFactory.getLogger(DrpApplication::class.java)

    @Bean
    fun initialize(
        personRepository: PersonRepository,
        taskRepository: TaskRepository
    ): CommandLineRunner? {
        return CommandLineRunner {
            personRepository.deleteAll()
            taskRepository.deleteAll()

            val mika = Tutee("Mika")
            personRepository.save(mika)
            val henry = Tutee("Henry")
            personRepository.save(henry)
            val jayme = Tutor("Jayme", listOf(mika, henry))
            personRepository.save(jayme)

            val start1 = GregorianCalendar(2021, 5, 7)
            val end1 = GregorianCalendar(2021, 5, 11)
            val start2 = GregorianCalendar(2021, 5, 14)
            val end2 = GregorianCalendar(2021, 5, 18)

            taskRepository.save(
                Task(
                    start1,
                    end1,
                    jayme,
                    mika,
                    "Maths question 1"
                )
            )
            taskRepository.save(
                Task(
                    start1,
                    end1,
                    jayme,
                    mika,
                    "Maths question 2"
                )
            )
            taskRepository.save(
                Task(
                    start1,
                    end1,
                    jayme,
                    mika,
                    "Maths question 3"
                )
            )
            taskRepository.save(
                Task(
                    start2,
                    end2,
                    jayme,
                    mika,
                    "Maths question 4"
                )
            )
            taskRepository.save(
                Task(
                    start2,
                    end2,
                    jayme,
                    mika,
                    "Maths question 5"
                )
            )
            taskRepository.save(
                Task(
                    start2,
                    end2,
                    jayme,
                    henry,
                    "English question 1"
                )
            )
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DrpApplication>(*args)
}
