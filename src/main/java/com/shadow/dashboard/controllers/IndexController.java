package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.models.History;
import com.shadow.dashboard.repository.ClientRepository;
import com.shadow.dashboard.repository.HistoryRepository;
import com.shadow.dashboard.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ClientService clientService;

    @GetMapping("/Table")
    public String table() {
        return "table";
    }

    @GetMapping("/index")
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView("index");
        List<Clientes> clientes = clientRepository.findAll();
        List<History> historias = historyRepository.findAll();

        if (clientes.size() > 5) {
            clientes = clientes.subList(0, 5);
        }

        // Calcular o preço total para cada cliente
        for (Clientes cliente : clientes) {
            Double juros = clientService.calculatejuros(cliente);  // Passando o cliente como parâmetro
            mv.addObject("juros", juros);  // Adiciona o priceTotal no ModelAndView
        }

        mv.addObject("clientes", clientes); // Corrigido o nome do atributo
        mv.addObject("historias", historias);
        return mv;
    }

}
