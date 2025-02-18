package com.shadow.dashboard.service;

import com.shadow.dashboard.models.Banco;
import com.shadow.dashboard.service.NotificationService;
import com.shadow.dashboard.repository.BancoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BancoService {

    @Autowired
    private BancoRepository bancoRepository;

    @Autowired
    private NotificationService notificationService; // Injetando o serviço de notificações

    public void saveBancoAndNotification(Banco banco) {
        // Salva o banco no banco de dados
        bancoRepository.save(banco);

        // Chama o método para criar a notificação
        notificationService.createNotification("Novo banco cadastrado: " + banco.getNome());
    }
}
