package com.shadow.dashboard.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RelatorioController {

    @GetMapping("Relatorio")
    public String relatorio() {
        return "relatorio";
    }
}
