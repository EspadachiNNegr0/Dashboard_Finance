package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.models.Notification;
import com.shadow.dashboard.models.Parcelas;
import com.shadow.dashboard.repository.ClientRepository;
import com.shadow.dashboard.repository.HistoricoRepository;
import com.shadow.dashboard.repository.NotificationRepository;
import com.shadow.dashboard.repository.ParcelasRepository;
import com.shadow.dashboard.repository.SociosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AnalyticsController {

    @Autowired
    private HistoricoRepository historicoRepository;

    @Autowired
    private ParcelasRepository parcelasRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SociosRepository sociosRepository;

    @GetMapping("/Analytics")
    public String showAnalytics(@RequestParam(value = "year", required = false) Integer selectedYear, Model model) {

        List<Integer> anosDisponiveis = parcelasRepository.findDistinctYears();

        if (selectedYear == null) {
            selectedYear = LocalDate.now().getYear();
        }

        List<String> meses = new ArrayList<>();
        List<Double> valoresMensais = new ArrayList<>();
        List<Notification> notifications = notificationRepository.findAll();
        List<Clientes> clientes = clientRepository.findAll();

        // Total de notificações
        int totalNotify = notifications.size();

        // Calculando valores mensais a partir das parcelas
        for (int month = 1; month <= 12; month++) {
            meses.add(getMonthName(month));
            valoresMensais.add(0.0);
        }

        // Busca os dados das parcelas e substitui os valores corretamente
        for (int month = 1; month <= 12; month++) {
            List<Parcelas> parcelas = parcelasRepository.findByMonthAndYear(month, selectedYear);

            if (!parcelas.isEmpty()) {
                double totalMensal = parcelas.stream()
                        .mapToDouble(Parcelas::getValor)
                        .sum();
                valoresMensais.set(month - 1, totalMensal); 
            }
        }


        double totalPago = parcelasRepository.findByStatusPago().stream().mapToDouble(Parcelas::getValor).sum();
        double totalAPagar = parcelasRepository.findByStatusAPagar().stream().mapToDouble(Parcelas::getValor).sum();
        double totalAtrasado = parcelasRepository.findByStatusAtrasado().stream().mapToDouble(Parcelas::getValor).sum();

        model.addAttribute("anosDisponiveis", anosDisponiveis);
        model.addAttribute("totalNotify", totalNotify);
        model.addAttribute("notifications", notifications);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("clientes", clientes);
        model.addAttribute("meses", meses);
        model.addAttribute("valoresMensais", valoresMensais);
        model.addAttribute("totalPago", totalPago);
        model.addAttribute("totalAPagar", totalAPagar);
        model.addAttribute("totalAtrasado", totalAtrasado);

        return "analytics";
    }

    private String getMonthName(int month) {
        String[] meses = {
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        };
        return (month >= 1 && month <= 12) ? meses[month - 1] : "";
    }
}
