package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.service.HistoricoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HistoricoController {

    @Autowired
    private HistoricoService historicoService;

    @GetMapping("/search")
    public ModelAndView search(@RequestParam(value = "keyword", required = false) String keyword) {
        ModelAndView mv = new ModelAndView("search");

        List<Historico> listHistorico = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            listHistorico = historicoService.listAll(keyword);  // Garanta que a lógica de pesquisa esteja funcionando
        }


        System.out.println("Resultados encontrados: " + listHistorico.size());  // Exibir quantidade de resultados


        mv.addObject("listHistorico", listHistorico);
        mv.addObject("keyword", keyword);  // Para mostrar o que foi pesquisado

        return mv;
    }

}

