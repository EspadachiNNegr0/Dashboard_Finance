package com.shadow.dashboard.service;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.models.History;
import com.shadow.dashboard.models.Notification;
import com.shadow.dashboard.repository.ClientRepository;
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

    @Autowired
    private ClientRepository clienteRepository;

    public void saveHistoryAndCreateNotification(History history) {
        // Salve o histórico
        historyRepository.save(history);

        // Criação da notificação
        createNotification(history);
    }

    private void createNotification(History history) {
        // Verifique se o ID do cliente existe
        if (history.getCliente().getId() == null) {
            System.out.println("ID do cliente é nulo no objeto History.");
            return;
        }

        // Buscar o cliente no banco de dados
        Clientes cliente = clienteRepository.findById(history.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para o ID: " + history.getCliente().getId()));

        // Crie a mensagem de notificação
        String message = "Novo histórico para o cliente " + cliente.getNome() + " com status " + history.getStatus();

        // Crie o objeto Notification
        Notification notification = new Notification();
        notification.setCliente(cliente); // Relacione a notificação ao cliente
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
