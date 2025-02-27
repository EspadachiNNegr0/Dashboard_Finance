package com.shadow.dashboard.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tb_parcelas")
public class Parcelas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "historico_id", nullable = false)
    private Historico historico;

    @Column(nullable = false)
    private int parcelas;

    @Column(nullable = false)
    private int pagas; // 🔹 0 = A PAGAR, -1 = ATRASADO, 1 = PAGO

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dataPagamento;

    @Column(nullable = false)
    private double valor;

    private double valorPago;

    private double valorSobra;

    private String BancoEntrada;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusParcela status;

    public Parcelas() {}

    // 🔹 Método que atualiza o status automaticamente
    public void atualizarStatus() {
        Date hoje = new Date();

        if (this.pagas > 0) {
            this.status = StatusParcela.PAGO;
        } else if (this.dataPagamento == null) {
            this.status = StatusParcela.PENDENTE;
        } else if (this.dataPagamento.before(hoje)) {
            this.status = StatusParcela.ATRASADO;
        } else {
            this.status = StatusParcela.PENDENTE;
        }
    }

    // Getters e Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Historico getHistorico() { return historico; }

    public void setHistorico(Historico historico) { this.historico = historico; }

    public int getParcelas() { return parcelas; }

    public void setParcelas(int parcelas) { this.parcelas = parcelas; }

    public int getPagas() { return pagas; }

    public void setPagas(int pagas) {
        this.pagas = pagas;
        atualizarStatus(); // 🔹 Atualiza o status automaticamente
    }

    public Date getDataPagamento() { return dataPagamento; }

    public void setDataPagamento(Date dataPagamento) { this.dataPagamento = dataPagamento; }

    public double getValor() { return valor; }

    public void setValor(double valor) { this.valor = valor; }

    public String getBancoEntrada() {
        return BancoEntrada;
    }

    public void setBancoEntrada(String bancoEntrada) {
        BancoEntrada = bancoEntrada;
    }

    public StatusParcela getStatus() { return status; }

    public void setStatus(StatusParcela status) { this.status = status; }

    public double getValorPago() {
        return valorPago;
    }

    public void setValorPago(double valorPago) {
        this.valorPago = valorPago;
    }

    public double getValorSobra() {
        return valorSobra;
    }

    public void setValorSobra(double valorSobra) {
        this.valorSobra = valorSobra;
    }

}
