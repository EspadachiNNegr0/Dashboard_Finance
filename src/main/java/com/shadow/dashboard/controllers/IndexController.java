package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.BancoRepository;
import com.shadow.dashboard.repository.ClientRepository;
import com.shadow.dashboard.repository.HistoryRepository;
import com.shadow.dashboard.repository.SociosRepository;
import com.shadow.dashboard.service.ClientService;
import com.shadow.dashboard.service.EncryptionService;
import com.shadow.dashboard.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
    private EncryptionService encryptionService; // Serviço de criptografia

    @GetMapping("/")
    public ModelAndView index() throws Exception {
        ModelAndView mv = new ModelAndView("index");
        List<Clientes> clientes = clientRepository.findAll();
        List<History> historias = historyRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();

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

        Map<Long, Object> dataformatada = new HashMap<>();
        for (History historia : historias) {
            String dataforma = historyService.formatadorData(historia);
            dataformatada.put(historia.getId(), dataforma);
        }


        for (History historia : historias) {
            String encryptedId = encryptionService.encrypt(String.valueOf(historia.getId()));
            historia.setEncryptedId(encryptedId); // Adicionar um campo com o ID criptografado
        }

        // Criando um mapa para armazenar a data de pagamento de cada History
        Map<Long, String> dataDePagamentoMapFormatada = new HashMap<>();
        for (History historia : historias) {
            String dataDePagamento = historyService.calculadorDeMeses(historia);
            dataDePagamentoMapFormatada.put(historia.getId(), dataDePagamento);
        }

        // Passando os dados para a visão
        mv.addObject("priceTotals", priceTotals);
        mv.addObject("dataformatada", dataformatada);
        mv.addObject("clientes", clientes);
        mv.addObject("historias", historias);
        mv.addObject("dataDePagamentoMap", dataDePagamentoMapFormatada); // Passando as datas formatadas
        mv.addObject("bancos", bancos);

        return mv;
    }

    @PostMapping("/")
    public String saveEmprestimo(@ModelAttribute History historia, RedirectAttributes redirectAttributes) {
        // Definir o status como 'progressing' automaticamente
        historia.setStatus(Status.PROCESSING);

        // Salve a entidade no banco
        historyRepository.save(historia);

        // Adicionar mensagem de sucesso
        redirectAttributes.addFlashAttribute("message", "Empréstimo registrado com sucesso!");

        // Redireciona para a página inicial
        return "redirect:/";
    }
}
