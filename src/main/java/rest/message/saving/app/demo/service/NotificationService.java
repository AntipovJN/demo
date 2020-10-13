package rest.message.saving.app.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rest.message.saving.app.demo.entity.Notification;
import rest.message.saving.app.demo.repository.INotificationRepository;
import rest.message.saving.app.demo.service.util.BusinessLogic;

import java.util.List;

@Service
public class NotificationService {

    private INotificationRepository notificationRepository;

    @Autowired
    public NotificationService(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public void executeBusinessLogicAndUpdateNotificationStatus(Notification notification)
    {
        BusinessLogic.doSomeWorkOnNotification();
        notification.setDelivered(true);
        notificationRepository.save(notification);
    }

    public List<Notification> geNotificationsPage (int pageNumber, int messagesPerPage) {
        Pageable page = PageRequest.of(pageNumber, messagesPerPage, Sort.by("time").descending());
        return notificationRepository.findAll(page).getContent();
    }
}
