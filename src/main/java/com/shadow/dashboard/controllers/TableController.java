package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Banco;
import com.shadow.dashboard.service.BancoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bancos") // Define um prefixo para os endpoints relacionados a bancos
public class TableController {

    @Autowired
    private BancoService bancoService;

    @PostMapping
    public ResponseEntity<?> saveBanco(@RequestBody Banco banco) {
        // Verifica se os campos estão preenchidos corretamente
        if (banco.getNome() == null || banco.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("O nome do banco não pode estar vazio!");
        }

        if (banco.getDescricao() == null || banco.getDescricao().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("A descrição do banco não pode estar vazia!");
        }

        // Salva o banco e gera notificação
        bancoService.saveBancoAndNotification(banco);

        return ResponseEntity.ok("Banco registrado com sucesso!");
    }
}
