package com.shadow.dashboard.controllers;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import com.shadow.dashboard.service.ClientService;
import com.shadow.dashboard.service.HistoricoService;
import com.shadow.dashboard.service.SocioService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private RelatorioEntradaRepository relatorioEntradaRepository;

    @Autowired
    private RelatorioSaidaRepository relatorioSaidaRepository;

    @Autowired
    private SocioService socioService;

    @Autowired
    private RelatorioFinanceiroRepository relatorioFinanceiroRepository;

    @Autowired
    private RelatorioProjetadaRepository relatorioProjetadaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/Table")
    public ModelAndView table() {
        ModelAndView mv = new ModelAndView("table");


        List<Historico> historias = historicoRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed()) // 🔥 Ordena pela data mais recente primeiro
                .collect(Collectors.toList());
        List<Clientes> clientes = clientRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Clientes::getNome)) // 🔠 ordena alfabeticamente por nome
                .collect(Collectors.toList());

        List<Socios> socios = sociosRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Socios::getName))
                .collect(Collectors.toList());

        List<Banco> bancos = bancoRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Banco::getNome))
                .collect(Collectors.toList());


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
        List<Notification> notifications = notificationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed()) // 🔥 Ordena pela data mais recente primeiro
                .collect(Collectors.toList());

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

    @GetMapping("/cliente/{id}")
    public String exibirHistoricoCliente(@PathVariable("id") Long id, Model model) {
        Clientes cliente = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para o ID: " + id));

        List<Historico> historicos = historicoRepository.findByCliente(cliente);

        if (historicos.isEmpty()) {
            throw new RuntimeException("Nenhum histórico encontrado para o cliente ID: " + id);
        }

        Historico historico = historicos.get(0); // Pegando o primeiro histórico apenas como exemplo

        model.addAttribute("cliente", cliente);
        model.addAttribute("historicos", historicos);
        model.addAttribute("historico", historico); // ✅ Adicionando um histórico ao Model

        return "modalHis"; // Nome do arquivo modalHis.html dentro da pasta templates/detalhe/
    }

    @GetMapping("/cliente2/{id}")
    public String exibirHistoricoCliente2(@PathVariable("id") Long id, Model model) {
        Clientes cliente = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para o ID: " + id));

        List<Historico> historicos = historicoRepository.findByCliente(cliente);

        if (historicos.isEmpty()) {
            throw new RuntimeException("Nenhum histórico encontrado para o cliente ID: " + id);
        }

        Historico historico = historicos.get(0); // Pegando o primeiro histórico apenas como exemplo

        model.addAttribute("cliente", cliente);
        model.addAttribute("historicos", historicos);
        model.addAttribute("historico", historico); // ✅ Adicionando um histórico ao Model

        return "modalHis2"; // Nome do arquivo modalHis.html dentro da pasta templates/detalhe/
    }

    // Buscar histórico pelo código do empréstimo
    @GetMapping("/emprestimo/{codigo}")
    public String exibirHistorico(@PathVariable("codigo") int codigo, Model model) {

        // Buscar o histórico pelo código
        Historico historico = historicoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Código não encontrado: " + codigo));

        // Buscar as parcelas associadas a esse histórico
        List<Parcelas> parcelas = historico.getParcelas();
        double totalJuros = historicoService.calcularTotalJuros(parcelas);

        // Adicionar os dados ao modelo para a view
        model.addAttribute("historico", historico);
        model.addAttribute("parcelas", parcelas); // Enviar as parcelas para a página
        model.addAttribute("totalJuros", totalJuros);

        return "His"; // Nome do arquivo modalHis.html dentro de templates/detalhe/
    }

    @GetMapping("/emprestimo/{codigo}/editar")
    public String editarHistorico(@PathVariable("codigo") int codigo, Model model) {
        // Buscar o histórico pelo código
        Historico historico = historicoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado para o código: " + codigo));

        model.addAttribute("histori", historico);
        return "editarHistorico"; // Nome da página de edição
    }

    @PostMapping("/emprestimo/{codigo}/salvar")
    public String salvarEdicaoHistorico(
            @PathVariable("codigo") int codigo,
            @RequestParam("price") double novoPrice,
            @RequestParam("percentage") int percentage,
            @RequestParam("description") String description) {

        Historico historico = historicoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado para o código: " + codigo));

        // 🔹 Verifica se o valor do empréstimo foi alterado
        boolean valorAlterado = historico.getPrice() != novoPrice;

        // Atualizar os dados do histórico
        historico.setPrice(novoPrice);
        historico.setPercentage(percentage);
        historico.setDescription(description);

        // 🔹 Salvar histórico atualizado
        historicoRepository.save(historico);

        // ✅ Se houve alteração, recalcula parcelas
        if (valorAlterado) {
            historicoService.recalcularParcelas(historico);
        }

        return "redirect:/histori/" + historico.getId(); // ✅ Redireciona para os detalhes
    }


    @DeleteMapping("/historico/delete/{id}")
    @Transactional
    public ResponseEntity<String> excluirHistorico(@PathVariable Long id) {
        try {
            System.out.println("🔍 Buscando histórico ID: " + id);

            Optional<Historico> historicoOptional = historicoRepository.findById(id);
            if (historicoOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("❌ Histórico não encontrado.");
            }

            Historico historico = historicoOptional.get();
            System.out.println("📌 Histórico encontrado: " + historico.getId());

            // 🔹 1. EXCLUIR RELATÓRIOS FINANCEIROS
            int relatoriosFinanceirosExcluidos = relatorioFinanceiroRepository.deleteByHistorico(historico);
            System.out.println("📌 Relatórios Financeiros excluídos: " + relatoriosFinanceirosExcluidos);

            // 🔹 2. EXCLUIR RELATÓRIOS DE ENTRADA E SAÍDA (antes das parcelas!)
            int relatoriosEntradaExcluidos = relatorioEntradaRepository.deleteByHistorico(historico);
            int relatoriosSaidaExcluidos = relatorioSaidaRepository.deleteByHistorico(historico);
            System.out.println("📌 Relatórios excluídos: Entrada=" + relatoriosEntradaExcluidos + ", Saída=" + relatoriosSaidaExcluidos);

            // 🔹 3. EXCLUIR RELATÓRIOS PROJETADOS (antes das parcelas!)
            int relatoriosProjetadosExcluidos = relatorioProjetadaRepository.deleteByHistorico(historico);
            System.out.println("📌 Relatórios Projetados excluídos: " + relatoriosProjetadosExcluidos);

            // 🔹 4. EXCLUIR PARCELAS (agora que os relatórios já foram removidos)
            int parcelasExcluidas = parcelasRepository.deleteByHistorico(historico);
            System.out.println("📌 Parcelas excluídas: " + parcelasExcluidas);

            // 🔹 5. EXCLUIR O HISTÓRICO
            historicoRepository.delete(historico);
            System.out.println("✅ Histórico excluído com sucesso!");

            return ResponseEntity.ok("✅ Histórico e relatórios excluídos com sucesso!");

        } catch (Exception e) {
            e.printStackTrace(); // 🔹 Exibe erro no console para depuração
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erro ao excluir histórico: " + e.getMessage());
        }
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

