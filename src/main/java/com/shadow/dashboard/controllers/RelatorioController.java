package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Notification;
import com.shadow.dashboard.models.Parcelas;
import com.shadow.dashboard.models.RelatorioFinanceiro;
import com.shadow.dashboard.repository.ParcelasRepository;
import com.shadow.dashboard.repository.RelatorioFinanceiroRepository;
import com.shadow.dashboard.service.RelatorioService;
import com.shadow.dashboard.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping
    public String relatorio(Model model) {
        List<RelatorioFinanceiro> relatoriosFinanceiros = relatorioFinanceiroRepository.findAll();

        List<Notification> notifications = notificationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();
        int totalNotify = notifications.size();

        model.addAttribute("totalNotify", totalNotify);
        model.addAttribute("notifications", notifications);
        model.addAttribute("relatoriosFinanceiros", relatoriosFinanceiros);

        return "relatorio";
    }

}
