package com.shadow.dashboard.service;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoricoService {

    @Autowired
    private HistoricoRepository historicoRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ClientRepository clienteRepository;

    public Historico saveHistoryAndCreateNotification(Historico historico) {
        // Verifique se os campos necessários estão preenchidos
        if (historico.getCreated() == null || historico.getParcelamento() <= 0) {
            throw new IllegalArgumentException("Os campos 'created' e 'parcelamento' são obrigatórios.");
        }

        // Calcule a data final (creationF) com base na lógica de parcelamento
        historico.setCreationF(calculaDataFinal(historico));

        // Salve o histórico no banco de dados
        historicoRepository.save(historico);

        // Criação da notificação
        createNotification(historico);

        return historico;
    }

    private Date calculaDataFinal(Historico historico) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(historico.getCreated());
        calendar.add(Calendar.MONTH, historico.getParcelamento());
        return calendar.getTime();
    }

    public List<Date> calculaDatasDePagamento(Historico historico) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(historico.getCreated());

        // Garante que a data inicial seja em janeiro
        if (calendar.get(Calendar.MONTH) != Calendar.JANUARY) {
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1); // Começa no 1º dia de janeiro
        }

        List<Date> datas = new ArrayList<>();
        for (int i = 0; i < historico.getParcelamento(); i++) {
            calendar.add(Calendar.MONTH, 1); // Adiciona um mês à data
            datas.add(calendar.getTime());
        }
        return datas;
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

    public String calculadorDeMeses(Historico history) {
        if (history != null && history.getCreated() != null && history.getParcelamento() > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(history.getCreated());
            calendar.add(Calendar.MONTH, history.getParcelamento());
            Date dataPagamento = calendar.getTime();
            history.setCreationF(dataPagamento); // Define no objeto `Historico`

            // Log para confirmar
            System.out.println("Data final calculada: " + dataPagamento);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dataPagamento);
        }
        return null;
    }


    public List<Historico> listAll(String keyword) {
        return historicoRepository.findAll(keyword);  // Chamando o repositório
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

    public double somaDeTodosOsEmprestimos(List<Historico> historias) {
        double soma = 0;
        for (Historico historico : historias) {
            soma = soma + historico.getPrice();
        }
        return soma;
    }

    public void pagarApenasJuros(Historico historico) {
        // Calcular o valor dos juros para esse mês
        double juros = calcularJuros(historico.getPrice(), historico.getPercentage());

        // Garantir que o valor de jurosPagos não seja nulo antes de somar
        double jurosPagosAtualizados = (historico.getJurosPagos() != null ? historico.getJurosPagos() : 0.0) + juros;

        // Atualizar o campo de juros pagos
        historico.setJurosPagos(jurosPagosAtualizados);

        // Registrar a data do pagamento de juros
        Date dataPagamento = new Date(); // Pode ser a data atual ou lógica para a data real do pagamento
        historico.getDatasPagamentos().add(dataPagamento);

        // Somar o valor dos juros ao preço para o próximo pagamento
        historico.setPrice(historico.getPrice() + juros);

        // Salvar no banco
        historicoRepository.save(historico);
    }


    private double calcularJuros(double price, int percentage) {
        // Calcular os juros baseados na porcentagem do histórico
        return price * (percentage / 100.0);
    }
}