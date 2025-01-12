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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/")
    public ModelAndView index() throws Exception {
        ModelAndView mv = new ModelAndView("index");

        List<Clientes> clientes = clientRepository.findAll();
        List<Historico> historias = historicoRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();

        // Limitar o número de clientes a 5
        if (clientes.size() > 5) {
            clientes = clientes.subList(0, 5);
        }

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

        // Passando os dados para a visão
        mv.addObject("totalNotify", totalNotify);
        mv.addObject("notifications", notifications);
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
}
