package com.shadow.dashboard.service;

import com.shadow.dashboard.models.Notification;
import com.shadow.dashboard.models.Socios;
import com.shadow.dashboard.repository.NotificationRepository;
import com.shadow.dashboard.repository.SociosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
public class SocioService {

    private static final Logger logger = Logger.getLogger(SocioService.class.getName());

    @Autowired
    private SociosRepository sociosRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public Socios saveAndNotify(Socios socio) {
        logger.info("Iniciando cadastro de funcionário.");
        validateSocio(socio);
        try {
            Socios savedSocio = sociosRepository.save(socio);
            logger.info("Funcionário salvo com sucesso: " + savedSocio.getName());
            createNotification(savedSocio);
            return savedSocio;
        } catch (Exception e) {
            logger.severe("Erro ao registrar funcionário: " + e.getMessage());
            throw new IllegalArgumentException("Erro ao registrar funcionário: " + e.getMessage(), e);
        }
    }

    private void validateSocio(Socios socio) {
        if (socio == null) {
            throw new IllegalArgumentException("Funcionário não pode ser nulo.");
        }
        if (socio.getName() == null || socio.getName().isBlank()) {
            throw new IllegalArgumentException("Nome do funcionário é obrigatório.");
        }
        if (socio.getAge() <= 0) {
            throw new IllegalArgumentException("Idade do funcionário deve ser maior que zero.");
        }
    }

    private void createNotification(Socios socio) {
        if (socio != null) {
            Notification notification = new Notification();
            notification.setMessage("📢 Novo Funcionário registrado: " + socio.getName());
            notification.setCreatedAt(LocalDateTime.now());

            // Agora salvando a notificação no banco
            notificationRepository.save(notification);
            logger.info("Notificação criada para o funcionário: " + socio.getName());
        }
    }
}
