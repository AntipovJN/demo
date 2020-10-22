package rest.message.saving.app.demo.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import rest.message.saving.app.demo.entity.Message;
import rest.message.saving.app.demo.service.MessageSender;
import rest.message.saving.app.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@Log4j2
public class MessageController {

    private static final int MESSAGES_PER_PAGE = 10;
    private MessageService messageService;
    private MessageSender messageSender;

    @Autowired
    public MessageController(MessageService messageService, MessageSender messageSender) {
        this.messageService = messageService;
        this.messageSender = messageSender;
    }

    @PostMapping("/message")
    public void saveMessasge(@RequestBody Message message) throws InterruptedException, ExecutionException {
        Future<RuntimeException> futureException = messageSender.saveAndNotifyAsync(message);
        try {
            futureException.get(100, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.info("Message save timeout");
        }
    }

    @GetMapping("/messages")
    public List<Message> getMessagesPage(
            @RequestParam(defaultValue = "0", required = false, name = "page") int page) {
        return messageService.getMessages(page, MESSAGES_PER_PAGE);
    }

    @GetMapping("/isready")
    public synchronized Boolean check () {
        return messageSender.isEmptyMessageCache();
    }
}
