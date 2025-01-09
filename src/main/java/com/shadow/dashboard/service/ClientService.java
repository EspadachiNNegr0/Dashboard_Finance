package com.shadow.dashboard.service;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.models.History;
import com.shadow.dashboard.repository.ClientRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // Método para calcular os juros sobre o preço base da história
    public double calcularJurosSobreHistoria(History historia) {
        if (historia != null && historia.getPrice() > 0 && historia.getPercentage() > 0) {
            // Aplica a porcentagem de juros sobre o preço da história
            return historia.getPrice() * historia.getPercentage() / 100.0;
        }
        return 0.0;
    }

    // Método para calcular o preço total (preço base + juros) de uma história
    public double calcularPrecoTotalComJuros(History historia) {
        if (historia != null) {
            double valorJuros = calcularJurosSobreHistoria(historia);
            // Preço total (base + juros), dividindo pelos parcelamentos se existirem
            double valorPorParcelas = valorJuros + historia.getPrice();
            return valorPorParcelas / historia.getParcelamento();
        }
        return 0.0;
    }
}
