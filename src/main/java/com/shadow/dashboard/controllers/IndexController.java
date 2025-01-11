package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import com.shadow.dashboard.service.ClientService;
import com.shadow.dashboard.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private SociosRepository sociosRepository;

    @Autowired
    private BancoRepository bancoRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/")
    public ModelAndView index() throws Exception {
        ModelAndView mv = new ModelAndView("index");
        List<Clientes> clientes = clientRepository.findAll();
        List<History> historias = historyRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Notification> notification = notificationRepository.findAll();

        // Limitar o número de clientes a 5
        if (clientes.size() > 5) {
            clientes = clientes.subList(0, 5);
        }

        // Criando um mapa para armazenar o priceTotal de cada cliente
        Map<Long, Double> priceTotals = new HashMap<>();
        for (History historia : historias) {
            // Calculando o preço total de um cliente, considerando suas histórias
            Double priceTotal = clientService.calcularPrecoTotalComJuros(historia);
            priceTotals.put(historia.getId(), priceTotal);  // Usando a ID do cliente como chave
        }

        Map<Long, Double> priceTotalSP = new HashMap<>();
        for (History history : historias) {
            Double priceTotal = clientService.calcularPrecoTotalComJurosSemParcelar(history);
            priceTotalSP.put(history.getId(), priceTotal);
        }

        int totalNotify = notification.size();


        Map<Long, Object> dataformatada = new HashMap<>();
        for (History historia : historias) {
            String dataforma = historyService.formatadorData(historia);
            dataformatada.put(historia.getId(), dataforma);
        }

        // Criando um mapa para armazenar a data de pagamento de cada History
        Map<Long, String> dataDePagamentoMapFormatada = new HashMap<>();
        for (History historia : historias) {
            String dataDePagamento = historyService.calculadorDeMeses(historia);
            dataDePagamentoMapFormatada.put(historia.getId(), dataDePagamento);
        }

        // Passando os dados para a visão
        mv.addObject("totalNotify", totalNotify);
        mv.addObject("notification", notification);
        mv.addObject("priceTotals", priceTotals);
        mv.addObject("priceTotalSP", priceTotalSP);
        mv.addObject("dataformatada", dataformatada);
        mv.addObject("clientes", clientes);
        mv.addObject("socios", socios);
        mv.addObject("historias", historias);
        mv.addObject("dataDePagamentoMap", dataDePagamentoMapFormatada); // Passando as datas formatadas
        mv.addObject("bancos", bancos);

        return mv;
    }

    @PostMapping("/")
    public String saveEmprestimo(@ModelAttribute History historia, RedirectAttributes redirectAttributes) {
        // Definir o status como 'progressing' automaticamente
        historia.setStatus(Status.PROCESSING);

        // Salve a entidade no banco de dados e crie a notificação automaticamente
        historyService.saveHistoryAndCreateNotification(historia);

        // Adicionar mensagem de sucesso
        redirectAttributes.addFlashAttribute("message", "Empréstimo registrado com sucesso!");

        // Redireciona para a página inicial
        return "redirect:/";
    }

    @GetMapping("/histori/{id}")
    public String detalhesVenda(@PathVariable("id") long id, Model model) {

        // Carregar listas no modelo
        List<Clientes> clientes = clientRepository.findAll();
        List<History> historias = historyRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();

        Map<Long, Object> dataformatada = new HashMap<>();
        for (History historia : historias) {
            String dataforma = historyService.formatadorData(historia);
            dataformatada.put(historia.getId(), dataforma);
        }

        // Criando um mapa para armazenar o priceTotal de cada cliente
        Map<Long, Double> priceTotals = new HashMap<>();
        for (History historia : historias) {
            // Calculando o preço total de um cliente, considerando suas histórias
            Double priceTotal = clientService.calcularPrecoTotalComJuros(historia);
            priceTotals.put(historia.getId(), priceTotal);  // Usando a ID do cliente como chave
        }

        Map<Long, Double> priceTotalSP = new HashMap<>();
        for (History history : historias) {
            Double priceTotal = clientService.calcularPrecoTotalComJurosSemParcelar(history);
            priceTotalSP.put(history.getId(), priceTotal);
        }

        // Criando um mapa para armazenar a data de pagamento de cada History
        Map<Long, String> dataDePagamentoMapFormatada = new HashMap<>();
        for (History historia : historias) {
            String dataDePagamento = historyService.calculadorDeMeses(historia);
            dataDePagamentoMapFormatada.put(historia.getId(), dataDePagamento);
        }

        // Adicionar listas ao modelo
        model.addAttribute("priceTotals", priceTotals);

        model.addAttribute("priceTotalSP", priceTotalSP);
        model.addAttribute("clientes", clientes);
        model.addAttribute("bancos", bancos);
        model.addAttribute("socios", socios);
        model.addAttribute("historias", historias);
        model.addAttribute("dataformatada", dataformatada);
        model.addAttribute("dataDePagamentoMap", dataDePagamentoMapFormatada); // Passando as datas formatadas

        // Obter história específica
        History historia = historyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("História não encontrada com o ID: " + id));

        // Adicionar história ao modelo
        model.addAttribute("histori", historia);

        // Retorna o template
        return "detalhe/detalhes";
    }


}
