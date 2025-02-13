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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
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
    private SociosRepository sociosRepository;

    @Autowired
    private BancoRepository bancoRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ParcelasRepository parcelasRepository; // Adicionado para evitar erro

    @GetMapping("/Table")
    public ModelAndView table() {
        ModelAndView mv = new ModelAndView("table");

        List<Clientes> clientes = clientRepository.findAll();
        List<Historico> historias = historicoRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();

        // Total de notificações
        int totalNotify = notifications.size();

        // Evitar NullPointerException verificando cliente antes de acessar getNome()
        Map<Long, Double> priceTotalsPorParcelas = historias.stream()
                .collect(Collectors.toMap(
                        Historico::getId,
                        h -> clientService.calcularPrecoTotalComJuros(h),
                        (a, b) -> b
                ));

        // Passando os dados para a visão
        mv.addObject("totalNotify", totalNotify);
        mv.addObject("notifications", notifications);
        mv.addObject("clientes", clientes);
        mv.addObject("historias", historias);
        mv.addObject("socios", socios);
        mv.addObject("bancos", bancos);

        return mv;
    }

    @GetMapping("/search")
    public ModelAndView search(@RequestParam(value = "keyword", required = false) String keyword) {
        ModelAndView mv = new ModelAndView("search");

        List<Clientes> clientes = clientRepository.findAll();
        List<Historico> historias = historicoRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();

        // Total de notificações
        int totalNotify = notifications.size();

        // Inicializa mapas para evitar erro ao acessar valores nulos
        Map<Long, Double> priceTotalsPorParcelas = new HashMap<>();
        Map<Long, Double> priceTotalSP = new HashMap<>();
        Map<Long, String> dataFormatada = new HashMap<>();
        Map<Long, String> dataDePagamentoMap = new HashMap<>();

        for (Historico historia : historias) {
            priceTotalsPorParcelas.put(historia.getId(), clientService.calcularPrecoTotalComJuros(historia));
            priceTotalSP.put(historia.getId(), clientService.calcularPrecoTotalComJurosSemParcelar(historia));
        }

        // Filtro utilizando contains e evitando null
        List<Historico> listHistorico = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            listHistorico = historias.stream()
                    .filter(h -> h.getCliente() != null && h.getCliente().getNome().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
        }

        System.out.println("Resultados encontrados: " + listHistorico.size());

        // Adiciona objetos ao modelo
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
        mv.addObject("keyword", keyword);

        return mv;
    }

    @GetMapping("/historico/{id}")
    public String exibirHistoricoCliente(@PathVariable("id") Long id, Model model) {
        Clientes cliente = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para o ID: " + id));

        List<Historico> historicos = historicoRepository.findByCliente(cliente);

        model.addAttribute("cliente", cliente);
        model.addAttribute("historicos", historicos);

        return "modalHis"; // Nome do arquivo modalHis.html dentro da pasta templates/detalhe/
    }

}
