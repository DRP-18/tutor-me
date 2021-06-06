package imperial.drp

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
    fun initialize(repository: HomeworkRepository): CommandLineRunner? {
        return CommandLineRunner {
            repository.deleteAll()
            // save a few customers
            repository.save(
                Homework(
                    GregorianCalendar(2021, 5, 1),
                    "Jayme",
                    "Mika",
                    "Maths question 1"
                )
            )
            repository.save(
                Homework(
                    GregorianCalendar(2021, 5, 1),
                    "Jayme",
                    "Mika",
                    "Maths question 2"
                )
            )
            repository.save(
                Homework(
                    GregorianCalendar(2021, 5, 1),
                    "Jayme",
                    "Mika",
                    "Maths question 3"
                )
            )
            repository.save(
                Homework(
                    GregorianCalendar(2021, 5, 2),
                    "Jayme",
                    "Mika",
                    "Maths question 4"
                )
            )
            repository.save(
                Homework(
                    GregorianCalendar(2021, 5, 2),
                    "Jayme",
                    "Mika",
                    "Maths question 5"
                )
            )
            repository.save(
                Homework(
                    GregorianCalendar(2021, 5, 2),
                    "Jayme",
                    "Henry",
                    "English question 1"
                )
            )
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DrpApplication>(*args)
}
