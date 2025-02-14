package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import com.shadow.dashboard.service.ClientService;
import com.shadow.dashboard.service.HistoricoService;
import com.shadow.dashboard.service.SocioService;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.mapping.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HistoricoController {

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
    private ParcelasRepository parcelasRepository; // Adicionado para evitar erro
    @Autowired
    private SocioService socioService;

    @GetMapping("/Table")
    public ModelAndView table() {
        ModelAndView mv = new ModelAndView("table");

        List<Clientes> clientes = clientRepository.findAll();
        List<Historico> historias = historicoRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();

        // Total de notificações
        int totalNotify = notifications.size();

        // Passando os dados para a visão
        mv.addObject("totalNotify", totalNotify);
        mv.addObject("notifications", notifications);
        mv.addObject("clientes", clientes);
        mv.addObject("historias", historias);
        mv.addObject("socios", socios);
        mv.addObject("bancos", bancos);

        return mv;
    }

    @GetMapping("/search")
    public ModelAndView search(@RequestParam(value = "keyword", required = false) String keyword) {
        ModelAndView mv = new ModelAndView("search");

        List<Clientes> clientes = clientRepository.findAll();
        List<Historico> historias = historicoRepository.findAll();
        List<Banco> bancos = bancoRepository.findAll();
        List<Socios> socios = sociosRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();

        // Total de notificações
        int totalNotify = notifications.size();

        // Inicializa mapas para evitar erro ao acessar valores nulos
        Map<Long, Double> priceTotalsPorParcelas = new HashMap<>();
        Map<Long, Double> priceTotalSP = new HashMap<>();
        Map<Long, String> dataFormatada = new HashMap<>();
        Map<Long, String> dataDePagamentoMap = new HashMap<>();

        for (Historico historia : historias) {
            priceTotalsPorParcelas.put(historia.getId(), clientService.calcularPrecoTotalComJuros(historia));
            priceTotalSP.put(historia.getId(), clientService.calcularPrecoTotalComJurosSemParcelar(historia));
        }

        // Filtro utilizando contains e evitando null
        List<Historico> listHistorico = new ArrayList<>();
        if (keyword != null && !keyword.isEmpty()) {
            listHistorico = historias.stream()
                    .filter(h -> h.getCliente() != null && h.getCliente().getNome().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
        }

        System.out.println("Resultados encontrados: " + listHistorico.size());

        // Adiciona objetos ao modelo
        mv.addObject("totalNotify", totalNotify);
        mv.addObject("notifications", notifications);
        mv.addObject("priceTotals", priceTotalsPorParcelas);
        mv.addObject("priceTotalSP", priceTotalSP);
        mv.addObject("dataFormatada", dataFormatada);
        mv.addObject("clientes", clientes);
        mv.addObject("socios", socios);
        mv.addObject("historias", historias);
        mv.addObject("dataDePagamentoMap", dataDePagamentoMap);
        mv.addObject("bancos", bancos);
        mv.addObject("listHistorico", listHistorico);
        mv.addObject("keyword", keyword);

        return mv;
    }

    @GetMapping("/historico/{id}")
    public String exibirHistoricoCliente(@PathVariable("id") Long id, Model model) {
        Clientes cliente = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para o ID: " + id));

        List<Historico> historicos = historicoRepository.findByCliente(cliente);

        model.addAttribute("cliente", cliente);
        model.addAttribute("historicos", historicos);

        return "modalHis"; // Nome do arquivo modalHis.html dentro da pasta templates/detalhe/
    }

    @PostMapping("/Table")
    public String SaveFuncionario(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Recebendo parâmetros: ");
            System.out.println("Nome: " + request.getParameter("name"));
            System.out.println("Idade: " + request.getParameter("idade"));
            System.out.println("Contato: " + request.getParameter("contact"));
            System.out.println("Endereço: " + request.getParameter("address"));

            Socios socios = new Socios();
            socios.setName(Optional.ofNullable(request.getParameter("name")).orElseThrow(() -> new IllegalArgumentException("Nome é obrigatório")));
            socios.setAge(Integer.parseInt(Optional.ofNullable(request.getParameter("idade")).orElse("0")));
            socios.setPhone(Optional.ofNullable(request.getParameter("contact")).orElse(""));
            socios.setAddress(Optional.ofNullable(request.getParameter("address")).orElse(""));

            socioService.saveAndNotify(socios);
            redirectAttributes.addFlashAttribute("message", "Funcionário registrado com sucesso!");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Idade inválida", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Erro ao registrar funcionário", e);
        }

        return "redirect:/Table";
    }

}

