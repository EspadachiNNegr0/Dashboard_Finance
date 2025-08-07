package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Banco;
import com.shadow.dashboard.service.BancoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class TableController {

    @Autowired
    private BancoService bancoService;

    @PostMapping("/bancos")
    public String saveBanco(
            @RequestParam("nome") String nome,
            @RequestParam("descricao") String descricao) {

        // Verifica se os campos estão preenchidos corretamente
        if (nome == null || nome.trim().isEmpty() || descricao == null || descricao.trim().isEmpty()) {
            return "redirect:/Table?error=Campos obrigatórios não preenchidos!";
        }

        Banco banco = new Banco();
        banco.setNome(nome);
        banco.setDescricao(descricao);

        bancoService.saveBancoAndNotification(banco);

        return "redirect:/Table";
    }

}
