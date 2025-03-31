package com.shadow.dashboard.service;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
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
    @Autowired
    private RelatorioProjetadaRepository relatorioProjetadaRepository;

    /**
     * 🔹 Salva um novo histórico e cria as parcelas associadas.
     */
    public void saveHistoryAndCreateNotification(Historico historico) {
        validarHistorico(historico);
        historico.setCodigo(gerarCodigoUnicoH());
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

    private int gerarCodigoUnicoH() {
        int codigo;
        Random random = new Random();

        do {
            codigo = 100000 + random.nextInt(900000); // Gera um número entre 100000 e 999999
        } while (historicoRepository.existsByCodigo(codigo));

        return codigo;
    }


    private int gerarCodigoUnico() {
        Integer maxCodigo = relatorioFinanceiroRepository.findMaxCodigo();
        return (maxCodigo == null ? 100000 : maxCodigo + 1);
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

        // 🔹 Salvar todas as parcelas primeiro
        parcelasRepository.saveAll(parcelasCriadas);

        // 🔹 Agora criar relatórios para cada parcela
        for (Parcelas parcela : parcelasCriadas) {
            relatorioService.criarRelatorioProjetadaParaParcela(parcela);
        }
    }

    /**
     * 🔹 Atualiza a parcela com um pagamento.
     */
    public void atualizarParcela(Parcelas parcela, Banco bancoEntrada, double valorPago, Date dataPagamento) {
        parcela.setBancoEntrada(bancoEntrada.getNome());
        parcela.setValorPago(parcela.getValorPago() + valorPago);

        // 🔹 Obtém o histórico associado à parcela
        Historico historico = parcela.getHistorico();

        // 🔹 Verifica se o histórico já está setado antes de buscar novamente
        if (historico == null) {
            throw new IllegalStateException("❌ ERRO: O objeto 'historico' está NULL ao tentar salvar RelatorioEntrada.");
        }

        // 🔹 Obtém a primeira parcela do histórico
        Parcelas primeiraParcela = parcelasRepository.findFirstByHistoricoOrderByParcelasAsc(historico);

        double juros = calcularJuros(historico, parcela);

        parcela.setValorJuros(juros);
        parcela.setValorAmortizado(parcela.getValorPago() - juros);
        parcela.setValorSobra(parcela.getValor() - (parcela.getValorAmortizado() + parcela.getValorJuros()));
        parcela.setPagas(1);
        parcela.setDataQPagamento(dataPagamento);
        parcela.setStatus(parcela.getPagas() == 1 ? StatusParcela.PAGO : StatusParcela.PENDENTE);

        parcelasRepository.save(parcela);

        // 🔹 Criar um novo RelatorioEntrada
        RelatorioEntrada relatorioEntrada = new RelatorioEntrada();

        // 🔹 Define histórico apenas se ainda não estiver setado
        if (relatorioEntrada.getHistorico() == null) {
            relatorioEntrada.setHistorico(historico);
        }

        relatorioEntrada.setStatus(StatusR.Entrada);
        relatorioEntrada.setCodigo(gerarCodigoUnico());
        relatorioEntrada.setBanco(parcela.getBancoEntrada());
        relatorioEntrada.setValor(parcela.getValorPago());
        relatorioEntrada.setJuros(parcela.getValorJuros());
        relatorioEntrada.setAmortizacao(parcela.getValorAmortizado());
        relatorioEntrada.setData(parcela.getDataQPagamento());

        // 🔹 Salvar o relatório apenas se o histórico estiver correto
        if (relatorioEntrada.getHistorico() != null) {
            relatorioEntradaRepository.save(relatorioEntrada);
            System.out.println("✅ RelatorioEntrada criado para a parcela ID: " + parcela.getId());
        } else {
            System.err.println("❌ ERRO: Não foi possível salvar RelatorioEntrada porque o histórico é NULL.");
        }

        // 🔹 Criar sempre um novo Relatório Financeiro, mesmo se já existir outro para a mesma parcela
        RelatorioFinanceiro relatorioFinanceiro = new RelatorioFinanceiro();
        if (relatorioFinanceiro.getHistorico() == null) {
            relatorioFinanceiro.setHistorico(historico);
        }
        relatorioFinanceiro.setCodigo(gerarCodigoUnico());
        relatorioFinanceiro.setRelatorioEntrada(relatorioEntrada);
        relatorioFinanceiro.setStatus(StatusR.Entrada);
        relatorioFinanceiro.setBanco(relatorioEntrada.getBanco());
        relatorioFinanceiro.setValor(relatorioEntrada.getValor());
        relatorioFinanceiro.setJuros(relatorioEntrada.getJuros());
        relatorioFinanceiro.setAmortizacao(relatorioEntrada.getAmortizacao());
        relatorioFinanceiro.setData(relatorioEntrada.getData());

        relatorioFinanceiroRepository.save(relatorioFinanceiro);
        System.out.println("✅ Criado novo Relatório Financeiro para a parcela ID: " + parcela.getId());
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
        novaParcela.setValorJuros(juros);
        novaParcela.setStatus(StatusParcela.PENDENTE);
        novaParcela.setPagas(0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parcela.getDataPagamento());
        calendar.add(Calendar.MONTH, 1);
        novaParcela.setDataPagamento(calendar.getTime());

        parcelasRepository.save(novaParcela);
        System.out.println("✅ Nova parcela criada com saldo residual: R$ " + saldoResidual);

        // Criar Relatório Projetado APENAS para essa parcela
        relatorioService.criarRelatorioProjetadaParaParcela(novaParcela);
    }

    private Long gerarNovoId(JpaRepository<?, Long> repository) {
        Long novoId;
        do {
            novoId = new Random().nextLong(1, Long.MAX_VALUE); // Gera um ID aleatório válido
        } while (repository.existsById(novoId)); // Garante que o ID não existe no banco

        return novoId;
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

        // 🔹 Se for a primeira parcela, juros com base no valor do empréstimo (price)
        if (parcela.getId().equals(primeiraParcela.getId())) {
            double jurosPrimeira = historico.getPrice() * taxaJuros;
            System.out.println("ℹ️ Juros da primeira parcela: R$" + jurosPrimeira);
            return jurosPrimeira;
        }

        // 🔹 Se valorPago == valorJuros da parcela atual, manter mesmo valor de juros
        if (Double.compare(parcela.getValorPago(), parcela.getValorJuros()) == 0) {
            System.out.println("ℹ️ Juros mantidos porque valorPago == valorJuros: R$" + parcela.getValorJuros());
            return parcela.getValorJuros();
        }

        // 🔹 Caso contrário, calcular o valor sem juros e depois descobrir o juros real
        double valorTotal = parcela.getValor();
        double valorBase = valorTotal / (1 + taxaJuros);
        double jurosCalculado = valorTotal - valorBase;

        System.out.println("ℹ️ Juros recalculados a partir do valor total com juros: R$" + jurosCalculado);
        return jurosCalculado;

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

                Historico historico = parcela.getHistorico();
                if (historico != null) {
                    atualizarStatusHistorico(historico); // 🔹 Atualiza o status do histórico
                    historicoRepository.save(historico);
                    criarNotificacao(historico, "❌ Histórico #" + historico.getId() + " agora está em status ATRASADO!");
                }
            }
        }
    }

    @Scheduled(fixedRate = 30000)
    public void verificarParcelasAtrasadasEAtualizarStatus() {
        List<Parcelas> parcelasAtrasadas = parcelasRepository.findByStatusAtrasado();
        Date hoje = new Date();

        for (Parcelas parcela : parcelasAtrasadas) {
            Historico historico = parcela.getHistorico();
            if (historico == null) continue;

            // ✅ Verifica se a data de pagamento ainda não venceu (ou seja, pode voltar a ser PENDENTE)
            if (!parcela.getDataPagamento().before(hoje)) {
                parcela.setPagas(0); // 🔹 Marca como "pendente" numericamente
                parcela.setStatus(StatusParcela.PENDENTE); // 🔹 Altera o status para PENDENTE
                parcelasRepository.save(parcela);

                atualizarStatusHistorico(historico);

                // ✅ Cria notificação de atualização para PENDENTE
                criarNotificacao(historico,
                        "⚠️ Sua parcela de R$ " + parcela.getValor() + " foi atualizada para PENDENTE, pois a data de pagamento ainda não venceu.");

                System.out.println("✅ Parcela #" + parcela.getId() + " atualizada para PENDENTE.");
            }

            // 🔹 Verifica se a parcela atrasada já foi paga
            if (parcela.getStatus() == StatusParcela.PAGO) {
                boolean temOutrasAtrasadas = parcelasRepository.findByHistorico(historico)
                        .stream().anyMatch(p -> p.getStatus() == StatusParcela.ATRASADO);

                // ✅ Se não houver mais atrasadas, muda status do histórico para PENDENTE
                if (!temOutrasAtrasadas && historico.getStatus() == Status.ATRASADO) {
                    historico.setStatus(Status.PENDENTE);
                    historicoRepository.save(historico);

                    criarNotificacao(historico,
                            "⚠️ Seu empréstimo #" + historico.getId() + " voltou para o status PENDENTE.");

                    System.out.println("✅ Histórico #" + historico.getId() + " voltou para PENDENTE.");
                }
            }
        }
    }


    public void atualizarStatusHistorico(Historico historico) {
        if (historico == null) return;

        List<Parcelas> parcelas = parcelasRepository.findByHistorico(historico);

        if (parcelas.isEmpty()) return;

        boolean temAtrasadas = parcelas.stream().anyMatch(parcela -> parcela.getPagas() == -1);
        boolean temPendentes = parcelas.stream().anyMatch(parcela -> parcela.getPagas() == 0);
        boolean todasPagas = parcelas.stream().allMatch(parcela -> parcela.getPagas() == 1);

        if (temAtrasadas) {
            historico.setStatus(Status.ATRASADO);
        } else if (todasPagas) {
            historico.setStatus(Status.PAGO);
        } else {
            historico.setStatus(Status.PENDENTE);
        }

        historicoRepository.save(historico);
    }

}
