package com.shadow.dashboard.service;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.models.History;
import com.shadow.dashboard.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // Método para calcular o preço total de uma venda (somando um preço de cada história)
    public double calculatejuros(Clientes cliente) {
        if (cliente.getHistory() != null && !cliente.getHistory().isEmpty()) {
            double totalPrice = 0.0;

            // Somar um preço de cada história
            for (History historia : cliente.getHistory()) {
                totalPrice += historia.getPrice();  // Somando o preço de cada história
            }

            // Agora usamos history.getPorcentagem() para aplicar a porcentagem corretamente
            double totalPorcentagem = 0.0;
            for (History historia : cliente.getHistory()) {
                totalPorcentagem += historia.getPercentage(); // Somando a porcentagem de cada história
            }

            // A porcentagem total será usada no preço final
            return totalPrice * totalPorcentagem / 100.0;
        }
        return 0.0;
    }

    // Método para calcular o preço total de uma venda (somando um preço de cada história)
    public double calcularPriceTotal(Clientes cliente) {

        if (calculatejuros(cliente) != 0.0) {
            double valorJuros = calculatejuros(cliente);

            double valortotal = 0.0;
            for (History historia : cliente.getHistory()) {
                valortotal += historia.getPrice();
            }
            return valortotal + valorJuros;
        }
        return 0.0;
    }
}
