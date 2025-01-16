package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import com.shadow.dashboard.service.ClientService;
import com.shadow.dashboard.service.HistoricoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Controller
public class HistoricoController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private HistoricoRepository historicoRepository;

    @Autowired
    private HistoricoService historicoService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private SociosRepository sociosRepository;

    @Autowired
    private BancoRepository bancoRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @GetMapping("/search")
    public ModelAndView search(@RequestParam(value = "keyword", required = false) String keyword) {
        ModelAndView mv = new ModelAndView("search");

        List<Clientes> clientes = clientRepository.findAll();
        List<Historico> historias = historicoRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();

        // Mapeamento para total de preços com juros
        Map<Long, Double> priceTotalsPorParcelas = new HashMap<>();
        for (Historico historia : historias) {
            Double priceTotal = clientService.calcularPrecoTotalComJuros(historia);
            priceTotalsPorParcelas.put(historia.getId(), priceTotal);  // Usando a ID do cliente como chave
        }

        // Mapeamento para o valor total sem parcelamento
        Map<Long, Double> priceTotalSP = new HashMap<>();
        for (Historico historia : historias) {
            Double priceTotal = clientService.calcularPrecoTotalComJurosSemParcelar(historia);
            priceTotalSP.put(historia.getId(), priceTotal);
        }

        // Total de notificações
        int totalNotify = notifications.size();

        // Mapear datas formatadas e de pagamento
        Map<Long, Object> dataFormatada = new HashMap<>();
        Map<Long, String> dataDePagamentoMap = new HashMap<>();
        for (Historico historia : historias) {
            dataFormatada.put(historia.getId(), historicoService.formatadorData(historia));
            dataDePagamentoMap.put(historia.getId(), historicoService.calculadorDeMeses(historia));
        }


        List<Historico> listHistorico = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            listHistorico = historicoService.listAll(keyword);  // Garanta que a lógica de pesquisa esteja funcionando
        }


        System.out.println("Resultados encontrados: " + listHistorico.size());  // Exibir quantidade de resultados


        mv.addObject("totalNotify", totalNotify);
        mv.addObject("notifications", notifications);
        mv.addObject("priceTotals", priceTotalsPorParcelas);
        mv.addObject("priceTotalSP", priceTotalSP);
        mv.addObject("dataFormatada", dataFormatada);
        mv.addObject("clientes", clientes);
        mv.addObject("socios", socios);
        mv.addObject("historias", historias);
        mv.addObject("dataDePagamentoMap", dataDePagamentoMap);
        mv.addObject("bancos", bancos);
        mv.addObject("listHistorico", listHistorico);
        mv.addObject("keyword", keyword);  // Para mostrar o que foi pesquisado

        return mv;
    }

    @GetMapping("Table")
    public ModelAndView table() {
        ModelAndView mv = new ModelAndView("table");

        List<Clientes> clientes = clientRepository.findAll();
        List<Historico> historias = historicoRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();

// Agora 'createdDate' pode ser usado para manipulações, persistência ou enviado para o Thymeleaf


        // Mapeamento para total de preços com juros
        Map<Long, Double> priceTotalsPorParcelas = new HashMap<>();
        for (Historico historia : historias) {
            Double priceTotal = clientService.calcularPrecoTotalComJuros(historia);
            priceTotalsPorParcelas.put(historia.getId(), priceTotal);  // Usando a ID do cliente como chave
        }

        // Mapeamento para o valor total sem parcelamento
        Map<Long, Double> priceTotalSP = new HashMap<>();
        for (Historico historia : historias) {
            Double priceTotal = clientService.calcularPrecoTotalComJurosSemParcelar(historia);
            priceTotalSP.put(historia.getId(), priceTotal);
        }

        // Total de notificações
        int totalNotify = notifications.size();

        // Mapear datas formatadas e de pagamento
        Map<Long, Object> dataFormatada = new HashMap<>();
        Map<Long, String> dataDePagamentoMap = new HashMap<>();
        for (Historico historia : historias) {
            dataFormatada.put(historia.getId(), historicoService.formatadorData(historia));
            dataDePagamentoMap.put(historia.getId(), historicoService.calculadorDeMeses(historia));
        }

        // Passando os dados para a visão
        mv.addObject("totalNotify", totalNotify);
        mv.addObject("notifications", notifications);
        mv.addObject("priceTotals", priceTotalsPorParcelas);
        mv.addObject("priceTotalSP", priceTotalSP);
        mv.addObject("dataFormatada", dataFormatada);
        mv.addObject("clientes", clientes);
        mv.addObject("socios", socios);
        mv.addObject("historias", historias);
        mv.addObject("dataDePagamentoMap", dataDePagamentoMap);
        mv.addObject("bancos", bancos);

        return mv;
    }

}

