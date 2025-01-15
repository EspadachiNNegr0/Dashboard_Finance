package com.shadow.dashboard.service;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.repository.ClientRepository;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

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
}
