package rest.message.saving.app.demo.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rest.message.saving.app.demo.entity.Message;
import rest.message.saving.app.demo.entity.Notification;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

@Log4j2
@Service
public class MessageSender {

    private MessageService messageService;
    private NotificationService notificationService;
    private volatile ConcurrentLinkedQueue<Message> messagesCache;

    public MessageSender(MessageService messageService, NotificationService notificationService) {
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.messagesCache = new ConcurrentLinkedQueue<>();
    }

    @Async
    public Future<RuntimeException> saveAndNotifyAsync(Message message) {
        if (message.getTime() == null)
            message.setTime(new Timestamp(new Date().getTime()));
        try {
            messagesCache.add(message);
            Notification notification = messageService.saveMessageAndExecuteBusinessLogic(message);
            notificationService.executeBusinessLogicAndUpdateNotificationStatus(notification);
        } catch (RuntimeException e) {
            log.warn("message was not delivered");
            return CompletableFuture.completedFuture(e);
        } finally {
            messagesCache.remove(message);
        }

        return CompletableFuture.completedFuture(null);
    }

    public synchronized Boolean isEmptyMessageCache ()
    {
        return messagesCache.isEmpty();
    }
}
