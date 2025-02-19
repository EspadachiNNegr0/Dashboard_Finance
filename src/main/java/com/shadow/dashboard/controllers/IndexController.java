package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import com.shadow.dashboard.service.ClientService;
import com.shadow.dashboard.service.HistoricoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class IndexController {

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
    private ParcelasRepository parcelasRepository;

    @Autowired
    private PagamentoLogRepository pagamentoLogRepository;

    // 🔹 Método para converter LocalDateTime para Date (para compatibilidade com Thymeleaf)
    private Date convertToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @GetMapping("/")
    public ModelAndView index(@RequestParam(value = "year", required = false) Integer selectedYear,
                              @RequestParam(value = "month", required = false) Integer selectedMonth) {
        ModelAndView mv = new ModelAndView("index");

        // Define o ano e mês atuais se nenhum for selecionado
        int currentYear = LocalDate.now().getYear();
        final int finalSelectedYear = (selectedYear == null) ? currentYear : selectedYear;
        final int finalSelectedMonth = (selectedMonth == null || selectedMonth == 0) ? 1 : selectedMonth;

        List<Integer> anosDisponiveis = parcelasRepository.findDistinctYears();
        List<Parcelas> todasParcelas = parcelasRepository.findParcelasByYear(finalSelectedYear);
        List<Notification> notifications = notificationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed()) // 🔥 Ordena pela data mais recente primeiro
                .collect(Collectors.toList());

        // Filtra apenas as parcelas do mês selecionado
        List<Parcelas> parcelasFiltradas = todasParcelas.stream()
                .filter(p -> {
                    if (p.getDataPagamento() == null) return false;
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(p.getDataPagamento());
                    return cal.get(Calendar.MONTH) + 1 == finalSelectedMonth;
                })
                .toList();

        // Contagem de clientes com histórico ativo baseado no pagamento de parcelas
        long totalClientes = historicoRepository.countClientesComHistoricoAtivoPorPagamento(finalSelectedYear, finalSelectedMonth);

        // Logs para depuração
        System.out.println("📊 Total de Clientes Ativos (com pagamento no período): " + totalClientes);

        // Informações auxiliares
        int totalNotify = notificationRepository.findAll().size();
        double somaDeEmprestimo = parcelasFiltradas.stream().mapToDouble(Parcelas::getValor).sum();
        long emprestimosAtivos = parcelasFiltradas.stream()
                .filter(parcela -> parcela.getHistorico() != null
                        && !"COMPLETE".equalsIgnoreCase(parcela.getHistorico().getStatus().toString()))
                .count();

        // Adiciona os atributos para a view
        mv.addObject("anosDisponiveis", anosDisponiveis);
        mv.addObject("selectedYear", finalSelectedYear);
        mv.addObject("selectedMonth", finalSelectedMonth);
        mv.addObject("parcelas", parcelasFiltradas);
        mv.addObject("totalNotify", totalNotify);
        mv.addObject("somaDeEmprestimo", somaDeEmprestimo);
        mv.addObject("emprestimosAtivos", emprestimosAtivos);
        mv.addObject("totalClientes", totalClientes);
        mv.addObject("notifications", notifications);

        return mv;
    }

    @PostMapping("/")
    public String saveEmprestimo(@ModelAttribute Historico historia,
                                 @RequestParam("bancoEntrada") Long bancoEntradaId,
                                 @RequestParam("bancoSaida") Long bancoSaidaId,
                                 RedirectAttributes redirectAttributes) {
        if (historia.getStatus() == null || (historia.getStatus() != Status.PAGO && historia.getStatus() != Status.PENDENTE)) {
            historia.setStatus(Status.PENDENTE);
        }

        // 🔹 Ensure that bancoEntrada and bancoSaida are not null
        Banco bancoEntrada = bancoRepository.findById(bancoEntradaId)
                .orElseThrow(() -> new RuntimeException("Banco de Entrada não encontrado"));

        Banco bancoSaida = bancoRepository.findById(bancoSaidaId)
                .orElseThrow(() -> new RuntimeException("Banco de Saída não encontrado"));

        // 🔹 Set the retrieved Banco objects in the Historico entity
        historia.setBancoEntrada(bancoEntrada);
        historia.setBancoSaida(bancoSaida);

        // Save the Historico entity
        historicoService.saveHistoryAndCreateNotification(historia);

        redirectAttributes.addFlashAttribute("message", "Empréstimo registrado com sucesso!");
        return "redirect:/Table";
    }


    @GetMapping("/histori/{id}")
    public String detalhesVenda(@PathVariable("id") Long id, Model model) {
        Historico histori = historicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado para o ID: " + id));

        // 🔹 Buscar parcelas associadas
        List<Parcelas> parcelasList = parcelasRepository.findByHistoricoId(id);



        model.addAttribute("histori", histori);
        model.addAttribute("parcelas", parcelasList);
        return "detalhe/detalhes";
    }

    @PostMapping("/pagar-parcela/{id}")
    public ResponseEntity<String> pagarParcela(@PathVariable Long id, @RequestParam double valorPago) {
        try {
            Parcelas parcela = parcelasRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Parcela não encontrada"));

            Historico historico = parcela.getHistorico();
            double valorMensal = historico.getValorMensal();

            // 🔹 Se o pagamento for menor que o valor mensal, criar uma nova parcela para o valor restante
            if (valorPago < valorMensal) {
                historicoService.criarNovaParcelaComValorRestante(parcela, valorPago, valorMensal);
            } else {
                parcela.setPagas(1);
                parcela.setStatus("PAGO");
                parcelasRepository.save(parcela);
            }

            // 🔹 Atualiza o status do histórico após pagamento
            historicoService.atualizarStatusHistorico(historico);

            // 🔹 Criar notificação sobre o pagamento
            historicoService.criarNotificacao(historico, valorPago, "Pagamento da Parcela");

            return ResponseEntity.ok("✅ Pagamento registrado com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Erro: " + e.getMessage());
        }
    }

    @PostMapping("/histori/{id}/pagar-mensal")
    public String pagarParcela(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Historico> optionalHistorico = historicoRepository.findById(id);

        if (optionalHistorico.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Histórico não encontrado.");
            return "redirect:/Table";
        }

        Historico historico = optionalHistorico.get();
        List<Parcelas> parcelasList = parcelasRepository.findByHistorico(historico);

        if (parcelasList.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nenhuma parcela encontrada.");
            return "redirect:/Table";
        }

        // 🔹 Seleciona a primeira parcela com pagas == 0 ou -1 (ATRASADA)
        Parcelas parcelaPendente = parcelasList.stream()
                .filter(p -> p.getPagas() == 0 || p.getPagas() == -1)
                .min(Comparator.comparing(Parcelas::getId))
                .orElse(null);

        if (parcelaPendente == null) {
            redirectAttributes.addFlashAttribute("error", "Todas as parcelas já foram pagas.");
            return "redirect:/histori/" + id;
        }

        // 🔹 Obtém o valor da parcela mensal
        double valorParcela = historico.getValorMensal();

        // 🔹 Registra o pagamento no log
        PagamentoLog log = new PagamentoLog();
        log.setHistorico(historico);
        log.setDataPagamento(LocalDateTime.now());
        log.setValorPago(valorParcela);

        try {
            pagamentoLogRepository.save(log);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao registrar pagamento.");
            return "redirect:/histori/" + id;
        }

        // 🔹 Atualiza a parcela como PAGA (1)
        parcelaPendente.setPagas(1);
        parcelasRepository.save(parcelaPendente);

        // 🔹 Verifica se todas as parcelas foram quitadas
        boolean todasPagas = parcelasList.stream().allMatch(p -> p.getPagas() == 1);

        if (todasPagas) {
            historico.setStatus(Status.PAGO);
        } else {
            // 🔹 Se ainda houver parcelas a pagar, o histórico volta para PENDENTE
            historico.setStatus(Status.PENDENTE);
        }

        historicoRepository.save(historico);

        // ✅ Criar Notificação do Pagamento Mensal
        historicoService.criarNotificacao(historico, valorParcela, "Pagamento Mensal");

        redirectAttributes.addFlashAttribute("success", "Pagamento mensal registrado com sucesso!");
        return "redirect:/histori/" + id;
    }

    @PostMapping("/histori/{id}/pagar-juros")
    public String pagarJuros(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<Historico> optionalHistorico = historicoRepository.findById(id);

        if (optionalHistorico.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Histórico não encontrado.");
            return "redirect:/Table";
        }

        Historico historico = optionalHistorico.get();
        List<Parcelas> parcelasList = parcelasRepository.findByHistorico(historico);

        if (parcelasList.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nenhuma parcela encontrada.");
            return "redirect:/Table";
        }

        // 🔹 Calcula o valor dos juros
        double valorJuros = historico.getValorTotal() * (historico.getPercentage() / 100.0);
        double valorMensal = historico.getValorMensal();

        // 🔹 Seleciona a primeira parcela pendente (ainda não paga)
        Parcelas primeiraParcelaPendente = parcelasList.stream()
                .filter(p -> p.getPagas() == 0)
                .min(Comparator.comparing(Parcelas::getId))
                .orElse(null);

        if (primeiraParcelaPendente == null) {
            redirectAttributes.addFlashAttribute("error", "Nenhuma parcela disponível para pagamento.");
            return "redirect:/histori/" + id;
        }

        // 🔹 Marca a parcela como PAGA (1), mesmo que tenha sido pago apenas o juros
        primeiraParcelaPendente.setPagas(1);
        parcelasRepository.save(primeiraParcelaPendente);

        // 🔹 Se o valor pago for menor que o valor da parcela, o restante será adicionado à próxima parcela
        double valorRestante = valorMensal - valorJuros;
        if (valorRestante > 0) {
            System.out.println("⚠️ Apenas os juros foram pagos. Criando nova parcela com o restante: R$ " + valorRestante);
            historicoService.criarNovaParcelaComValorRestante(primeiraParcelaPendente, valorJuros, valorMensal);
        }

        // 🔹 Atualiza o histórico com o novo valor total
        historico.setValorTotal(historico.getValorTotal() + valorJuros);
        historicoRepository.save(historico);

        // 🔹 Atualiza o novo valor mensal das parcelas pendentes
        historicoService.atualizarProximasParcelasEValorMensal(historico, parcelasList);

        // 🔹 Registra o pagamento no log
        PagamentoLog log = new PagamentoLog();
        log.setHistorico(historico);
        log.setValorPago(valorJuros);
        log.setDataPagamento(LocalDateTime.now());

        try {
            pagamentoLogRepository.save(log);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao registrar pagamento de juros.");
            return "redirect:/histori/" + id;
        }

        // ✅ Criar Notificação do Pagamento de Juros
        historicoService.criarNotificacao(historico, valorJuros, "Pagamento de Juros");

        redirectAttributes.addFlashAttribute("success", "Pagamento de juros registrado com sucesso! Novo valor de juros: R$ " + valorJuros);
        return "redirect:/histori/" + id;
    }


}
