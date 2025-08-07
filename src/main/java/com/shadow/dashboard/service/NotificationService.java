package com.shadow.dashboard.service;

import com.shadow.dashboard.models.Notification;
import com.shadow.dashboard.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(String message) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }
    // Executa a cada 5 minutos (300000 ms)
    @Scheduled(fixedRate = 300000)
    public void deleteOldNotifications() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(5);
        List<Notification> antigas = notificationRepository.findByCreatedAtBefore(limite);
        if (!antigas.isEmpty()) {
            notificationRepository.deleteAll(antigas);
            System.out.println("Notificações antigas apagadas: " + antigas.size());
        }
    }
}
