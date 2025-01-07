package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.models.History;
import com.shadow.dashboard.repository.ClientRepository;
import com.shadow.dashboard.repository.HistoryRepository;
import com.shadow.dashboard.service.ClientService;
import com.shadow.dashboard.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ClientService clientService;

    @GetMapping("/Table")
    public ModelAndView table() {
        ModelAndView mv = new ModelAndView("table");
        List<Clientes> clientes = clientRepository.findAll();
        List<History> historias = historyRepository.findAll();

        // Limitar o número de clientes a 5
        if (clientes.size() > 5) {
            clientes = clientes.subList(0, 5);
        }

        // Criando um mapa para armazenar o priceTotal de cada cliente
        Map<Long, Double> priceTotals = new HashMap<>();
        for (Clientes cliente : clientes) {
            Double priceTotal = clientService.calcularPriceTotal(cliente);
            priceTotals.put(cliente.getId(), priceTotal);  // Adicionando ao mapa
        }

        Map<Long, Object> dataformatada = new HashMap<>();
        for (History historia : historias) {
            String dataforma = historyService.formatadorData(historia);
            dataformatada.put(historia.getId(), dataforma);
        }

        // Criando um mapa para armazenar a data de pagamento de cada History
        // Passando as datas de pagamento já formatadas
        Map<Long, String> dataDePagamentoMapFormatada = new HashMap<>();
        for (History historia : historias) {
            String dataDePagamento = historyService.calculadorDeMeses(historia);  // Agora retorna uma string
            dataDePagamentoMapFormatada.put(historia.getId(), dataDePagamento);  // Armazenando no mapa
        }

        System.out.println("Clientes: " + clientes);
        System.out.println("Price Totals: " + priceTotals);
        System.out.println("Data de Pagamento Map: " + dataDePagamentoMapFormatada);


        // Passando os dados para a visão
        mv.addObject("priceTotals", priceTotals);
        mv.addObject("dataformatada", dataformatada);
        mv.addObject("clientes", clientes);
        mv.addObject("historias", historias);
        mv.addObject("dataDePagamentoMap", dataDePagamentoMapFormatada); // Passando as datas formatadas
        // Adicionando as datas de pagamento

        return mv;

    }

    @GetMapping("/index")
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView("index");
        List<Clientes> clientes = clientRepository.findAll();
        List<History> historias = historyRepository.findAll();

        // Limitar o número de clientes a 5
        if (clientes.size() > 5) {
            clientes = clientes.subList(0, 5);
        }

        // Criando um mapa para armazenar o priceTotal de cada cliente
        Map<Long, Double> priceTotals = new HashMap<>();
        for (Clientes cliente : clientes) {
            Double priceTotal = clientService.calcularPriceTotal(cliente);
            priceTotals.put(cliente.getId(), priceTotal);  // Adicionando ao mapa
        }

        // Passando os dados para a visão
        mv.addObject("priceTotals", priceTotals);
        mv.addObject("clientes", clientes);
        mv.addObject("historias", historias);

        return mv;
    }
}