package com.shadow.dashboard.service;

import com.shadow.dashboard.models.Banco;
import com.shadow.dashboard.models.Notification;
import com.shadow.dashboard.repository.NotificationRepository;
import com.shadow.dashboard.service.NotificationService;
import com.shadow.dashboard.repository.BancoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BancoService {

    @Autowired
    private BancoRepository bancoRepository;

    @Autowired
    private NotificationRepository notificationRepository; // Injetando o serviço de notificações

    public void saveBancoAndNotification(Banco banco) {
        if (banco.getNome() == null || banco.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do banco não pode estar vazio!");
        }

        bancoRepository.save(banco);
        System.out.println("✅ Banco salvo com sucesso: " + banco.getNome());

        // Exemplo de notificação, se necessário
        Notification notification = new Notification();
        notification.setMessage("Novo banco registrado: " + banco.getNome());
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
