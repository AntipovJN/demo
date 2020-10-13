package rest.message.saving.app.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rest.message.saving.app.demo.entity.Notification;
import rest.message.saving.app.demo.service.NotificationService;

import java.util.List;

@RestController
public class NotificationController {

    private static final int MESSAGES_PER_PAGE = 10;
    private NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public List<Notification> getNotificationsPage(
            @RequestParam(defaultValue = "0", name = "page", required = false) int page) {
        return notificationService.geNotificationsPage(page, MESSAGES_PER_PAGE);
    }
}
