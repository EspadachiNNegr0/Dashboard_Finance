package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.repository.HistoricoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AnalyticsController {

    @Autowired
    private HistoricoRepository historicoRepository;

    @GetMapping("/Analytics")
    public String showAnalytics(Model model) {
        int currentYear = 2024; // use LocalDate.now().getYear() para pegar o ano atual dinamicamente

        List<String> meses = new ArrayList<>();
        List<Double> valoresMensais = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            List<Historico> historicos = historicoRepository.findByMonthAndYear(month, currentYear);

            double totalMensal = historicos.stream()
                                           .mapToDouble(Historico::getValorMensal) 
                                           .sum();

            meses.add(getMonthName(month)); // Função para converter número do mês para nome
            valoresMensais.add(totalMensal);
        }

        model.addAttribute("meses", meses);
        model.addAttribute("valoresMensais", valoresMensais);

        return "analytics"; 
    }

    private String getMonthName(int month) {
        switch (month) {
            case 1: return "Janeiro";
            case 2: return "Fevereiro";
            case 3: return "Março";
            case 4: return "Abril";
            case 5: return "Maio";
            case 6: return "Junho";
            case 7: return "Julho";
            case 8: return "Agosto";
            case 9: return "Setembro";
            case 10: return "Outubro";
            case 11: return "Novembro";
            case 12: return "Dezembro";
            default: return "";
        }
    }
}
