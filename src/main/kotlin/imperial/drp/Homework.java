package imperial.drp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Homework {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String tutor;
  private String tutee;
  private String content;
  private String date;

  protected Homework() {
  }
  
  public Homework(String tutor, String tutee, String content, String date) {
    this.tutor = tutor;
    this.tutee = tutee;
    this.content = content;
    this.date = date;
  }

  public String getTutor() {
    return tutor;
  }

  public String getTutee() {
    return tutee;
  }

  public String getContent() {
    return content;
  }

  public String getDate() {
    return date;
  }

  public Long getId() {
    return id;
  }
}
