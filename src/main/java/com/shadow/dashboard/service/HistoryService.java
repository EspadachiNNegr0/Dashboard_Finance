package com.shadow.dashboard.service;

import com.shadow.dashboard.models.History;
import com.shadow.dashboard.models.Notification;
import com.shadow.dashboard.repository.HistoryRepository;
import com.shadow.dashboard.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class HistoryService {

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public void saveHistoryAndCreateNotification(History history) {
        // Salve o histórico
        historyRepository.save(history);

        // Criação da notificação
        createNotification(history);
    }

    private void createNotification(History history) {
        // Crie a mensagem de notificação
        String message = "Novo histórico para o cliente " + history.getCliente().getNome() + " com status " + history.getStatus();

        // Crie o objeto Notification
        Notification notification = new Notification();
        notification.setCliente(history.getCliente());
        notification.setMessage(message);

        // Salve a notificação no banco de dados
        notificationRepository.save(notification);
    }


    public String formatadorData(History history) {
        if (history != null && history.getCreated() != null) {
            Calendar calendars = Calendar.getInstance();
            calendars.setTime(history.getCreated());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(calendars.getTime());
        }
        return null;
    }

    // Método para calcular a data de pagamento
    public String calculadorDeMeses(History history) {
        if (history != null && history.getCreated() != null && history.getParcelamento() > 0) {
            // Obtém a data de criação (início)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(history.getCreated());

            // Adiciona o número de meses de acordo com o parcelamento
            calendar.add(Calendar.MONTH, history.getParcelamento());

            // Obtemos a nova data (dia de pagamento)
            Date dataPagamento = calendar.getTime();

            // Formatar a data no formato "dd MM yyyy"
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dataPagamento);  // Retorna a data formatada
        }
        return null;  // Se algum dado estiver faltando, retorna null
    }


}
