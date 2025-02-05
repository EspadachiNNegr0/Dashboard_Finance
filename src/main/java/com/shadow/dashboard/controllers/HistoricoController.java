package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import com.shadow.dashboard.service.ClientService;
import com.shadow.dashboard.service.HistoricoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private NotificationRepository notificationRepository;

    @GetMapping("/Table") // ✅ Agora a rota será reconhecida corretamente
    public ModelAndView table() {
        ModelAndView mv = new ModelAndView("table");

        List<Clientes> clientes = clientRepository.findAll();
        List<Historico> historias = historicoRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();

        // Total de notificações
        int totalNotify = notifications.size();

        // Mapear preços e datas
        Map<Long, Double> priceTotalsPorParcelas = historias.stream()
                .collect(Collectors.toMap(Historico::getId, clientService::calcularPrecoTotalComJuros));

        Map<Long, Double> priceTotalSP = historias.stream()
                .collect(Collectors.toMap(Historico::getId, clientService::calcularPrecoTotalComJurosSemParcelar));

        Map<Long, Object> dataFormatada = historias.stream()
                .collect(Collectors.toMap(Historico::getId, historicoService::formatadorData));

        Map<Long, String> dataDePagamentoMap = historias.stream()
                .collect(Collectors.toMap(Historico::getId, historicoService::calculadorDeMeses));

        // Passando os dados para a visão
        mv.addObject("totalNotify", totalNotify);
        mv.addObject("notifications", notifications);
        mv.addObject("priceTotals", priceTotalsPorParcelas);
        mv.addObject("priceTotalSP", priceTotalSP);
        mv.addObject("dataFormatada", dataFormatada);
        mv.addObject("clientes", clientes);
        mv.addObject("historias", historias);
        mv.addObject("dataDePagamentoMap", dataDePagamentoMap);

        return mv;
    }

}
