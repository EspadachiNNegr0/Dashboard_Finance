package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping("/adicionar")
    public String adicionarCliente(@ModelAttribute Clientes cliente, RedirectAttributes redirectAttributes) {
        try {
            // Salva o cliente no banco e cria uma notificação
            clientService.saveClienteAndCreateNotification(cliente);
            redirectAttributes.addFlashAttribute("success", "✅ Cliente"+ cliente.getNome() +" cadastrado com sucesso!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erro interno ao cadastrar cliente.");
        }

        // Redireciona para a mesma página para exibir as mensagens no modal
        return "redirect:/Table";
    }
}
