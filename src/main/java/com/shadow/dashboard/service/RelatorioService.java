package com.shadow.dashboard.service;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class RelatorioService {

    private final RelatorioSaidaRepository relatorioSaidaRepository;
    private final RelatorioEntradaRepository relatorioEntradaRepository;
    private final RelatorioFinanceiroRepository relatorioFinanceiroRepository;
    private final HistoricoRepository historicoRepository;
    private final ParcelasRepository parcelasRepository;
    private final RelatorioProjetadaRepository relatorioProjetadaRepository;

    @Autowired
    public RelatorioService(RelatorioSaidaRepository relatorioSaidaRepository, RelatorioEntradaRepository relatorioEntradaRepository, RelatorioFinanceiroRepository relatorioFinanceiroRepository, HistoricoRepository historicoRepository, ParcelasRepository parcelasRepository, RelatorioProjetadaRepository relatorioProjetadaRepository) {
        this.relatorioSaidaRepository = relatorioSaidaRepository;
        this.relatorioEntradaRepository = relatorioEntradaRepository;
        this.relatorioFinanceiroRepository = relatorioFinanceiroRepository;
        this.historicoRepository = historicoRepository;
        this.parcelasRepository = parcelasRepository;
        this.relatorioProjetadaRepository = relatorioProjetadaRepository;
    }

    private int gerarCodigoUnico() {
        return new Random().ints(100000, 999999)
                .filter(codigo -> !historicoRepository.existsByCodigo(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Não foi possível gerar um código único."));
    }

    public void criarRelatorioSaida(Historico historico) {
        if (!relatorioSaidaRepository.existsByHistorico(historico)) {
            List<Parcelas> parcelas = parcelasRepository.findByHistorico(historico);

            RelatorioSaida relatorioSaida = new RelatorioSaida();
            relatorioSaida.setCodigo(gerarCodigoUnico());
            relatorioSaida.setValor(historico.getPrice());
            relatorioSaida.setBanco(historico.getBancoSaida().getNome());
            relatorioSaida.setData(historico.getCreated());
            relatorioSaida.setStatus(StatusR.Saida);
            relatorioSaida.setHistorico(historico);

            relatorioSaidaRepository.save(relatorioSaida);
            criarRelatorioFinanceiroSaida(relatorioSaida);

            System.out.println("✅ [SUCESSO] Relatório de saída salvo com sucesso!");
        }
    }

    public void criarRelatorioProjetada(Historico historicoSalvo) {
        List<Parcelas> parcelas = parcelasRepository.findByHistorico(historicoSalvo);

        if (parcelas.isEmpty()) {
            System.out.println("⚠️ Nenhuma parcela encontrada para o histórico ID: " + historicoSalvo.getId());
            return;
        }

        for (Parcelas parcela : parcelas) {
            Optional<RelatorioProjetada> existente = relatorioProjetadaRepository.findByHistorico(historicoSalvo);

            if (existente.isPresent()) {
                System.out.println("⚠️ Relatório Projetado já existe para a parcela ID: " + parcela.getId());
                continue;
            }

            RelatorioProjetada relatorioProjetado = new RelatorioProjetada();
            relatorioProjetado.setCodigo(gerarCodigoUnico());
            relatorioProjetado.setValor(parcela.getValor());
            relatorioProjetado.setJuros(parcela.getValorJuros());
            relatorioProjetado.setAmortizacao(parcela.getValorAmortizado());
            relatorioProjetado.setBanco(parcela.getBancoEntrada());
            relatorioProjetado.setData(parcela.getDataPagamento());
            relatorioProjetado.setStatus(StatusR.Projetada);
            relatorioProjetado.setHistorico(historicoSalvo);
            relatorioProjetado.setParcela(parcela);

            relatorioProjetadaRepository.save(relatorioProjetado);
            criarRelatorioFinanceiroProjetada(relatorioProjetado);

            System.out.println("✅ [SUCESSO] Relatório Projetado criado para parcela ID: " + parcela.getId());
        }
    }

    public void criarRelatorioFinanceiroProjetada(RelatorioProjetada relatorioProjetada) {
        RelatorioFinanceiro relatorioFinanceiro = new RelatorioFinanceiro();
        relatorioFinanceiro.setCodigo(gerarCodigoUnico());
        relatorioFinanceiro.setValor(relatorioProjetada.getValor());
        relatorioFinanceiro.setJuros(relatorioProjetada.getJuros());
        relatorioFinanceiro.setAmortizacao(relatorioProjetada.getAmortizacao());
        relatorioFinanceiro.setBanco(relatorioProjetada.getBanco());
        relatorioFinanceiro.setData(relatorioProjetada.getData());
        relatorioFinanceiro.setStatus(relatorioProjetada.getStatus());
        relatorioFinanceiro.setHistorico(relatorioProjetada.getHistorico());
        relatorioFinanceiro.setRelatorioProjetada(relatorioProjetada);
        relatorioFinanceiroRepository.save(relatorioFinanceiro);
    }

    public void criarRelatorioProjetadaParaParcela(Parcelas parcela) {
        Historico historicoSalvo = parcela.getHistorico();

        List<RelatorioProjetada> existentes = relatorioProjetadaRepository.findByHistoricoAndParcela(historicoSalvo, parcela);

        if (!existentes.isEmpty()) {
            System.out.println("⚠️ Relatório Projetado já existe para a parcela ID: " + parcela.getId() + ". Ignorando criação.");
            return;
        }

        RelatorioProjetada relatorioProjetado = new RelatorioProjetada();
        relatorioProjetado.setCodigo(gerarCodigoUnico());
        relatorioProjetado.setValor(parcela.getValor());
        relatorioProjetado.setJuros(0.0);
        relatorioProjetado.setAmortizacao(0.0);
        relatorioProjetado.setBanco(parcela.getBancoEntrada());
        relatorioProjetado.setData(parcela.getDataPagamento());
        relatorioProjetado.setStatus(StatusR.Projetada);
        relatorioProjetado.setHistorico(historicoSalvo);
        relatorioProjetado.setParcela(parcela);

        relatorioProjetadaRepository.save(relatorioProjetado);
        criarRelatorioFinanceiroProjetada(relatorioProjetado);

        System.out.println("✅ [SUCESSO] Relatório Projetado criado para parcela ID: " + parcela.getId());
    }

    public void criarRelatorioEntrada(Historico historicoSalvo) {
        List<Parcelas> parcelas = parcelasRepository.findByHistorico(historicoSalvo);

        if (parcelas.isEmpty()) {
            System.out.println("⚠️ Nenhuma parcela encontrada para o histórico ID: " + historicoSalvo.getId());
            return;
        }

        for (Parcelas parcela : parcelas) {
            // 🔹 Verifica se já existe um relatório para essa parcela
            if (!relatorioEntradaRepository.existsByHistorico(historicoSalvo)) {
                RelatorioEntrada relatorioEntrada = new RelatorioEntrada();
                relatorioEntrada.setCodigo(gerarCodigoUnico()); // ✅ Código único por parcela
                relatorioEntrada.setValor(parcela.getValor());
                relatorioEntrada.setJuros(parcela.getValorJuros());
                relatorioEntrada.setAmortizacao(parcela.getValorAmortizado());
                relatorioEntrada.setBanco(parcela.getBancoEntrada());
                relatorioEntrada.setData(parcela.getDataPagamento());
                relatorioEntrada.setStatus(StatusR.Entrada);
                relatorioEntrada.setHistorico(historicoSalvo);

                relatorioEntradaRepository.save(relatorioEntrada);

                // 🔹 Criar o Relatório Financeiro correspondente
                criarRelatorioFinanceiroEntrada(relatorioEntrada);

                System.out.println("✅ [SUCESSO] Relatório de entrada criado para parcela ID: " + parcela.getId());
            } else {
                System.out.println("⚠️ Relatório de entrada já existe para a parcela ID: " + parcela.getId());
            }
        }
    }

    public void criarRelatorioFinanceiroEntrada(RelatorioEntrada relatorioEntrada) {
        RelatorioFinanceiro relatorioFinanceiro = new RelatorioFinanceiro();
        relatorioFinanceiro.setCodigo(relatorioEntrada.getCodigo());
        relatorioFinanceiro.setValor(relatorioEntrada.getValor());
        relatorioFinanceiro.setJuros(relatorioEntrada.getJuros());
        relatorioFinanceiro.setAmortizacao(relatorioEntrada.getAmortizacao());
        relatorioFinanceiro.setBanco(relatorioEntrada.getBanco());
        relatorioFinanceiro.setData(relatorioEntrada.getData());
        relatorioFinanceiro.setStatus(relatorioEntrada.getStatus());
        relatorioFinanceiro.setHistorico(relatorioEntrada.getHistorico());
        relatorioFinanceiro.setRelatorioEntrada(relatorioEntrada);
        relatorioFinanceiroRepository.save(relatorioFinanceiro);
    }

    private void criarRelatorioFinanceiroSaida(RelatorioSaida relatorioSaida) {
        RelatorioFinanceiro relatorioFinanceiro = new RelatorioFinanceiro();
        relatorioFinanceiro.setCodigo(relatorioSaida.getCodigo());
        relatorioFinanceiro.setValor(relatorioSaida.getValor());
        relatorioFinanceiro.setBanco(relatorioSaida.getBanco());
        relatorioFinanceiro.setData(relatorioSaida.getData());
        relatorioFinanceiro.setStatus(relatorioSaida.getStatus());
        relatorioFinanceiro.setHistorico(relatorioSaida.getHistorico());
        relatorioFinanceiro.setRelatorioSaida(relatorioSaida);
        relatorioFinanceiroRepository.save(relatorioFinanceiro);
    }

}
