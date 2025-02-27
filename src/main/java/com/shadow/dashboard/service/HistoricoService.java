package com.shadow.dashboard.service;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    /**
     * Salva o histórico e cria suas parcelas e notificações
     */
    public Historico saveHistoryAndCreateNotification(Historico historico) {
        // Validações
        if (historico.getCreated() == null || historico.getParcelamento() <= 0) {
            throw new IllegalArgumentException("Os campos 'created' e 'parcelamento' são obrigatórios.");
        }

        // 🔹 Gerar código único antes de salvar
        if (historico.getCodigo() == 0) { // Se ainda não tiver código, gera um novo
            historico.setCodigo(gerarCodigoUnico());
        }

        // 🔹 Calcular a data final do empréstimo
        historico.setCreationF(calculaDataFinal(historico));

        // 🔹 Salvar o histórico antes de criar as parcelas
        historico = historicoRepository.save(historico);

        // 🔹 Criar notificação
        createNotification(historico);

        return historico;
    }

    private int gerarCodigoUnico() {
        Random random = new Random();
        int codigo;

        do {
            codigo = random.nextInt(900000) + 100000; // Gera um número entre 100000 e 999999
        } while (historicoRepository.existsByCodigo(codigo));

        return codigo;
    }

    /**
     * Calcula a data final do empréstimo
     */
    private Date calculaDataFinal(Historico historico) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(historico.getCreated());
        calendar.add(Calendar.MONTH, historico.getParcelamento());
        return calendar.getTime();
    }

    /**
     * Cria uma notificação associada ao histórico e ao cliente
     */
    private void createNotification(Historico historico) {
        if (historico.getCliente().getId() == null) {
            System.out.println("ID do cliente é nulo no objeto Historico.");
            return;
        }

        Clientes cliente = clienteRepository.findById(historico.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para o ID: " + historico.getCliente().getId()));

        String message = "📢 Novo empréstimo registrado para " + cliente.getNome() +
                " no valor de R$ " + historico.getPrice() +
                " com juros de " + historico.getPercentage() + "%.";

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    public void criarNotificacao(Historico historico, double valorPago, String tipoPagamento) {
        if (historico.getCliente() == null) {
            return; // Não cria notificação se não houver cliente associado
        }

        Notification notification = new Notification();
        notification.setMessage("📢 " + tipoPagamento + (valorPago > 0 ? " de R$ " + valorPago : "")
                + " registrado para o empréstimo #" + historico.getId()
                + " do cliente " + historico.getCliente().getNome() + ".");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        notificationRepository.save(notification);
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
                criarNotificacao(historico.getCliente(),
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

                    criarNotificacao(historico.getCliente(),
                            "⚠️ Seu empréstimo #" + historico.getId() + " voltou para o status PENDENTE.");

                    System.out.println("✅ Histórico #" + historico.getId() + " voltou para PENDENTE.");
                }
            }
        }
    }


    public void atualizarStatusHistorico(Historico historico) {
        if (historico == null) return;

        // 🔹 Obtém todas as parcelas associadas ao histórico
        List<Parcelas> parcelas = parcelasRepository.findByHistorico(historico);

        // 🔹 Se não houver parcelas, mantém o status atual
        if (parcelas.isEmpty()) return;

        // 🔹 Verifica se existem parcelas atrasadas (-1)
        boolean temParcelasAtrasadas = parcelas.stream().anyMatch(parcela -> parcela.getPagas() == -1);

        // 🔹 Verifica se existem parcelas pendentes (0)
        boolean temParcelasPendentes = parcelas.stream().anyMatch(parcela -> parcela.getPagas() == 0);

        // 🔹 Obtém a última parcela registrada
        Parcelas ultimaParcela = parcelas.stream()
                .max(Comparator.comparing(Parcelas::getId))
                .orElse(null);

        // ✅ Se houver parcelas atrasadas, o status do histórico será ATRASADO
        if (temParcelasAtrasadas) {
            historico.setStatus(Status.ATRASADO);
        }
        // ✅ Se a última parcela foi paga e não há parcelas pendentes, o histórico fica como PAGO
        else if (ultimaParcela != null && ultimaParcela.getPagas() == 1 && !temParcelasPendentes) {
            historico.setStatus(Status.PAGO);
            System.out.println("✅ Histórico #" + historico.getId() + " foi atualizado para PAGO.");
        }
        // ✅ Se ainda houver parcelas a serem pagas, mantém como PENDENTE
        else {
            historico.setStatus(Status.PENDENTE);
        }

        historicoRepository.save(historico);
    }

    @Transactional
    public void recalcularParcelas(Historico historico) {
        List<Parcelas> parcelas = parcelasRepository.findByHistorico(historico);

        // ✅ Novo valor de cada parcela
        double novoValorParcela = historico.getPrice() / historico.getParcelamento();

        // 🔹 Verifica se o número de parcelas diminuiu ou aumentou
        int diferencaParcelas = historico.getParcelamento() - parcelas.size();

        // 🔹 Atualiza as parcelas existentes
        for (int i = 0; i < Math.min(historico.getParcelamento(), parcelas.size()); i++) {
            Parcelas parcela = parcelas.get(i);
            parcela.setValor(novoValorParcela);
            parcelasRepository.save(parcela);
        }

        // 🔹 Se o número de parcelas aumentou, cria novas parcelas
        if (diferencaParcelas > 0) {
            for (int i = parcelas.size(); i < historico.getParcelamento(); i++) {
                Parcelas novaParcela = new Parcelas();
                novaParcela.setHistorico(historico);
                novaParcela.setValor(novoValorParcela);
                novaParcela.setParcelas(i + 1);
                novaParcela.setPagas(0);
                novaParcela.setStatus(StatusParcela.PENDENTE);

                // 🔹 Define a data de pagamento da nova parcela (mês seguinte)
                Calendar dataPagamento = Calendar.getInstance();
                dataPagamento.setTime(historico.getCreated());
                dataPagamento.add(Calendar.MONTH, i);

                novaParcela.setDataPagamento(dataPagamento.getTime());

                parcelasRepository.save(novaParcela);
            }
        }

        // 🔹 Se o número de parcelas diminuiu, remove as parcelas excedentes
        else if (diferencaParcelas < 0) {
            List<Parcelas> parcelasParaRemover = parcelas.subList(historico.getParcelamento(), parcelas.size());
            parcelasRepository.deleteAll(parcelasParaRemover);
        }

        System.out.println("✅ Parcelas recalculadas com sucesso!");
    }

    public boolean quitarEmprestimoSeNecessario(Historico historico, double valorPago, double valorRestante) {
        if (valorPago == valorRestante) {
            List<Parcelas> parcelasDoHistorico = parcelasRepository.findByHistoricoId(historico.getId());

            for (Parcelas p : parcelasDoHistorico) {
                p.setPagas(1);
                p.setStatus(StatusParcela.PAGO);
                p.setValorPago(Optional.ofNullable(p.getValorPago()).orElse(0.0));
            }

            parcelasRepository.saveAll(parcelasDoHistorico);

            // ✅ Atualiza o status do histórico como "QUITADO"
            historico.setStatus(Status.PAGO);
            atualizarStatusHistorico(historico);
            return true; // Retorna true se o empréstimo foi quitado
        }
        return false; // Retorna false se ainda restam pagamentos
    }


    /**
     * Atualiza automaticamente o status de parcelas vencidas a cada 1 minuto.
     */

    @Scheduled(fixedRate = 30000)
    public void atualizarStatusParcelasVencidas() {
        List<Parcelas> parcelas = parcelasRepository.findAll();
        Date hoje = new Date();

        for (Parcelas parcela : parcelas) {
            if (parcela.getDataPagamento() == null) continue;

            Historico historico = parcela.getHistorico();

            // 🔹 Atualiza para ATRASADO se a data de pagamento estiver no passado e não estiver paga
            if (parcela.getDataPagamento().before(hoje) && parcela.getPagas() == 0) {
                parcela.setPagas(-1);
                parcela.setStatus(StatusParcela.ATRASADO);
                parcelasRepository.save(parcela);

                if (historico != null) {
                    atualizarStatusHistorico(historico);
                    criarNotificacao(historico.getCliente(),
                            "❌ Sua parcela de R$ " + parcela.getValor() + " venceu e está ATRASADA!");
                }
            }
        }
    }


    private void criarNotificacao(Clientes cliente, String mensagem) {
        if (cliente == null) return;

        Notification notificacao = new Notification();
        notificacao.setMessage(mensagem);
        notificacao.setCreatedAt(LocalDateTime.now());
        notificacao.setRead(false);

        notificationRepository.save(notificacao);
    }

    public void criarParcela(Historico historicoSalvo) {
        int totalParcelas = historicoSalvo.getParcelamento(); // Número de parcelas
        double valorParcela = historicoSalvo.getPrice() / totalParcelas; // Valor de cada parcela

        Calendar calendario = Calendar.getInstance(); // Obtem a data atual
        calendario.setTime(historicoSalvo.getCreated()); // Usa a data do empréstimo como referência

        for (int i = 1; i <= totalParcelas; i++) {
            calendario.add(Calendar.MONTH, 1); // Adiciona um mês para cada parcela

            Parcelas parcela = new Parcelas();
            parcela.setHistorico(historicoSalvo);
            parcela.setParcelas(i); // Número da parcela (1, 2, 3...)
            parcela.setValor(valorParcela);
            parcela.setDataPagamento(calendario.getTime()); // Define a data futura
            parcela.setStatus(StatusParcela.PENDENTE); // Inicialmente, a parcela está pendente
            parcela.setPagas(0); // Nenhuma parcela foi paga ainda
            parcela.setBancoEntrada(null); // Banco de Entrada será definido apenas no pagamento

            parcelasRepository.save(parcela); // Salva a parcela no banco
        }
    }

    public void adicionarValorSobraNaProximaParcela(Parcelas parcela, double valorSobra) {
        if (valorSobra <= 0) return; // Se não houver sobra, não faz nada

        // 🔹 Buscar a próxima parcela pendente dentro do mesmo histórico
        Optional<Parcelas> proximaParcelaOptional = parcelasRepository
                .findFirstByHistoricoAndPagasOrderByDataPagamentoAsc(parcela.getHistorico(), 0);

        if (proximaParcelaOptional.isPresent()) {
            Parcelas proximaParcela = proximaParcelaOptional.get();

            // 🔹 Somar `valorSobra` ao valor já presente na próxima parcela
            double novoValor = proximaParcela.getValor() + valorSobra;
            proximaParcela.setValor(novoValor);

            // 🔹 Atualizar a sobra para continuar acumulando corretamente
            double novaSobra = proximaParcela.getValorSobra() + valorSobra;
            proximaParcela.setValorSobra(novaSobra);

            parcelasRepository.save(proximaParcela); // ✅ Salvar no banco

            System.out.println("✅ Valor sobrando de " + valorSobra + " foi adicionado à próxima parcela ID: " + proximaParcela.getId());
        } else {
            System.out.println("⚠️ Não há mais parcelas pendentes para adicionar o valor sobrando.");
        }
    }
}


