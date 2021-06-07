package imperial.drp

import imperial.drp.dao.PersonRepository
import imperial.drp.dao.TaskRepository
import imperial.drp.entity.Person
import imperial.drp.entity.Task
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

            var jayme = Person("Jayme")
            personRepository.save(jayme)
            var mika = Person("Mika")
            personRepository.save(mika)
            var henry = Person("Henry")
            personRepository.save(henry)

            // save a few customers
            taskRepository.save(
                Task(
                    GregorianCalendar(2021, 5, 1),
                    jayme,
                    mika,
                    "Maths question 1"
                )
            )
            taskRepository.save(
                Task(
                    GregorianCalendar(2021, 5, 1),
                    jayme,
                    mika,
                    "Maths question 2"
                )
            )
            taskRepository.save(
                Task(
                    GregorianCalendar(2021, 5, 1),
                    jayme,
                    mika,
                    "Maths question 3"
                )
            )
            taskRepository.save(
                Task(
                    GregorianCalendar(2021, 5, 2),
                    jayme,
                    mika,
                    "Maths question 4"
                )
            )
            taskRepository.save(
                Task(
                    GregorianCalendar(2021, 5, 2),
                    jayme,
                    mika,
                    "Maths question 5"
                )
            )
            taskRepository.save(
                Task(
                    GregorianCalendar(2021, 5, 2),
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
