package imperial.drp.dao

import imperial.drp.entity.File
import imperial.drp.entity.Task
import org.springframework.data.repository.CrudRepository

interface FileRepository : CrudRepository<File, Long> {

}