package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.repository.ClientRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ClientController {

    @Autowired
    private ClientRepository clienteRepository;


    @PostMapping("/clientes")
    public String adicionarCliente(Clientes cliente, RedirectAttributes redirectAttributes) {
        clienteRepository.save(cliente);
        redirectAttributes.addFlashAttribute("message", "Cliente adicionado com sucesso!");
        return "redirect:/Table"; // Redireciona para a tabela de clientes
    }
}

