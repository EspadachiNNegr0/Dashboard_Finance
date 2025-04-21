package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import com.shadow.dashboard.service.ClientService;
import com.shadow.dashboard.service.HistoricoService;
import com.shadow.dashboard.service.RelatorioService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
public class IndexController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private HistoricoRepository historicoRepository;

    @Autowired
    private HistoricoService historicoService;

    @Autowired
    private RelatorioEntradaRepository relatorioEntradaRepository;

    @Autowired
    private BancoRepository bancoRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ParcelasRepository parcelasRepository;

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private RelatorioFinanceiroRepository relatorioFinanceiroRepository;

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

    @PostMapping("/save")
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

            double taxaJuros = historia.getPercentage() / 100.0; // Converte para decimal
            int parcelas = historia.getParcelamento();
            double valorPrincipal = historia.getPrice();

            double montanteTotal = valorPrincipal * ((taxaJuros * Math.pow(1 + taxaJuros, parcelas)) / (Math.pow(1 + taxaJuros, parcelas) - 1)) * parcelas;

            // 🔹 Atualiza o valor total no histórico
            historia.setMontante(montanteTotal);

            // 🔹 Salvar o histórico no banco
            historicoService.saveHistoryAndCreateNotification(historia); // Não precisa atribuir o retorno

            // ✅ Criar Parcelas automaticamente
            historicoService.criarParcelas(historia); // Aqui você já usa o objeto `historia`

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
                .filter(p -> p.getPagas() == 0 || p.getPagas() == -1) // Permite PENDENTE e ATRASADA
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
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dataPagamento,
                               RedirectAttributes redirectAttributes) {
        try {
            // 🔹 Buscar a parcela
            Parcelas parcela = parcelasRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("❌ Parcela não encontrada"));

            // 🔹 Buscar histórico associado
            Historico historico = parcela.getHistorico();
            if (historico == null) {
                throw new RuntimeException("❌ Erro: A parcela ID " + parcela.getId() + " não possui um histórico associado.");
            }

            // 🔹 Buscar banco de entrada
            Banco bancoEntrada = bancoRepository.findById(bancoEntradaId)
                    .orElseThrow(() -> new RuntimeException("❌ Banco de Entrada não encontrado"));

            // 🔹 Validar o pagamento
            double juros = historicoService.calcularJuros(historico, parcela);

            double epsilon = 0.01; // margem de 1 centavo
            if (valorPago + epsilon < juros) {
                redirectAttributes.addFlashAttribute("error", "❌ O valor pago não pode ser menor que os juros!");
                return "redirect:/histori/" + historico.getId();
            }

            double montante = historico.getMontante();

            // 🔹 Atualizar a parcela com o pagamento
            historicoService.atualizarParcela(parcela, bancoEntrada, valorPago, dataPagamento);
            parcelasRepository.save(parcela); // ✅ Salvar a atualização no banco

            // 🔹 Verificar sobra e repassá-la
            double valorSobra = parcela.getValorSobra();
            if (valorSobra > 0) {
                historicoService.repassarSobra(parcela, historico);

                // 🔹 Se não houver mais parcelas pendentes e o empréstimo **ainda não foi quitado**, criar nova parcela
                boolean existeParcelaPendente = parcelasRepository.countByHistoricoAndStatus(historico, StatusParcela.PENDENTE) > 0;

                if (!existeParcelaPendente && historico.getStatus() != Status.PAGO) {
                    System.out.println("⚠️ Nenhuma parcela pendente encontrada. Criando nova parcela com a sobra...");
                    historicoService.criarNovaParcelaSeNecessario(historico, valorSobra, parcela);
                }
            }

            // 🔹 Verificar se o valor total pago já cobre o montante e quitar o empréstimo
            double amortizado = historicoService.calcularTotalPago(historico);
            if (amortizado >= montante) {
                historicoService.quitarEmprestimoSeNecessario(historico, amortizado);
                redirectAttributes.addFlashAttribute("success", "✅ Empréstimo quitado com sucesso!");
                return "redirect:/histori/" + historico.getId(); // ⛔ **RETORNA AQUI! NÃO CRIA MAIS PARCELAS**
            }

            // 🔹 Atualizar status do histórico
            historicoService.atualizarStatusHistorico(historico);

            // 🔹 Criar notificação do pagamento
            historicoService.criarNotificacao(historico, "💰 Pagamento de R$ " + valorPago + " realizado.");

            redirectAttributes.addFlashAttribute("success", "✅ Pagamento registrado com sucesso!");
            return "redirect:/histori/" + historico.getId();

        } catch (RuntimeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "❌ Erro: " + e.getMessage());
            return "redirect:/Table";
        }
    }




}