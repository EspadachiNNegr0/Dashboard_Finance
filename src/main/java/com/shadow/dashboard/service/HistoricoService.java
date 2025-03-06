package com.shadow.dashboard.service;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class HistoricoService {

    @Autowired
    private HistoricoRepository historicoRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private ClientRepository clienteRepository;
    @Autowired
    private ParcelasRepository parcelasRepository;
    @Autowired
    private RelatorioEntradaRepository relatorioEntradaRepository;
    @Autowired
    private RelatorioSaidaRepository relatorioSaidaRepository;
    @Autowired
    private RelatorioService relatorioService;
    @Autowired
    private RelatorioFinanceiroRepository relatorioFinanceiroRepository;

    /**
     * 🔹 Salva um novo histórico e cria as parcelas associadas.
     */
    public void saveHistoryAndCreateNotification(Historico historico) {
        validarHistorico(historico);
        historico.setCodigo(historico.getCodigo() == 0 ? gerarCodigoUnico() : historico.getCodigo());
        historico.setCreationF(calculaDataFinal(historico));
        historico = historicoRepository.save(historico);

        criarParcelas(historico);
        relatorioService.criarRelatorioSaida(historico);
        criarNotificacao(historico, "📢 Novo empréstimo registrado.");
    }

    private void validarHistorico(Historico historico) {
        if (historico.getCreated() == null || historico.getParcelamento() <= 0) {
            throw new IllegalArgumentException("Os campos 'created' e 'parcelamento' são obrigatórios.");
        }
    }

    private int gerarCodigoUnico() {
        Random random = new Random();
        int codigo;
        do {
            codigo = random.nextInt(900000) + 100000;
        } while (historicoRepository.existsByCodigo(codigo));
        return codigo;
    }

    private Date calculaDataFinal(Historico historico) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(historico.getCreated());
        calendar.add(Calendar.MONTH, historico.getParcelamento());
        return calendar.getTime();
    }

    /**
     * 🔹 Criar uma notificação para o histórico.
     */
    public void criarNotificacao(Historico historico, String mensagem) {
        if (historico == null) return;
        Notification notificacao = new Notification();
        notificacao.setMessage(mensagem);
        notificacao.setCreatedAt(LocalDateTime.now());
        notificacao.setRead(false);
        notificationRepository.save(notificacao);
    }

    /**
     * 🔹 Criar as parcelas do histórico.
     */
    public void criarParcelas(Historico historico) {
        // 🔹 Se já existem parcelas para esse histórico, não criar novas
        if (!parcelasRepository.findByHistorico(historico).isEmpty()) return;

        double montanteTotal = historico.getMontante();
        double valorParcela = montanteTotal / historico.getParcelamento();
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(historico.getCreated());

        List<Parcelas> parcelasCriadas = new ArrayList<>();

        for (int i = 1; i <= historico.getParcelamento(); i++) {
            calendario.add(Calendar.MONTH, 1);

            Parcelas parcela = new Parcelas();
            parcela.setHistorico(historico);
            parcela.setParcelas(i);
            parcela.setValor(valorParcela);
            parcela.setDataPagamento(calendario.getTime());
            parcela.setStatus(StatusParcela.PENDENTE);
            parcela.setPagas(0);

            parcelasCriadas.add(parcela);
        }

        // 🔹 Salvar todas as parcelas de uma vez para evitar inconsistências
        parcelasRepository.saveAll(parcelasCriadas);

        // 🔹 Criar relatórios após salvar parcelas
        relatorioService.criarRelatorioEntrada(historico);
    }


    /**
     * 🔹 Atualiza o status do histórico conforme as parcelas.
     */
    public void atualizarStatusHistorico(Historico historico) {
        List<Parcelas> parcelas = parcelasRepository.findByHistorico(historico);
        boolean temAtrasadas = parcelas.stream().anyMatch(p -> p.getPagas() == -1);
        boolean temPendentes = parcelas.stream().anyMatch(p -> p.getPagas() == 0);

        if (temAtrasadas) {
            historico.setStatus(Status.ATRASADO);
        } else if (!temPendentes) {
            historico.setStatus(Status.PAGO);
        } else {
            historico.setStatus(Status.PENDENTE);
        }

        historicoRepository.save(historico);
    }

    /**
     * 🔹 Atualiza a parcela com um pagamento.
     */
    public void atualizarParcela(Parcelas parcela, Banco bancoEntrada, double valorPago) {
        parcela.setBancoEntrada(bancoEntrada.getNome());
        parcela.setValorPago(parcela.getValorPago() + valorPago);

        // 🔹 Obtém o histórico associado à parcela
        Historico historico = parcela.getHistorico();

        // 🔹 Obtém a primeira parcela do histórico
        Parcelas primeiraParcela = parcelasRepository.findFirstByHistoricoOrderByParcelasAsc(historico);

        double juros;

        if (parcela.getId().equals(primeiraParcela.getId())) {
            // 🔹 Se for a primeira parcela, usa os juros do Price do histórico
            juros = historico.getPrice() * (historico.getPercentage() / 100.0);
        } else {
            // 🔹 A partir da segunda parcela, calcular juros com base no valor da parcela
            juros = parcela.getValor() * (historico.getPercentage() / 100.0);
        }

        parcela.setValorJuros(juros);
        parcela.setValorAmortizado(parcela.getValorPago() - juros);
        parcela.setValorSobra(parcela.getValor() - (parcela.getValorAmortizado() + parcela.getValorJuros()));
        parcela.setPagas(1);
        parcela.setDataQPagamento(new Date());
        parcela.setStatus(parcela.getPagas() == 1 ? StatusParcela.PAGO : StatusParcela.PENDENTE);

        parcelasRepository.save(parcela);

        // 🔹 Buscar o Relatório de Entrada **correspondente à parcela** e atualizar apenas se existir
        Optional<RelatorioEntrada> optionalRelatorioEntrada = relatorioEntradaRepository.findByParcela(parcela);

        if (optionalRelatorioEntrada.isPresent()) {
            RelatorioEntrada relatorioEntrada = optionalRelatorioEntrada.get();
            relatorioEntrada.setBanco(parcela.getBancoEntrada());
            relatorioEntrada.setValor(parcela.getValor());
            relatorioEntrada.setJuros(parcela.getValorJuros());
            relatorioEntrada.setAmortizacao(parcela.getValorAmortizado());

            relatorioEntradaRepository.save(relatorioEntrada);
            System.out.println("✅ Relatório de Entrada atualizado para a parcela ID: " + parcela.getId());

            // 🔹 Atualizar o Relatório Financeiro com base no Relatório de Entrada
            Optional<RelatorioFinanceiro> optionalRelatorioFinanceiro = relatorioFinanceiroRepository.findByParcela(parcela);

            if (optionalRelatorioFinanceiro.isPresent()) {
                RelatorioFinanceiro relatorioFinanceiro = optionalRelatorioFinanceiro.get();
                relatorioFinanceiro.setBanco(relatorioEntrada.getBanco());
                relatorioFinanceiro.setValor(relatorioEntrada.getValor());
                relatorioFinanceiro.setJuros(relatorioEntrada.getJuros());
                relatorioFinanceiro.setAmortizacao(relatorioEntrada.getAmortizacao());

                relatorioFinanceiroRepository.save(relatorioFinanceiro);
                System.out.println("✅ Relatório Financeiro atualizado para a parcela ID: " + parcela.getId());
            } else {
                System.out.println("⚠️ Nenhum relatório financeiro encontrado para a parcela ID: " + parcela.getId() + ". Nenhuma atualização realizada.");
            }
        } else {
            System.out.println("⚠️ Nenhum relatório de entrada encontrado para a parcela ID: " + parcela.getId() + ". Nenhuma atualização realizada.");
        }
    }

    /**
     * 🔹 Criar uma nova parcela se necessário (caso haja saldo residual).
     */
    public void criarNovaParcelaSeNecessario(Historico historico, double saldoResidual, Parcelas parcela) {
        if (saldoResidual <= 0 || historico.getStatus() == Status.PAGO) {
            System.out.println("⚠️ Nenhuma nova parcela será criada, pois o empréstimo já foi quitado.");
            return;
        }

        double juros = saldoResidual * (parcela.getHistorico().getPercentage() / 100.0);

        Parcelas novaParcela = new Parcelas();
        novaParcela.setHistorico(historico);
        novaParcela.setParcelas(parcela.getParcelas() + 1);
        novaParcela.setValor(saldoResidual + juros);
        novaParcela.setStatus(StatusParcela.PENDENTE);
        novaParcela.setPagas(0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parcela.getDataPagamento());
        calendar.add(Calendar.MONTH, 1);
        novaParcela.setDataPagamento(calendar.getTime());

        parcelasRepository.save(novaParcela);
        System.out.println("✅ Nova parcela criada com saldo residual: R$ " + saldoResidual);
    }

    /**
     * 🔹 Método para criar um Relatório de Entrada automaticamente ao gerar nova parcela
     */
    private void criarRelatorioEntrada(Parcelas parcela, Historico historico) {
        RelatorioEntrada relatorioEntrada = new RelatorioEntrada();
        relatorioEntrada.setHistorico(historico);
        relatorioEntrada.setCodigo(historico.getCodigo());
        relatorioEntrada.setValor(parcela.getValor());
        relatorioEntrada.setJuros(parcela.getValorJuros());
        relatorioEntrada.setAmortizacao(parcela.getValorAmortizado());
        relatorioEntrada.setBanco(parcela.getBancoEntrada());
        relatorioEntrada.setData(new Date()); // Usa a data atual
        relatorioEntrada.setStatus(StatusR.Entrada);

        relatorioEntradaRepository.save(relatorioEntrada);
        relatorioService.criarRelatorioFinanceiroEntrada(relatorioEntrada);

        System.out.println("📌 Novo relatório de entrada criado para a parcela ID: " + parcela.getId());
    }

    public double calcularTotalJuros(List<Parcelas> parcelas) {
        return parcelas.stream()
                .mapToDouble(Parcelas::getValorJuros) // Soma todos os valores de juros
                .sum();
    }


    /**
     * 🔹 Calcula o total pago de um histórico.
     */

    public double calcularTotalPago(Historico historico) {
        List<Parcelas> parcelasPagas = parcelasRepository.findByHistoricoIdAndStatus(historico.getId(), StatusParcela.PAGO);
        double valorAmortizadoTotal = 0;
        double valorJurosTotal = 0;
        for (Parcelas parcelaPaga : parcelasPagas) {
            valorAmortizadoTotal += parcelaPaga.getValorAmortizado();
            valorJurosTotal += parcelaPaga.getValorJuros();
        }
        return valorAmortizadoTotal + valorJurosTotal;
    }

    /**
     * 🔹 Recalcula as parcelas do histórico com base no saldo restante e juros compostos.
     */
    @Transactional
    public void recalcularParcelas(Historico historico) {
        List<Parcelas> parcelas = parcelasRepository.findByHistorico(historico);

        if (parcelas.isEmpty()) {
            System.out.println("⚠️ Nenhuma parcela encontrada para o histórico #" + historico.getId());
            return;
        }

        // 🔹 Calcula novo valor de cada parcela com juros compostos
        double montanteTotal = historico.getMontante();
        double taxaJuros = historico.getPercentage() / 100.0;
        int totalParcelas = historico.getParcelamento();

        double montanteCorrigido = montanteTotal * Math.pow((1 + taxaJuros), totalParcelas);
        double novoValorParcela = montanteCorrigido / totalParcelas;

        System.out.println("📌 Recalculando parcelas para histórico #" + historico.getId());
        System.out.println("   ➝ Novo valor por parcela: R$" + String.format("%.2f", novoValorParcela));

        // 🔹 Atualiza as parcelas existentes
        for (int i = 0; i < totalParcelas && i < parcelas.size(); i++) {
            Parcelas parcela = parcelas.get(i);
            parcela.setValor(novoValorParcela);
            parcelasRepository.save(parcela);
        }

        // 🔹 Se o número de parcelas aumentou, cria novas parcelas
        if (parcelas.size() < totalParcelas) {
            Calendar calendario = Calendar.getInstance();
            calendario.setTime(historico.getCreated());

            for (int i = parcelas.size(); i < totalParcelas; i++) {
                calendario.add(Calendar.MONTH, 1);

                Parcelas novaParcela = new Parcelas();
                novaParcela.setHistorico(historico);
                novaParcela.setParcelas(i + 1);
                novaParcela.setValor(novoValorParcela);
                novaParcela.setDataPagamento(calendario.getTime());
                novaParcela.setStatus(StatusParcela.PENDENTE);
                novaParcela.setPagas(0);

                parcelasRepository.save(novaParcela);
            }
            System.out.println("📌 Novas parcelas criadas.");
        }

        // 🔹 Se o número de parcelas diminuiu, remove as parcelas excedentes
        if (parcelas.size() > totalParcelas) {
            List<Parcelas> parcelasParaRemover = parcelas.subList(totalParcelas, parcelas.size());
            parcelasRepository.deleteAll(parcelasParaRemover);
            System.out.println("📌 Parcelas excedentes removidas.");
        }

        System.out.println("✅ Parcelas recalculadas com sucesso!");
    }


    /**
     * 🔹 Verifica e quita o empréstimo se todas as parcelas forem pagas.
     */
    public boolean quitarEmprestimoSeNecessario(Historico historico, double totalAmortizado) {
        if (totalAmortizado >= historico.getMontante()) {
            List<Parcelas> parcelasDoHistorico = parcelasRepository.findByHistorico(historico);

            for (Parcelas p : parcelasDoHistorico) {
                p.setPagas(1);
                p.setStatus(StatusParcela.PAGO);
            }

            parcelasRepository.saveAll(parcelasDoHistorico);

            // ✅ Atualizar o status do histórico
            historico.setStatus(Status.PAGO);
            historicoRepository.save(historico);

            System.out.println("✅ Empréstimo quitado! Nenhuma nova parcela será criada.");
            return true;
        }
        return false;
    }

    /**
     * 🔹 Repassa o valor de sobra para a próxima parcela.
     */
    public void repassarSobra(Parcelas parcela, Historico historico) {
        double valorSobra = parcela.getValorSobra();

        if (valorSobra <= 0) {
            System.out.println("⚠️ Nenhuma sobra para repassar.");
            return;
        }

        System.out.println("📌 Repasse de sobra de R$" + valorSobra);

        // 🔹 Busca a próxima parcela pendente
        List<Parcelas> proximasParcelas = parcelasRepository.findProximaParcela(historico, parcela.getParcelas());

        if (!proximasParcelas.isEmpty()) {
            Parcelas proximaParcela = proximasParcelas.get(0); // ✅ Pega a primeira parcela pendente

            // 🔹 Adiciona a sobra ao valor da próxima parcela
            proximaParcela.setValor(proximaParcela.getValor() + valorSobra);
            parcelasRepository.save(proximaParcela);
            System.out.println("✅ Sobra de R$" + valorSobra + " adicionada à próxima parcela ID: " + proximaParcela.getId());

            // 🔹 Atualizar o Relatório de Entrada correspondente à próxima parcela
            Optional<RelatorioEntrada> optionalRelatorioEntrada = relatorioEntradaRepository.findByParcela(proximaParcela);

            if (optionalRelatorioEntrada.isPresent()) {
                RelatorioEntrada relatorioEntrada = optionalRelatorioEntrada.get();
                relatorioEntrada.setValor(proximaParcela.getValor());
                relatorioEntrada.setJuros(proximaParcela.getValorJuros());
                relatorioEntrada.setAmortizacao(proximaParcela.getValorAmortizado());

                relatorioEntradaRepository.save(relatorioEntrada);
                System.out.println("✅ Relatório de Entrada atualizado para a parcela ID: " + proximaParcela.getId());

                // 🔹 Atualizar o Relatório Financeiro correspondente
                Optional<RelatorioFinanceiro> optionalRelatorioFinanceiro = relatorioFinanceiroRepository.findByParcela(proximaParcela);

                if (optionalRelatorioFinanceiro.isPresent()) {
                    RelatorioFinanceiro relatorioFinanceiro = optionalRelatorioFinanceiro.get();
                    relatorioFinanceiro.setValor(relatorioEntrada.getValor());
                    relatorioFinanceiro.setJuros(relatorioEntrada.getJuros());
                    relatorioFinanceiro.setAmortizacao(relatorioEntrada.getAmortizacao());

                    relatorioFinanceiroRepository.save(relatorioFinanceiro);
                    System.out.println("✅ Relatório Financeiro atualizado para a parcela ID: " + proximaParcela.getId());
                } else {
                    System.out.println("⚠️ Nenhum relatório financeiro encontrado para a parcela ID: " + proximaParcela.getId());
                }
            } else {
                System.out.println("⚠️ Nenhum relatório de entrada encontrado para a parcela ID: " + proximaParcela.getId());
            }
        } else {
            System.out.println("⚠️ Nenhuma próxima parcela encontrada. Nenhuma nova parcela será criada.");
        }

        // 🔹 Atualiza a parcela atual para indicar que a sobra foi repassada
        parcela.setValorSobra(0);
        parcelasRepository.save(parcela);
    }

    /**
     * 🔹 Calcula os juros compostos sobre o valor do histórico.
     */
    public double calcularJuros(Historico historico, Parcelas parcela) {
        double taxaJuros = historico.getPercentage() / 100.0;
        Parcelas primeiraParcela = parcelasRepository.findFirstByHistoricoOrderByParcelasAsc(historico);

        // Se for a primeira parcela, usa o juros de Price do histórico
        if (parcela.getId().equals(primeiraParcela.getId())) {
            return historico.getPrice() * taxaJuros;
        }

        // A partir da segunda parcela, calcula o juros baseado no valor da parcela
        return parcela.getValor() * taxaJuros;
    }
    /**
     * 🔹 Exclui um histórico e todos os seus registros associados.
     */
    @Transactional
    public void excluirHistoricoEAssociados(Long id) {
        Historico historico = historicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Histórico não encontrado"));

        parcelasRepository.deleteByHistorico(historico);
        relatorioEntradaRepository.deleteByHistorico(historico);
        relatorioSaidaRepository.deleteByHistorico(historico);
        historicoRepository.delete(historico);
    }

    /**
     * 🔹 Atualiza automaticamente o status de parcelas vencidas.
     */
    @Scheduled(fixedRate = 30000)
    public void atualizarStatusParcelasVencidas() {
        Date hoje = new Date();
        List<Parcelas> parcelas = parcelasRepository.findAll();
        for (Parcelas parcela : parcelas) {
            if (parcela.getDataPagamento().before(hoje) && parcela.getPagas() == 0) {
                parcela.setPagas(-1);
                parcela.setStatus(StatusParcela.ATRASADO);
                parcelasRepository.save(parcela);
                criarNotificacao(parcela.getHistorico(), "❌ Parcela vencida e agora está ATRASADA!");
            }
        }
    }
}
