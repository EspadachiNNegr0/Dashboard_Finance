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
                                 @RequestParam("bancoSaida") Long bancoSaidaId,
                                 RedirectAttributes redirectAttributes) {
        try {
            // 🔹 Validação para evitar valores nulos
            if (historia.getPrice() == null || historia.getPrice() <= 0) {
                redirectAttributes.addFlashAttribute("error", "O valor do empréstimo deve ser maior que zero!");
                return "redirect:/Table";
            }

            if (historia.getParcelamento() == null || historia.getParcelamento() <= 0) {
                redirectAttributes.addFlashAttribute("error", "O parcelamento deve ser maior que zero!");
                return "redirect:/Table";
            }

            if (historia.getPercentage() == null) {
                historia.setPercentage(0); // ✅ Define 0% como padrão se não informado
            }

            // 🔹 Definir status padrão caso não esteja definido
            if (historia.getStatus() == null) {
                historia.setStatus(Status.PENDENTE);
            }

            // 🔹 Buscar o banco de saída no banco de dados
            Banco bancoSaida = bancoRepository.findById(bancoSaidaId)
                    .orElseThrow(() -> new RuntimeException("Banco de Saída não encontrado"));

            historia.setBancoSaida(bancoSaida);

            // 🔹 Salvar o histórico no banco
            Historico historicoSalvo = historicoService.saveHistoryAndCreateNotification(historia);

            // ✅ Criar Parcelas automaticamente
            historicoService.criarParcela(historicoSalvo);

            redirectAttributes.addFlashAttribute("success", "Empréstimo registrado com sucesso!");
            return "redirect:/Table";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao salvar empréstimo: " + e.getMessage());
            return "redirect:/Table";
        }
    }


    @GetMapping("/histori/{id}")
    public String detalhesHistorico(@PathVariable Long id, Model model) {
        // 🔹 Buscar histórico pelo ID
        Historico histori = historicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado para o ID: " + id));

        // 🔹 Buscar todas as parcelas relacionadas
        List<Parcelas> parcelas = parcelasRepository.findByHistorico(histori);

        // 🔹 Buscar parcelas pendentes (para pagamento)
        Parcelas parcelaSelecionada = parcelas.stream()
                .filter(p -> p.getPagas() == 0) // Seleciona a primeira pendente
                .findFirst()
                .orElse(null);

        // 🔹 Buscar os bancos cadastrados
        List<Banco> bancos = bancoRepository.findAll();

        // ✅ Adicionar atributos ao modelo para o Thymeleaf
        model.addAttribute("histori", histori);
        model.addAttribute("parcelas", parcelas);
        model.addAttribute("parcelaSelecionada", parcelaSelecionada);
        model.addAttribute("bancos", bancos);

        return "detalhe/detalhes"; // Nome do arquivo HTML dentro de `templates/`
    }


    @PostMapping("/pagar-parcela/{id}")
    public String pagarParcela(@PathVariable Long id,
                               @RequestParam double valorPago,
                               @RequestParam Long bancoEntradaId,
                               RedirectAttributes redirectAttributes) {
        try {
            // 🔹 Buscar a parcela pelo ID
            Parcelas parcela = parcelasRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Parcela não encontrada"));

            Historico historico = parcela.getHistorico();
            double valorMensal = historico.getPrice() / historico.getParcelamento();
            double juros = (historico.getPercentage() / 100.0) * historico.getPrice(); // Calcula os juros

            // 🔹 Se o pagamento for menor que os juros, impede o pagamento
            if (valorPago < juros) {
                redirectAttributes.addFlashAttribute("error",
                        "❌ O valor pago não pode ser menor que os juros da parcela! Juros mínimo: "
                                + String.format("%.2f", juros));
                return "redirect:/histori/" + historico.getId();
            }

            // 🔹 Buscar o banco de entrada selecionado
            Banco bancoEntrada = bancoRepository.findById(bancoEntradaId)
                    .orElseThrow(() -> new RuntimeException("Banco de Entrada não encontrado"));

            // 🔹 Salvar o banco de entrada e valor pago na parcela
            parcela.setBancoEntrada(bancoEntrada.getNome());
            parcela.setValorPago(valorPago);
            parcela.setPagas(1); // ✅ Marca a parcela como PAGA
            parcela.setStatus(StatusParcela.PAGO);

            // 🔹 Calcular o valor restante (sobra)
            double valorSobra = valorMensal - valorPago;

            // ✅ Atualiza `valorSobra` da parcela paga
            parcela.setValorSobra(valorSobra);
            parcelasRepository.save(parcela); // Salvar a parcela atualizada

            // ✅ Agora adiciona esse `valorSobra` na próxima parcela
            historicoService.adicionarValorSobraNaProximaParcela(parcela, valorSobra);

            // 🔹 Atualiza o status do histórico após pagamento
            historicoService.atualizarStatusHistorico(historico);

            // 🔹 Criar notificação sobre o pagamento
            historicoService.criarNotificacao(historico, valorPago, "Pagamento da Parcela");

            redirectAttributes.addFlashAttribute("success", "✅ Pagamento registrado com sucesso!");
            return "redirect:/histori/" + historico.getId(); // ✅ Redireciona para a página de detalhes

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erro: " + e.getMessage());
            return "redirect:/histori/" + id;
        }
    }




}
