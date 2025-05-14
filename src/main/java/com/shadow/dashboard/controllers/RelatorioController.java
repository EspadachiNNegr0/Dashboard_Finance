package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    private ClientRepository clientRepository;

    @GetMapping
    public String relatorio(Model model) {
        List<RelatorioFinanceiro> relatoriosFinanceiros = relatorioFinanceiroRepository.findAll();

        List<Notification> notifications = notificationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();
        int totalNotify = notifications.size();
        List<Clientes> clientes = clientRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Clientes::getNome)) // 🔠 ordena alfabeticamente por nome
                .collect(Collectors.toList());

        List<Socios> funcionarios = sociosRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Socios::getName))
                .collect(Collectors.toList());

        List<Banco> bancos = bancoRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Banco::getNome))
                .collect(Collectors.toList());


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
