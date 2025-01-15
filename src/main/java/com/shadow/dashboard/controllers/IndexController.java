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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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

    public Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @GetMapping("/")
    public ModelAndView index() throws Exception {
        ModelAndView mv = new ModelAndView("index");

        List<Clientes> clientes = clientRepository.findAll();
        List<Historico> historias = historicoRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();

        String keyword = "Eduardo";
        List<Historico> listHistorico = historicoService.listAll(keyword);

        // Limitar o número de clientes a 5
        if (clientes.size() > 5) {
            clientes = clientes.subList(0, 5);
        }

// Agora 'createdDate' pode ser usado para manipulações, persistência ou enviado para o Thymeleaf


        // Mapeamento para total de preços com juros
        Map<Long, Double> priceTotals = new HashMap<>();
        for (Historico historia : historias) {
            Double priceTotal = clientService.calcularPrecoTotalComJuros(historia);
            priceTotals.put(historia.getId(), priceTotal);  // Usando a ID do cliente como chave
        }

        // Mapeamento para o valor total sem parcelamento
        Map<Long, Double> priceTotalSP = new HashMap<>();
        for (Historico historia : historias) {
            Double priceTotal = clientService.calcularPrecoTotalComJurosSemParcelar(historia);
            priceTotalSP.put(historia.getId(), priceTotal);
        }

        // Total de notificações
        int totalNotify = notifications.size();

        // Mapear datas formatadas e de pagamento
        Map<Long, Object> dataFormatada = new HashMap<>();
        Map<Long, String> dataDePagamentoMap = new HashMap<>();
        for (Historico historia : historias) {
            dataFormatada.put(historia.getId(), historicoService.formatadorData(historia));
            dataDePagamentoMap.put(historia.getId(), historicoService.calculadorDeMeses(historia));
        }

        double somaDeEmprestimo = historicoService.somaDeTodosOsEmprestimos(historias);

        // Passando os dados para a visão
        mv.addObject("totalNotify", totalNotify);
        mv.addObject("listHistorico", listHistorico);
        mv.addObject("notifications", notifications);
        mv.addObject("somaDeEmprestimo", somaDeEmprestimo);
        mv.addObject("priceTotals", priceTotals);
        mv.addObject("priceTotalSP", priceTotalSP);
        mv.addObject("dataFormatada", dataFormatada);
        mv.addObject("clientes", clientes);
        mv.addObject("socios", socios);
        mv.addObject("historias", historias);
        mv.addObject("dataDePagamentoMap", dataDePagamentoMap);
        mv.addObject("bancos", bancos);

        return mv;
    }

    @PostMapping("/")
    public String saveEmprestimo(@ModelAttribute Historico historia, RedirectAttributes redirectAttributes) {
        // Definir o status como 'processing' automaticamente
        historia.setStatus(Status.PROCESSING);

        // Salvar a entidade no banco de dados e criar a notificação
        historicoService.saveHistoryAndCreateNotification(historia);

        // Adicionar mensagem de sucesso
        redirectAttributes.addFlashAttribute("message", "Empréstimo registrado com sucesso!");

        // Redireciona para a página inicial
        return "redirect:/";
    }

    @GetMapping("/histori/{id}")
    public String detalhesVenda(@PathVariable("id") long id, Model model) {
        // Buscar o histórico específico pelo ID
        Historico histori = historicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado para o ID: " + id));

        // Carregar outras listas no modelo
        List<Clientes> clientes = clientRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Historico> historias = historicoRepository.findAll();

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
        model.addAttribute("priceTotals", priceTotals);
        model.addAttribute("priceTotalSP", priceTotalSP);
        model.addAttribute("clientes", clientes);
        model.addAttribute("socios", socios);
        model.addAttribute("bancos", bancos);
        model.addAttribute("dataFormatada", dataFormatada);
        model.addAttribute("dataDePagamentoMap", dataDePagamentoMapFormatada);

        return "detalhe/detalhes";  // Verifique se o caminho para o template está correto
    }

    @GetMapping("/editHistorico/{id}")
    public String editHistorico(@PathVariable("id") Long id, Model model) {
        Historico historico = historicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado para o ID: " + id));

        List<Clientes> clientes = clientRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        Map<Long, Double> priceTotalSP = new HashMap<>();
        Map<Long, Double> priceTotals = new HashMap<>();
        Map<Long, Object> dataDePagamentoMap = new HashMap<>();

        // Process the necessary data, e.g., price totals
        priceTotalSP.put(historico.getId(), clientService.calcularPrecoTotalComJurosSemParcelar(historico));
        priceTotals.put(historico.getId(), clientService.calcularPrecoTotalComJuros(historico));
        // Add data for last payment date
        dataDePagamentoMap.put(historico.getId(), historicoService.calculadorDeMeses(historico));

        model.addAttribute("histori", historico);
        model.addAttribute("priceTotalSP", priceTotalSP);
        model.addAttribute("priceTotals", priceTotals);
        model.addAttribute("dataDePagamentoMap", dataDePagamentoMap);
        model.addAttribute("clientes", clientes);
        model.addAttribute("bancos", bancos);
        model.addAttribute("socios", socios);

        return "edit";
    }

    @PostMapping("/editHistorico")
    public String updateHistorico(@ModelAttribute("histori") Historico histori,
                                  RedirectAttributes redirectAttributes) {
        // Processo de atualização, conforme já descrito
        Historico originalHistorico = historicoRepository.findById(histori.getId())
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado"));

        // Preenche a data de criação com a data e hora atual, caso esteja nula
        if (histori.getCreated() == null) {
            histori.setCreated(convertToDate(LocalDateTime.now())); // Agora 'LocalDateTime' é convertido para 'Date'
        }



        histori.setId(originalHistorico.getId());
        histori.setCliente(originalHistorico.getCliente()); // Não alterar cliente
        histori.setStatus(originalHistorico.getStatus());   // Não alterar status

        // Salvar a entidade com os dados atualizados
        historicoService.atualizeHistoryAndCreateNotification(histori);

        redirectAttributes.addFlashAttribute("message", "Histórico atualizado com sucesso!");

        return "redirect:/";
    }

}
