package com.shadow.dashboard.service;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.models.Notification;
import com.shadow.dashboard.repository.ClientRepository;
import com.shadow.dashboard.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Método para calcular os juros sobre o preço base da história.
     * Aplica a porcentagem sobre o preço.
     */
    public double calcularJurosSobreHistoria(Historico historia) {
        // Verificação do preço e porcentagem para garantir cálculo seguro
        if (historia != null && historia.getPrice() > 0 && historia.getPercentage() > 0) {
            return historia.getPrice() * historia.getPercentage() / 100.0;
        }
        return 0.0;  // Retorna 0 caso não seja possível calcular
    }

    /**
     * Método para calcular o preço total, incluindo juros, e distribuído pelas parcelas
     * @param historia: objeto contendo o histórico do empréstimo
     */
    public double calcularPrecoTotalComJuros(Historico historia) {
        if (historia != null && historia.getParcelamento() > 0) {
            double valorJuros = calcularJurosSobreHistoria(historia);
            // Preço total: Preço base + juros, distribuído por parcelamento
            return (historia.getPrice() + valorJuros) / historia.getParcelamento();
        }
        return historia != null ? historia.getPrice() : 0.0; // Caso parcelamento seja 0 ou não exista
    }

    /**
     * Método para calcular o preço total (base + juros) sem distribuir em parcelas.
     * @param historia: objeto contendo o histórico do empréstimo
     */
    public double calcularPrecoTotalComJurosSemParcelar(Historico historia) {

        if (historia != null) {
            double valorJuros = calcularJurosSobreHistoria(historia);
            // Preço total: Preço base + juros, sem parcelamento
            return historia.getPrice() + valorJuros;
        }
        return 0.0;  // Caso o objeto história seja nulo
    }


    public Clientes saveClienteAndCreateNotification(Clientes cliente) {
        // Validações básicas
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O campo 'nome' é obrigatório.");
        }
        if (cliente.getCpf() == null || cliente.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("O campo 'CPF' é obrigatório.");
        }

        // Verificar se o CPF já existe
        Optional<Clientes> clienteExistente = clientRepository.findByCpf(cliente.getCpf());
        if (clienteExistente.isPresent()) {
            throw new RuntimeException("O CPF já está cadastrado no sistema.");
        }

        // Salvar cliente
        cliente = clientRepository.save(cliente);

        // Criar notificação
        criarNotificacao(cliente, "✅ Cliente "+ cliente.getNome() +" cadastrado com sucesso!");

        return cliente;
    }

    public void criarNotificacao(Clientes cliente, String mensagem) {
        if (cliente == null || cliente.getId() == null) return;

        Notification notification = new Notification();
        notification.setMessage(mensagem);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        notificationRepository.save(notification);
    }

}

