package imperial.drp.dao

import imperial.drp.entity.Conversation
import imperial.drp.entity.Person
import org.springframework.data.repository.CrudRepository

interface ConversationRepository : CrudRepository<Conversation, Long> {

//    fun findByUser(user:Person): List<Conversation> {
//        val convList = mutableListOf<Conversation>()
//        convList.addAll(findByUser1(user))
//        convList.addAll(findByUser2(user))
//        return convList
//    }

    fun findAllByUser1OrUser2(user1: Person, user2: Person): List<Conversation>
    fun findAllByUser1AndUser2(user1: Person, user2: Person): List<Conversation>

}