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

        // Gerando os meses e valores de vendas mensais
        for (int mes = 1; mes <= 12; mes++) {
            String nomeMes = getMonthName(mes);
            double valorMensal = parcelasRepository.findTotalByMonthAndYear(mes, selectedYear)
                    .stream()
                    .mapToDouble(Parcelas::getValor)
                    .sum();

            meses.add(nomeMes);
            valoresMensais.add(valorMensal);
        }

        // Calculando totais por status
        double totalPago = parcelasRepository.findByStatusPago().stream().mapToDouble(Parcelas::getValor).sum();
        double totalPendente = parcelasRepository.findByStatusPendente().stream().mapToDouble(Parcelas::getValor).sum();
        double totalAtrasado = parcelasRepository.findByStatusAtrasado().stream().mapToDouble(Parcelas::getValor).sum();

        // Logs para depuração
        System.out.println("✅ Total Pago: " + totalPago);
        System.out.println("⚠️ Total Pendente: " + totalPendente);
        System.out.println("❌ Total Atrasado: " + totalAtrasado);
        System.out.println("📊 Meses: " + meses);
        System.out.println("📊 Valores Mensais: " + valoresMensais);

        // Adicionando ao modelo para o Thymeleaf
        model.addAttribute("anosDisponiveis", anosDisponiveis);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("meses", meses);
        model.addAttribute("valoresMensais", valoresMensais);
        model.addAttribute("totalPago", totalPago);
        model.addAttribute("totalPendente", totalPendente);
        model.addAttribute("totalAtrasado", totalAtrasado);
        model.addAttribute("clientes", clientes);
        model.addAttribute("notifications", notifications);

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
