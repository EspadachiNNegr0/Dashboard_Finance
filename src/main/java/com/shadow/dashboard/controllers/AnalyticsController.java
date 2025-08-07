package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/Analytics") // 🔥 Todas as rotas agora começam com "/Analytics"
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

    @Autowired
    private BancoRepository bancoRepository;

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
        List<Banco> bancos = bancoRepository.findAll();


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
        model.addAttribute("bancos", bancos);

        return "analytics";
    }


    private String getMonthName(int month) {
        String[] meses = {
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        };
        return (month >= 1 && month <= 12) ? meses[month - 1] : "";
    }

    // ================== EDITAR CLIENTE (Aceita POST) ==================
    @PostMapping("/clientes/editar")
    public String editarCliente(@ModelAttribute Clientes cliente) {
        if (cliente.getId() != null && clientRepository.existsById(cliente.getId())) {
            clientRepository.save(cliente);
        }
        return "redirect:/Analytics/Analytics"; // 🔥 Redireciona para o dashboard após edição
    }

    // ================== EXCLUIR CLIENTE (Aceita POST) ==================
    @PostMapping("/clientes/excluir")
    public String excluirCliente(@RequestParam Long id) {
        if (id != null && clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
        }
        return "redirect:/Analytics/Analytics"; // 🔥 Redireciona para o dashboard após exclusão
    }

    // ================== EDITAR FUNCIONÁRIO (Aceita POST) ==================
    @PostMapping("/funcionarios/editar")
    public String editarFuncionario(@ModelAttribute Socios funcionario) {
        if (funcionario.getId() != null && sociosRepository.existsById(funcionario.getId())) {
            sociosRepository.save(funcionario);
        }
        return "redirect:/Analytics/Analytics";  // 🔥 Redireciona para Analytics após edição
    }

    // ================== EXCLUIR FUNCIONÁRIO (Aceita POST) ==================
    @PostMapping("/funcionarios/excluir")
    public String excluirFuncionario(@RequestParam Long id) {
        if (id != null && sociosRepository.existsById(id)) {
            sociosRepository.deleteById(id);
        }
        return "redirect:/Analytics/Analytics";  // 🔥 Redireciona para Analytics após exclusão
    }

    // ================== EDITAR BANCO (Aceita POST) ==================
    @PostMapping("/bancos/editar")
    public String editarBanco(@ModelAttribute Banco banco) {
        if (banco.getId() != null && bancoRepository.existsById(banco.getId())) {
            bancoRepository.save(banco);
        }
        return "redirect:/Analytics/Analytics"; // 🔥 Redireciona para a lista de bancos após edição
    }

    // ================== EXCLUIR BANCO (Aceita POST) ==================
    @PostMapping("/bancos/excluir")
    public String excluirBanco(@RequestParam Long id) {
        if (id != null && bancoRepository.existsById(id)) {
            bancoRepository.deleteById(id);
        }
        return "redirect:/Analytics/Analytics"; // 🔥 Redireciona para a lista de bancos após exclusão
    }
}
