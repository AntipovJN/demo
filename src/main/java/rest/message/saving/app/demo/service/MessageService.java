package rest.message.saving.app.demo.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import rest.message.saving.app.demo.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rest.message.saving.app.demo.entity.Notification;
import rest.message.saving.app.demo.repository.IMessageRepository;
import rest.message.saving.app.demo.service.util.BusinessLogic;

import java.util.List;

@Service
public class MessageService {

    private IMessageRepository messageRepository;
    private NotificationService notificationService;

    @Autowired
    public MessageService(IMessageRepository messageRepository, NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.notificationService = notificationService;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public Notification saveMessageAndExecuteBusinessLogic(Message message) {
        messageRepository.save(message);
        BusinessLogic.doSomeWorkOnCommentCreation();

        Notification notification = new Notification();
        notification.setMessage(message);
        notificationService.save(notification);
        return notification;
    }

    public List<Message> getMessages(int pageNumber, int messagesPerPage) {
        Pageable page = PageRequest.of(pageNumber, messagesPerPage, Sort.by("time").descending());

        return messageRepository.findAll(page).getContent();
    }

}

