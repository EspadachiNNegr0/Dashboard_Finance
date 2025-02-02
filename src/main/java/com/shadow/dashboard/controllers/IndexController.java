package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import com.shadow.dashboard.service.ClientService;
import com.shadow.dashboard.service.HistoricoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // Função para converter Date para LocalDate
    private LocalDate convertToLocalDate(Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate(); // 🔹 Correta conversão de SQL Date para LocalDate
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); // 🔹 Caso seja um Date normal
    }

    @GetMapping("/")
    public ModelAndView index(@RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "month", required = false, defaultValue = "0") int selectedMonth) {
        ModelAndView mv = new ModelAndView("index");

        // Buscando todos os dados
        List<Clientes> clientes = clientRepository.findAll();
        // Buscar todos os históricos
        List<Historico> historias = (status != null && !status.isEmpty())
                ? new ArrayList<>(historicoRepository.findByStatus(status)) // 🔹 Garante mutabilidade
                : new ArrayList<>(historicoRepository.findAll()); // 🔹 Garante mutabilidade


        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();

        String keyword = "Eduardo";
        List<Historico> listHistorico = historicoService.listAll(keyword);

        // Limitar o número de clientes a 5
        if (clientes.size() > 5) {
            clientes = clientes.subList(0, 5);
        }


        // Criar um mapa para armazenar as datas de pagamento filtradas
        Map<Long, List<Date>> mapaDatasPagamento = new HashMap<>();
        // 🔹 Aplicando a conversão para LocalDate antes de filtrar
        if (selectedMonth != 0) {
            historias = historias.stream()
                    .filter(h -> {
                        List<Date> datasPagamento = historicoService.calculaDatasDePagamento(h);
                        boolean temPagamentoNoMes = datasPagamento.stream()
                                .anyMatch(data -> convertToLocalDate(data).getMonthValue() == selectedMonth);
                        if (temPagamentoNoMes) {
                            mapaDatasPagamento.put(h.getId(), datasPagamento); // Adiciona apenas se o empréstimo tiver pagamento no mês
                        }
                        return temPagamentoNoMes;
                    })
                    .collect(Collectors.toList());
        } else {
            // Se for "Todos os meses", carregar normalmente todas as datas de pagamento
            for (Historico historico : historias) {
                mapaDatasPagamento.put(historico.getId(), historicoService.calculaDatasDePagamento(historico));
            }
        }

        // Criando os mapas de dados a serem passados para a view
        Map<Long, Double> priceTotalsPorParcelas = new HashMap<>();
        Map<Long, Double> priceTotalSP = new HashMap<>();
        Map<Long, Object> dataFormatada = new HashMap<>();
        Map<Long, String> dataDePagamentoMap = new HashMap<>();

        for (Historico historia : historias) {
            mapaDatasPagamento.put(historia.getId(), historicoService.calculaDatasDePagamento(historia));
            priceTotalsPorParcelas.put(historia.getId(), clientService.calcularPrecoTotalComJuros(historia));
            priceTotalSP.put(historia.getId(), clientService.calcularPrecoTotalComJurosSemParcelar(historia));
            dataFormatada.put(historia.getId(), historicoService.formatadorData(historia));
            dataDePagamentoMap.put(historia.getId(), historicoService.calculadorDeMeses(historia));
        }

        historias.sort(Comparator.comparing(Historico::getCreated).reversed());

        // Total de notificações
        int totalNotify = notifications.size();
        double somaDeEmprestimo = historicoService.somaDeTodosOsEmprestimos(historias);

        // Passando os dados para a visão
        mv.addObject("datasDePagamento", mapaDatasPagamento);
        mv.addObject("selectedMonth", selectedMonth);
        mv.addObject("totalNotify", totalNotify);
        mv.addObject("listHistorico", listHistorico);
        mv.addObject("notifications", notifications);
        mv.addObject("somaDeEmprestimo", somaDeEmprestimo);
        mv.addObject("priceTotals", priceTotalsPorParcelas);
        mv.addObject("priceTotalSP", priceTotalSP);
        mv.addObject("dataFormatada", dataFormatada);
        mv.addObject("clientes", clientes);
        mv.addObject("socios", socios);
        mv.addObject("historias", historias);
        mv.addObject("dataDePagamentoMap", dataDePagamentoMap);
        mv.addObject("bancos", bancos);

        return mv;
    }

    private int getMonthFromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1; // +1 porque os meses em Calendar são 0-indexados
    }

    @PostMapping("/")
    public String saveEmprestimo(@ModelAttribute Historico historia, RedirectAttributes redirectAttributes) {
        // Verifica se o status está vazio ou é diferente de COMPLETE e FAILED
        if (historia.getStatus() == null || (historia.getStatus() != Status.COMPLETE && historia.getStatus() != Status.FAILED)) {
            historia.setStatus(Status.PROCESSING); // Define o status como PROCESSING (ativo)
        }

        // Salva o histórico no banco de dados e cria a notificação
        historicoService.saveHistoryAndCreateNotification(historia);

        // Adiciona mensagem de sucesso
        redirectAttributes.addFlashAttribute("message", "Empréstimo registrado com sucesso!");

        // Redireciona para a página inicial
        return "redirect:/";
    }

    @GetMapping("/histori/{id}")
    public String detalhesVenda(@PathVariable("id") String id, Model model) {
        // Converter o id para long
        long historiId = Long.parseLong(id);

        // Buscar o histórico específico pelo ID
        Historico histori = historicoRepository.findById(historiId)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado para o ID: " + historiId));

        // Carregar outras listas no modelo
        List<Clientes> clientes = clientRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Historico> historico = historicoRepository.findAll();

        // Mapear datas formatadas
        Map<Long, Object> dataFormatada = new HashMap<>();
        dataFormatada.put(histori.getId(), historicoService.formatadorData(histori));

        // Calcular preços totais com juros
        Map<Long, Double> priceTotals = new HashMap<>();
        priceTotals.put(histori.getId(), clientService.calcularPrecoTotalComJuros(histori));

        // Calcular preço sem parcelar
        Map<Long, Double> priceTotalSP = new HashMap<>();
        priceTotalSP.put(histori.getId(), clientService.calcularPrecoTotalComJurosSemParcelar(histori));

        // Mapear data de pagamento
        Map<Long, String> dataDePagamentoMapFormatada = new HashMap<>();
        dataDePagamentoMapFormatada.put(histori.getId(), historicoService.calculadorDeMeses(histori));

        // Passar dados para o modelo
        model.addAttribute("histori", histori);
        model.addAttribute("historico", historico);
        model.addAttribute("priceTotals", priceTotals);
        model.addAttribute("priceTotalSP", priceTotalSP);
        model.addAttribute("clientes", clientes);
        model.addAttribute("socios", socios);
        model.addAttribute("bancos", bancos);
        model.addAttribute("dataFormatada", dataFormatada);
        model.addAttribute("dataDePagamentoMap", dataDePagamentoMapFormatada);

        return "detalhe/detalhes";  // Verifique se o caminho para o template está correto
    }


    @PostMapping("/histori/{id}")
    public String pagarJuros(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Historico historico = historicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado"));

        historicoService.pagarApenasJuros(historico);

        redirectAttributes.addFlashAttribute("message", "Pagamento de juros realizado com sucesso!");
        return "redirect:/";
    }






}