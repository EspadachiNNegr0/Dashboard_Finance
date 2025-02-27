package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
        List<Notification> notifications = notificationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed()) // 🔥 Ordena pela data mais recente primeiro
                .collect(Collectors.toList());
        int totalNotify = notificationRepository.findAll().size();
        List<Socios> socios = sociosRepository.findAll();
        List<Clientes> clientes = clientRepository.findAll();
        List<Historico> historicos = historicoRepository.findAll();


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
        Long quantidadeNaoPagos = historicoRepository.countEmprestimosNaoPagos();
        Double totalPriceDosHistoricos = historicoRepository.sumTotalPrice();
        Long totalClientes = clientRepository.countClientes();

        // Logs para depuração
        System.out.println("✅ Total Pago: " + totalPago);
        System.out.println("⚠️ Total Pendente: " + totalPendente);
        System.out.println("❌ Total Atrasado: " + totalAtrasado);
        System.out.println("📊 Meses: " + meses);
        System.out.println("📊 Valores Mensais: " + valoresMensais);

        // Adicionando ao modelo para o Thymeleaf
        model.addAttribute("anosDisponiveis", anosDisponiveis);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("totalNotify", totalNotify);
        model.addAttribute("socios", socios);
        model.addAttribute("meses", meses);
        model.addAttribute("valoresMensais", valoresMensais);
        model.addAttribute("totalPago", totalPago);
        model.addAttribute("totalPendente", totalPendente);
        model.addAttribute("totalAtrasado", totalAtrasado);
        model.addAttribute("clientes", clientes);
        model.addAttribute("notifications", notifications);
        model.addAttribute("quantidadeNaoPagos", quantidadeNaoPagos);
        model.addAttribute("historicos", historicos);
        model.addAttribute("totalPriceDosHistoricos", totalPriceDosHistoricos);
        model.addAttribute("totalClientes", totalClientes);

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
