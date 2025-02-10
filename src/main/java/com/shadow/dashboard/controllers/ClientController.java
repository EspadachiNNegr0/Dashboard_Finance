package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.repository.ClientRepository;
import com.shadow.dashboard.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/clientes")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @PostMapping
    public ResponseEntity<Map<String, String>> adicionarCliente(@RequestBody Clientes cliente) {
        Map<String, String> response = new HashMap<>();

        // Verifica se o CPF já existe no banco de dados
        Optional<Clientes> clienteExistente = clientRepository.findByCpf(cliente.getCpf());
        if (clienteExistente.isPresent()) {
            response.put("message", "Erro: CPF já cadastrado no sistema.");
            return ResponseEntity.badRequest().body(response);
        }

        // Se o CPF não existir, salva o cliente e cria a notificação
        clientService.saveClienteAndCreateNotification(cliente);
        response.put("message", "Cliente cadastrado com sucesso!");

        return ResponseEntity.ok(response);
    }
}
