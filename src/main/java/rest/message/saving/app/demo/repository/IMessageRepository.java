package rest.message.saving.app.demo.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import rest.message.saving.app.demo.entity.Message;
import org.springframework.stereotype.Repository;

@Repository
public interface IMessageRepository extends PagingAndSortingRepository<Message, Long> {

}
