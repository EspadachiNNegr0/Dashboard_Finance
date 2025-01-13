package com.shadow.dashboard.service;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.models.Notification;
import com.shadow.dashboard.repository.ClientRepository;
import com.shadow.dashboard.repository.HistoricoRepository;
import com.shadow.dashboard.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class HistoricoService {

    @Autowired
    private HistoricoRepository historicoRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ClientRepository clienteRepository;

    public void saveHistoryAndCreateNotification(Historico historico) {
        // Salve o histórico
        historicoRepository.save(historico);

        // Criação da notificação
        createNotification(historico);
    }

    private void createNotification(Historico historico) {
        // Verifique se o ID do cliente existe
        if (historico.getCliente().getId() == null) {
            System.out.println("ID do cliente é nulo no objeto History.");
            return;
        }

        // Buscar o cliente no banco de dados
        Clientes cliente = clienteRepository.findById(historico.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para o ID: " + historico.getCliente().getId()));

        // Crie a mensagem de notificação
        String message = "Novo histórico para o cliente " + cliente.getNome() + " com status " + historico.getStatus();

        // Crie o objeto Notification
        Notification notification = new Notification();
        notification.setCliente(cliente); // Relacione a notificação ao cliente
        notification.setMessage(message);

        // Salve a notificação no banco de dados
        notificationRepository.save(notification);
    }


    public String formatadorData(Historico history) {
        if (history != null && history.getCreated() != null) {
            Calendar calendars = Calendar.getInstance();
            calendars.setTime(history.getCreated());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(calendars.getTime());
        }
        return null;
    }

    // Método para calcular a data de pagamento
    public String calculadorDeMeses(Historico history) {
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


    public void atualizeHistoryAndCreateNotification(Historico historico) {
        // Salve o histórico
        historicoRepository.save(historico);

        // Criação da notificação
        createNotificationatualizada(historico);
    }

    private void createNotificationatualizada(Historico historico) {
        if (historico.getCliente().getId() == null) {
            System.out.println("Cliente is null");
        }
        Clientes cliente = clienteRepository.findById(historico.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para o ID: " + historico.getCliente().getId()));

        String msg = "Message updated with success! " + historico.getCliente().getNome();

        Notification notification = new Notification();
        notification.setCliente(cliente);
        notification.setMessage(msg);
        notificationRepository.save(notification);
    }
}