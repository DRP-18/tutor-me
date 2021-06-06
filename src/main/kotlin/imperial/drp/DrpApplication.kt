package imperial.drp

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class DrpApplication {
    private val log = LoggerFactory.getLogger(DrpApplication::class.java)

    @Bean
    fun initialize(repository: HomeworkRepository): CommandLineRunner? {
        return CommandLineRunner {
            repository.deleteAll()
            // save a few customers
            repository.save(Homework("Jayme", "Mika", "Maths question 1", "31/5/2021"))
            repository.save(Homework("Jayme", "Mika", "Maths question 2", "31/5/2021"))
            repository.save(Homework("Jayme", "Mika", "Maths question 3", "31/5/2021"))
            repository.save(Homework("Jayme", "Mika", "Maths question 4", "1/6/2021"))
            repository.save(Homework("Jayme", "Mika", "Maths question 5", "1/6/2021"))
            repository.save(Homework("Jayme", "Henry", "English question 1", "1/6/2021"))

            var homeworks = repository.findByTutee("Henry")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DrpApplication>(*args)
}
