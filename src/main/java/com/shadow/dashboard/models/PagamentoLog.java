package com.shadow.dashboard.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Tb_Pagamento_Log")
public class PagamentoLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "historico_id", nullable = false)
    private Historico historico; // Relacionamento com o empréstimo

    @Column(nullable = false)
    private Double valorPago;

    @Column(nullable = false)
    private LocalDateTime dataPagamento;

    public PagamentoLog() {
    }

    public PagamentoLog(Historico historico, Double valorPago) {
        this.historico = historico;
        this.valorPago = valorPago;
        this.dataPagamento = LocalDateTime.now(); // Define a data do pagamento como o momento atual
    }

    public Long getId() {
        return id;
    }

    public Historico getHistorico() {
        return historico;
    }

    public void setHistorico(Historico historico) {
        this.historico = historico;
    }

    public Double getValorPago() {
        return valorPago;
    }

    public void setValorPago(Double valorPago) {
        this.valorPago = valorPago;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) { // ✅ Agora aceita LocalDateTime corretamente
        this.dataPagamento = dataPagamento;
    }
}
