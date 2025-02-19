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


    public void criarNovaParcelaComValorRestante(Parcelas ultimaParcela, double valorPago, double valorMensal) {
        double valorRestante = valorMensal - valorPago;
        Historico historico = ultimaParcela.getHistorico();

        // 🔹 Verifica se a última parcela é realmente a última do parcelamento original
        boolean isUltimaParcelaDoParcelamento = ultimaParcela.getParcelas() == historico.getParcelamento();

        System.out.println("📊 Verificação de criação de nova parcela:");
        System.out.println("   🔸 Valor Restante: " + valorRestante);
        System.out.println("   🔸 Última Parcela no Histórico: " + ultimaParcela.getParcelas());
        System.out.println("   🔸 Parcelamento Original do Histórico: " + historico.getParcelamento());
        System.out.println("   🔸 É a Última Parcela do Parcelamento? " + isUltimaParcelaDoParcelamento);

        // ✅ Define a data de vencimento da nova parcela para o próximo mês
        Calendar calendar = Calendar.getInstance();
        if (ultimaParcela.getDataPagamento() != null) {
            calendar.setTime(ultimaParcela.getDataPagamento());
        } else {
            calendar.setTime(new Date());
        }
        calendar.add(Calendar.MONTH, 1);
        Date proximaDataPagamento = calendar.getTime();

        // 🔍 Verifica se já existe uma parcela com a data esperada
        boolean existeParcelaFutura = parcelasRepository.findByHistorico(historico).stream()
                .anyMatch(parcela -> parcela.getDataPagamento() != null
                        && parcela.getDataPagamento().equals(proximaDataPagamento));

        System.out.println("📅 Próxima Data Esperada: " + proximaDataPagamento);
        System.out.println("❓ Existe uma parcela futura já registrada? " + existeParcelaFutura);

        // ✅ Só cria uma nova parcela se:
        // 1. O valor restante for significativo (maior que 0.01 para evitar problemas de arredondamento)
        // 2. A última parcela atual for realmente a última do parcelamento original
        // 3. Não existir uma parcela futura já registrada na data esperada
        if (valorRestante > 0.01 && isUltimaParcelaDoParcelamento && !existeParcelaFutura) {
            Parcelas novaParcela = new Parcelas();

            // ✅ Configura a nova parcela no mesmo histórico
            novaParcela.setHistorico(historico);
            novaParcela.setValor(valorRestante);
            novaParcela.setPagas(0); // 🔹 Nova parcela deve ser paga
            novaParcela.setStatus(StatusParcela.PENDENTE);

            // ✅ Incrementa o número de parcelas no histórico e define na nova parcela
            historico.setParcelamento(historico.getParcelamento() + 1);
            historicoRepository.save(historico); // 🔹 Salva o histórico atualizado

            novaParcela.setParcelas(historico.getParcelamento());
            novaParcela.setDataPagamento(proximaDataPagamento);

            // ✅ Salva a nova parcela no banco de dados
            parcelasRepository.save(novaParcela);
            System.out.println("✅ Nova parcela criada: ID " + novaParcela.getId() + " com valor restante de R$ " + valorRestante);

            // ✅ Atualiza o status do histórico após a criação da nova parcela
            atualizarStatusHistorico(historico);
        } else {
            System.out.println("⚠️ Nenhuma nova parcela foi criada. Motivos possíveis:");
            if (valorRestante <= 0.01) {
                System.out.println("   🔸 O valor restante é zero ou muito pequeno: " + valorRestante);
            }
            if (!isUltimaParcelaDoParcelamento) {
                System.out.println("   🔸 A parcela atual não é a última do parcelamento original.");
            }
            if (existeParcelaFutura) {
                System.out.println("   🔸 Já existe uma parcela registrada para a próxima data esperada.");
            }
        }
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


