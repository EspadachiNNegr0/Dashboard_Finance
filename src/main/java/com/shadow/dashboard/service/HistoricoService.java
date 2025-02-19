package com.shadow.dashboard.service;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
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

        // 🔹 Calcular o valor total do empréstimo considerando os juros
        double valorTotalComJuros = calcularValorTotalComJuros(historico);
        historico.setValorTotal(valorTotalComJuros);

        // 🔹 Calcular o valor mensal das parcelas
        double valorParcelaMensal = calcularValorMensal(historico);
        historico.setValorMensal(valorParcelaMensal);

        // 🔹 Calcular a data final do empréstimo
        historico.setCreationF(calculaDataFinal(historico));

        // 🔹 Salvar o histórico antes de criar as parcelas
        historico = historicoRepository.save(historico);

        // 🔹 Criar parcelas vinculadas às datas de pagamento
        criarParcelas(historico);

        // 🔹 Criar notificação
        createNotification(historico);

        return historico;
    }

    public void atualizarProximasParcelasEValorMensal(Historico historico, List<Parcelas> parcelasList) {
        // 🔹 Filtra as parcelas que ainda não foram pagas
        List<Parcelas> parcelasNaoPagas = parcelasList.stream()
                .filter(p -> p.getPagas() == 0)
                .sorted(Comparator.comparing(Parcelas::getId))
                .toList();

        if (!parcelasNaoPagas.isEmpty()) {
            // 🔹 Recalcula o valor das próximas parcelas com base no saldo devedor atualizado
            double novoValorMensal = historico.getValorTotal() / parcelasNaoPagas.size();
            historico.setValorMensal(novoValorMensal); // ✅ Atualiza o `valorMensal` do `Historico`

            for (Parcelas parcela : parcelasNaoPagas) {
                parcela.setValor(novoValorMensal);
                parcelasRepository.save(parcela);
            }
        }
    }

    /**
     * Calcula o valor total do empréstimo considerando os juros
     */
    private double calcularValorTotalComJuros(Historico historico) {
        double juros = historico.getPrice() * (historico.getPercentage() / 100.0);
        return historico.getPrice() + juros;
    }

    /**
     * Calcula o valor mensal da parcela com base no total corrigido com juros
     */
    private double calcularValorMensal(Historico historico) {
        double totalComJuros = calcularValorTotalComJuros(historico);
        return totalComJuros / historico.getParcelamento();
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
     * Cria as parcelas com os valores corretos e datas ajustadas
     */
    private void criarParcelas(Historico historico) {
        Calendar calendar = Calendar.getInstance();

        // 🔹 Garante que a data de criação do histórico esteja definida
        if (historico.getCreated() == null) {
            System.out.println("❌ Erro: O campo 'created' do histórico está NULL. Usando a data atual.");
            historico.setCreated(new Date());
        }

        calendar.setTime(historico.getCreated());

        double valorMensal = historico.getValorMensal();

        for (int i = 0; i < historico.getParcelamento(); i++) {
            calendar.add(Calendar.MONTH, 1); // Adiciona um mês para cada parcela

            Parcelas parcela = new Parcelas();
            parcela.setHistorico(historico);
            parcela.setParcelas(historico.getParcelamento());
            parcela.setPagas(0);
            parcela.setValor(valorMensal);

            // ✅ Garante que `dataPagamento` nunca seja NULL
            Date dataParcela = calendar.getTime();
            if (dataParcela == null) {
                System.out.println("⚠️ AVISO: Data gerada é NULL. Usando a data atual.");
                dataParcela = new Date();
            }
            parcela.setDataPagamento(dataParcela);

            parcelasRepository.save(parcela);
            System.out.println("✅ Parcela salva com data: " + dataParcela);
        }
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

    // 🔹 Método para pagar uma parcela e atualizar o status do histórico
    public void atualizarStatusParcelaPaga(Long parcelaId, double valorPago) {
        Parcelas parcela = parcelasRepository.findById(parcelaId)
                .orElseThrow(() -> new RuntimeException("Parcela não encontrada"));

        Historico historico = parcela.getHistorico();

        // ✅ Marca a parcela como paga
        parcela.setPagas(1);
        parcela.setStatus("PAGO");
        parcelasRepository.save(parcela);

        // ✅ Atualiza o status do histórico se não houver mais parcelas atrasadas
        atualizarStatusHistorico(historico);

        // ✅ Cria uma notificação informando que a parcela foi paga
        criarNotificacao(historico.getCliente(),
                "✅ Pagamento de R$ " + valorPago + " realizado na parcela #" + parcela.getId());
    }

    @Scheduled(fixedRate = 30000) // Executa a cada 60 segundos (1 minuto)
    public void verificarParcelasAtrasadasEAtualizarStatus() {
        List<Parcelas> parcelasAtrasadas = parcelasRepository.findByStatusAtrasado();

        for (Parcelas parcela : parcelasAtrasadas) {
            Historico historico = parcela.getHistorico();
            if (historico == null) continue;

            // 🔹 Verifica se a parcela atrasada já foi paga
            if ("PAGO".equals(parcela.getStatus())) {
                boolean temOutrasAtrasadas = parcelasRepository.findByHistorico(historico)
                        .stream().anyMatch(p -> "ATRASADO".equals(p.getStatus()));

                // ✅ Se não houver mais atrasadas, muda status para PENDENTE
                if (!temOutrasAtrasadas && historico.getStatus() == Status.ATRASADO) {
                    historico.setStatus(Status.PENDENTE);
                    historicoRepository.save(historico);

                    // ✅ Cria notificação informando a atualização
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


    public void criarNovaParcelaComValorRestante(Parcelas ultimaParcela, double valorPago, double valorMensal) {
        double valorRestante = valorMensal - valorPago;

        // 🔹 Só cria uma nova parcela se houver valor restante maior que 0.01 para evitar erro de arredondamento
        if (valorRestante > 0.01) {
            Parcelas novaParcela = new Parcelas();
            Historico historico = ultimaParcela.getHistorico();

            // ✅ Define corretamente a nova parcela dentro do mesmo histórico
            novaParcela.setHistorico(historico);
            novaParcela.setValor(valorRestante);
            novaParcela.setPagas(0); // 🔹 Nova parcela deve ser paga

            // ✅ Atualiza o número total de parcelas no histórico e na nova parcela
            historico.setParcelamento(historico.getParcelamento() + 1);
            historicoRepository.save(historico); // 🔹 Salva o histórico atualizado

            novaParcela.setParcelas(historico.getParcelamento());

            // ✅ Define a data da nova parcela como o mês seguinte
            Calendar calendar = Calendar.getInstance();
            if (ultimaParcela.getDataPagamento() != null) {
                calendar.setTime(ultimaParcela.getDataPagamento());
            } else {
                calendar.setTime(new Date());
            }
            calendar.add(Calendar.MONTH, 1);
            novaParcela.setDataPagamento(calendar.getTime());

            // ✅ Salva a nova parcela no banco de dados
            parcelasRepository.save(novaParcela);
            System.out.println("✅ Nova parcela criada: ID " + novaParcela.getId() + " com valor restante de R$ " + valorRestante);

            // ✅ Atualiza o status do histórico após a criação da nova parcela
            atualizarStatusHistorico(historico);
        } else {
            System.out.println("⚠️ Nenhuma nova parcela foi criada. Motivos possíveis:");
            System.out.println("   🔸 O valor restante é zero ou muito pequeno: " + valorRestante);
            System.out.println("   🔸 A última parcela pode já ter sido paga.");
        }
    }


    /**
     * Atualiza automaticamente o status de parcelas vencidas a cada 1 minuto.
     */

    @Scheduled(fixedRate = 30000) // Executa a cada 60 segundos (1 min)
    public void atualizarStatusParcelasVencidas() {
        List<Parcelas> parcelas = parcelasRepository.findAll();
        Date hoje = new Date();

        for (Parcelas parcela : parcelas) {
            if (parcela.getDataPagamento() == null) continue;

            if (parcela.getDataPagamento().before(hoje) && parcela.getPagas() == 0) {
                parcela.setPagas(-1);
                parcela.setStatus("ATRASADO");
                parcelasRepository.save(parcela);

                Historico historico = parcela.getHistorico();
                if (historico != null) {
                    atualizarStatusHistorico(historico); // ✅ Atualiza status do histórico
                    criarNotificacao(historico.getCliente(),
                            "❌ Sua parcela de R$ " + parcela.getValor() + " venceu e está ATRASADA!");
                }
            }
        }
    }

    /**
     * Cria uma notificação associada ao cliente.
     */
    private void criarNotificacao(Clientes cliente, String mensagem) {
        if (cliente == null) return;

        Notification notificacao = new Notification();
        notificacao.setMessage(mensagem);
        notificacao.setCreatedAt(LocalDateTime.now());
        notificacao.setRead(false);

        notificationRepository.save(notificacao);
    }
}


