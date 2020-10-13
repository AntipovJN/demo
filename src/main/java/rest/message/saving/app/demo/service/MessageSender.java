package rest.message.saving.app.demo.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rest.message.saving.app.demo.entity.Message;
import rest.message.saving.app.demo.entity.Notification;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Log4j2
@Service
public class MessageSender {

    private MessageService messageService;
    private NotificationService notificationService;

    public MessageSender(MessageService messageService, NotificationService notificationService) {
        this.messageService = messageService;
        this.notificationService = notificationService;
    }

    @Async
    public Future<RuntimeException> saveAndNotifyAsync(Message message) {
        if (message.getTime() == null)
            message.setTime(new Timestamp(new Date().getTime()));

        try {
            Notification notification = messageService.saveMessageAndExecuteBusinessLogic(message);
            notificationService.executeBusinessLogicAndUpdateNotificationStatus(notification);
        }catch (RuntimeException e)
        {
            log.warn("message was not delivered");
            return CompletableFuture.completedFuture(e);
        }
        return CompletableFuture.completedFuture(null);

    }
}
