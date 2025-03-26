package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import com.shadow.dashboard.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/Relatorio")
public class RelatorioController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RelatorioFinanceiroRepository relatorioFinanceiroRepository;
    @Autowired
    private ParcelasRepository parcelasRepository;
    @Autowired
    private BancoRepository bancoRepository;
    @Autowired
    private SociosRepository sociosRepository;
    @Autowired
    private ClientesRepository clientesRepository;

    @GetMapping
    public String relatorio(Model model) {
        List<RelatorioFinanceiro> relatoriosFinanceiros = relatorioFinanceiroRepository.findAll();

        List<Notification> notifications = notificationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();
        int totalNotify = notifications.size();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> funcionarios = sociosRepository.findAll();
        List<Clientes> clientes = clientesRepository.findAll();

        model.addAttribute("totalNotify", totalNotify);
        model.addAttribute("notifications", notifications);
        model.addAttribute("relatoriosFinanceiros", relatoriosFinanceiros);
        model.addAttribute("bancos", bancos);
        model.addAttribute("funcionarios", funcionarios);
        model.addAttribute("clientes", clientes);

        return "relatorio";
    }

    @GetMapping("/gerar-pdf")
    public String gerarPdfView(Model model) {
        List<RelatorioFinanceiro> relatoriosFinanceiros = relatorioFinanceiroRepository.findAll();
        model.addAttribute("relatoriosFinanceiros", relatoriosFinanceiros);
        return "relatorio-pdf";
    }


}
