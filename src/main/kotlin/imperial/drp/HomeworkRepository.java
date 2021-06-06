package imperial.drp;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface HomeworkRepository extends CrudRepository<Homework, Long> {

  List<Homework> findByTutee(String tutee);

  Homework findById(long id);
}
