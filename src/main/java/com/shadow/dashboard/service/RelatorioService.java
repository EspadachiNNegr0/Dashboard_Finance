package com.shadow.dashboard.service;

import com.shadow.dashboard.models.*;
import com.shadow.dashboard.repository.HistoricoRepository;
import com.shadow.dashboard.repository.RelatorioEntradaRepository;
import com.shadow.dashboard.repository.RelatorioSaidaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
public class RelatorioService {

    private final RelatorioSaidaRepository relatorioSaidaRepository;
    private final RelatorioEntradaRepository relatorioEntradaRepository;
    private final HistoricoRepository historicoRepository;

    @Autowired
    public RelatorioService(RelatorioSaidaRepository relatorioSaidaRepository, HistoricoRepository historicoRepository, RelatorioEntradaRepository relatorioEntradaRepository) {
        this.relatorioSaidaRepository = relatorioSaidaRepository;
        this.historicoRepository = historicoRepository;
        this.relatorioEntradaRepository = relatorioEntradaRepository;
    }

        private int gerarCodigoUnico() {
            return new Random().ints(100000, 999999)
                    .filter(codigo -> !historicoRepository.existsByCodigo(codigo))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Não foi possível gerar um código único."));
        }

    public void criarRelatorioSaida(Historico historico) {

        RelatorioSaida relatorioSaida = new RelatorioSaida();
        relatorioSaida.setCodigo(gerarCodigoUnico());
        relatorioSaida.setValor(historico.getPrice());
        relatorioSaida.setBanco(String.valueOf(historico.getBancoSaida().getNome()));
        relatorioSaida.setData(new Date());
        relatorioSaida.setStatus(StatusR.Saida);
        relatorioSaida.setHistorico(historico); // 🔹 Associando ao histórico


        relatorioSaidaRepository.save(relatorioSaida);
        System.out.println("✅ [SUCESSO] Relatório de saída salvo com sucesso!");

    }


    public void criarRelatorioEntrada(Parcelas parcela, Historico historicoSalvo) {
            RelatorioEntrada relatorioEntrada = new RelatorioEntrada();
            relatorioEntrada.setCodigo(gerarCodigoUnico());
            relatorioEntrada.setValor(parcela.getValor());
            relatorioEntrada.setBanco(parcela.getBancoEntrada());
            relatorioEntrada.setData(parcela.getDataPagamento());
            relatorioEntrada.setStatus(StatusR.Entrada);
            relatorioEntrada.setHistorico(historicoSalvo);


            relatorioEntradaRepository.save(relatorioEntrada);
        System.out.println("✅ [SUCESSO] Relatório de entrada salvo com sucesso!");
        }




}
