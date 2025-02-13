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

    /**
     * Calcula o valor total do empréstimo SEM considerar os juros
     */
    public double calcularValorTotalSemJuros(Historico historico) {
        return historico.getPrice(); // Apenas o valor original do empréstimo
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

    /**
     * Retorna a soma de todos os empréstimos ativos
     */
    public double somaDeTodosOsEmprestimos(List<Historico> historias) {
        return historias.stream().mapToDouble(Historico::getPrice).sum();
    }

    public List<Date> calculaDatasDePagamento(Historico historico) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(historico.getCreated());

        // Garante que a data inicial seja em janeiro
        if (calendar.get(Calendar.MONTH) != Calendar.JANUARY) {
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1); // Começa no 1º dia de janeiro
        }

        List<Date> datas = new ArrayList<>();
        for (int i = 0; i < historico.getParcelamento(); i++) {
            calendar.add(Calendar.MONTH, 1); // Adiciona um mês à data
            datas.add(calendar.getTime());
        }
        return datas;
    }

    /**
     * Cria uma notificação para pagamentos
     */
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

        // 🔹 Obtém o valor mensal esperado
        double valorMensal = historico.getValorMensal();

        // 🔹 Verifica se o valor pago foi menor que o valor mensal (apenas juros pagos)
        if (valorPago < valorMensal) {
            System.out.println("⚠️ Apenas juros foram pagos! Criando nova parcela para o valor restante.");

            // Calcula o valor restante
            double valorRestante = valorMensal - valorPago;

            // Criar nova parcela para o valor restante
            Parcelas novaParcela = new Parcelas();
            novaParcela.setHistorico(historico);
            novaParcela.setParcelas(1); // Apenas uma parcela adicional
            novaParcela.setPagas(0); // Ainda precisa ser paga
            novaParcela.setValor(valorRestante);

            // Define data da nova parcela para o próximo mês
            Calendar calendar = Calendar.getInstance();
            if (parcela.getDataPagamento() != null) {
                calendar.setTime(parcela.getDataPagamento());
            } else {
                calendar.setTime(new Date());
            }
            calendar.add(Calendar.MONTH, 1);
            novaParcela.setDataPagamento(calendar.getTime());

            parcelasRepository.save(novaParcela);
            System.out.println("✅ Nova parcela criada com valor restante de R$ " + valorRestante);
        } else {
            // 🔹 Se o valor total foi pago, a parcela é considerada quitada
            parcela.setPagas(1);
            parcelasRepository.save(parcela);
        }

        // 🔹 Criar notificação de pagamento
        criarNotificacao(historico.getCliente(),
                "✅ Pagamento de R$ " + valorPago + " realizado na parcela #" + parcela.getId());

        // 🔹 Atualiza o status do histórico
        atualizarStatusHistoricoAoPagar(historico);
    }

    /**
     * Verifica se ainda existem parcelas atrasadas e ajusta o status do histórico
     */
    private void atualizarStatusHistoricoAoPagar(Historico historico) {
        if (historico == null) return;

        // Verifica se ainda existem parcelas atrasadas (-1)
        boolean temParcelasAtrasadas = parcelasRepository.findByHistorico(historico)
                .stream().anyMatch(parcela -> parcela.getPagas() == -1);

        // Se não houver mais parcelas atrasadas, retorna o status do histórico para PENDENTE
        if (!temParcelasAtrasadas && historico.getStatus() == Status.ATRASADO) {
            historico.setStatus(Status.PENDENTE);
            historicoRepository.save(historico);

            // Criar notificação de alteração de status
            criarNotificacao(historico.getCliente(),
                    "⚠️ Seu empréstimo #" + historico.getId() + " voltou para PENDENTE.");
        }
    }

    public void criarNovaParcelaComValorRestante(Parcelas ultimaParcela, double valorPago, double valorMensal) {
        double valorRestante = valorMensal - valorPago;

        if (valorRestante > 0) {
            Parcelas novaParcela = new Parcelas();
            novaParcela.setHistorico(ultimaParcela.getHistorico());
            novaParcela.setParcelas(1);
            novaParcela.setPagas(0); // 🔹 Nova parcela precisa ser paga
            novaParcela.setValor(valorRestante);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(ultimaParcela.getDataPagamento() != null ? ultimaParcela.getDataPagamento() : new Date());
            calendar.add(Calendar.MONTH, 1);
            novaParcela.setDataPagamento(calendar.getTime());

            parcelasRepository.save(novaParcela);
            System.out.println("✅ Nova parcela criada com valor restante de R$ " + valorRestante);

            // 🔹 Atualiza o histórico para refletir o aumento do número de parcelas
            Historico historico = ultimaParcela.getHistorico();
            historico.setParcelamento(historico.getParcelamento() + 1);
            historico.setValorTotal(historico.getValorTotal() + valorRestante); // 🔹 Atualiza o valor total
            historicoRepository.save(historico);

            // 🔹 Atualiza o novo valor mensal das parcelas pendentes
            List<Parcelas> parcelasList = parcelasRepository.findByHistorico(historico);
            atualizarProximasParcelasEValorMensal(historico, parcelasList);
        }
    }

    /**
     * Atualiza automaticamente o status de parcelas vencidas a cada 1 minuto.
     */

    @Scheduled(fixedRate = 60000) // Executa a cada 60 segundos (1 min)
    public void atualizarStatusParcelasVencidas() {
        List<Parcelas> parcelas = parcelasRepository.findAll();
        Date hoje = new Date();

        for (Parcelas parcela : parcelas) {
            // ✅ Verifica se `dataPagamento` não é nula antes de comparar
            if (parcela.getDataPagamento() == null) {
                System.out.println("⚠️ AVISO: Parcela ID " + parcela.getId() + " possui dataPagamento NULL. Definindo como data atual.");
                parcela.setDataPagamento(hoje); // Define uma data para evitar erro
                parcelasRepository.save(parcela);
                continue; // Passa para a próxima parcela
            }

            if (parcela.getDataPagamento().before(hoje)) {
                if (parcela.getPagas() == 0) {
                    parcela.setPagas(-1); // 🔹 Define como ATRASADO (-1)
                    parcelasRepository.save(parcela);

                    // 🔹 Verificação extra para evitar NullPointerException
                    Optional.ofNullable(parcela.getHistorico())
                            .map(Historico::getCliente)
                            .ifPresent(cliente -> criarNotificacao(cliente,
                                    "❌ Sua parcela de R$ " + parcela.getValor() + " venceu e está PENDENTE!"));
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


