package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.repository.HistoricoRepository;
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

    @GetMapping("/Analytics")
    public String showAnalytics(@RequestParam(value = "year", required = false) Integer selectedYear, Model model) {

        List<Integer> anosDisponiveis = historicoRepository.findDistinctYears();

        if (selectedYear == null) {
            selectedYear = LocalDate.now().getYear();
        }

        List<String> meses = new ArrayList<>();
        List<Double> valoresMensais = new ArrayList<>();

        // Calculando valores mensais
        for (int month = 1; month <= 12; month++) {
            List<Historico> historicos = historicoRepository.findByMonthAndYear(month, selectedYear);

            double totalMensal = historicos.stream()
                                           .mapToDouble(Historico::getValorMensal)
                                           .sum();

            meses.add(getMonthName(month));
            valoresMensais.add(totalMensal);
        }
        // Consultando total de empréstimos por sócio
        List<Object[]> sociosData = historicoRepository.sumLoansBySocio();
        List<String> sociosNames = new ArrayList<>();
        List<Double> sociosValues = new ArrayList<>();

        for (Object[] obj : sociosData) {
            sociosNames.add((String) obj[0]);
            sociosValues.add((Double) obj[1]);
        }

        model.addAttribute("anosDisponiveis", anosDisponiveis);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("meses", meses);
        model.addAttribute("valoresMensais", valoresMensais);
        model.addAttribute("sociosNames", sociosNames);
        model.addAttribute("sociosValues", sociosValues);

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
