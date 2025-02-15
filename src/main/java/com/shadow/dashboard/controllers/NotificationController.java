package com.shadow.dashboard.controllers;

import com.shadow.dashboard.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping("/notifications/clear")
    public String clearNotifications(Model model) {
        notificationRepository.deleteAll(); // 🔹 Apaga todas as notificações
        model.addAttribute("notifications", notificationRepository.findAll()); // 🔹 Atualiza a lista
        return "index"; // 🔹 Retorna a página principal SEM redirecionamento
    }
}
