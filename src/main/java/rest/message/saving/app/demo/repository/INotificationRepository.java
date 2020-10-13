package rest.message.saving.app.demo.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import rest.message.saving.app.demo.entity.Notification;

@Repository
public interface INotificationRepository extends PagingAndSortingRepository<Notification, Long> {
}
