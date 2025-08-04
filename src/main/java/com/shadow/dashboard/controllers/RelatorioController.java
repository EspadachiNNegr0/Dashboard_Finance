package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/Relatorio")
public class RelatorioController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RelatorioFinanceiroRepository relatorioFinanceiroRepository;
    @Autowired
    private ParcelasRepository parcelasRepository;
    @Autowired
    private BancoRepository bancoRepository;
    @Autowired
    private SociosRepository sociosRepository;
    @Autowired
    private ClientRepository clientRepository;

    @GetMapping
    public String relatorio(Model model) {
        List<RelatorioFinanceiro> relatoriosFinanceiros = relatorioFinanceiroRepository.findAll();

        List<Notification> notifications = notificationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();
        int totalNotify = notifications.size();
        List<Clientes> clientes = clientRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Clientes::getNome)) // 🔠 ordena alfabeticamente por nome
                .collect(Collectors.toList());

        List<Socios> funcionarios = sociosRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Socios::getName))
                .collect(Collectors.toList());

        List<Banco> bancos = bancoRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Banco::getNome))
                .collect(Collectors.toList());


        model.addAttribute("totalNotify", totalNotify);
        model.addAttribute("notifications", notifications);
        model.addAttribute("relatoriosFinanceiros", relatoriosFinanceiros);
        model.addAttribute("bancos", bancos);
        model.addAttribute("funcionarios", funcionarios);
        model.addAttribute("clientes", clientes);

        return "relatorio";
    }

    @GetMapping("/gerar-pdf")
    public String gerarPdfView(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String mes,
            @RequestParam(required = false) String banco,
            @RequestParam(required = false) String funcionario,
            @RequestParam(required = false) String cliente,
            Model model
    ) {
        List<RelatorioFinanceiro> relatoriosFinanceiros = filtrarRelatorios(tipo, mes, banco, funcionario, cliente);
        model.addAttribute("relatoriosFinanceiros", relatoriosFinanceiros);
        return "relatorio-pdf";
    }

    public List<RelatorioFinanceiro> filtrarRelatorios(String tipo, String mes, String banco, String funcionario, String cliente) {
        List<RelatorioFinanceiro> todos = relatorioFinanceiroRepository.findAll();

        return todos.stream()
                .filter(r -> tipo == null || tipo.isBlank() ||
                        (r.getStatus() != null && r.getStatus().name().equalsIgnoreCase(tipo)))

                .filter(r -> {
                    if (mes == null || mes.isEmpty()) return true;
                    if (r.getData() == null) return false;
                    String dataFormatada = new java.text.SimpleDateFormat("yyyy-MM").format(r.getData());
                    return dataFormatada.equals(mes);
                })

                .filter(r -> banco == null || banco.isEmpty() ||
                        (r.getBanco() != null && r.getBanco().equalsIgnoreCase(banco)))

                .filter(r -> funcionario == null || funcionario.isEmpty() ||
                        (r.getHistorico() != null && r.getHistorico().getSocios() != null &&
                                r.getHistorico().getSocios().getName() != null &&
                                funcionario.equalsIgnoreCase(r.getHistorico().getSocios().getName())))

                .filter(r -> cliente == null || cliente.isEmpty() ||
                        (r.getHistorico() != null && r.getHistorico().getCliente() != null &&
                                r.getHistorico().getCliente().getNome() != null &&
                                cliente.equalsIgnoreCase(r.getHistorico().getCliente().getNome())))

                .collect(Collectors.toList());
    }



}
