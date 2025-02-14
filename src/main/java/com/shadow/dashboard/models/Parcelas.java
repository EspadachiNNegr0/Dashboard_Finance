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
    private int pagas; // 🔹 0 = A PAGAR, -1 = PENDENTE, >0 = PAGO

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dataPagamento;

    @Column(nullable = false)
    private double valor;


    @Column(length = 20) // 🔹 Adiciona a coluna status diretamente na tabela
    private String status;

    // 🔹 Construtor padrão
    public Parcelas() {}

    // 🔹 Método que atualiza o status automaticamente
    public void atualizarStatus() {
        Date hoje = new Date();

        if (this.pagas > 0) {
            // ✅ Pago se já foi pago
            this.status = "PAGO";
        } else if (this.dataPagamento == null) {
            // ✅ A Pagar se não tem data de pagamento
            System.out.println("⚠️ Parcela ID " + this.id + ": Sem dataPagamento, status definido como 'A PAGAR'.");
            this.status = "A PAGAR";
        } else if (this.dataPagamento.before(hoje) && this.pagas == 0) {
            // ✅ Atrasado se a data de pagamento é no passado e não foi pago
            this.status = "ATRASADO";
        } else {
            // ✅ Caso padrão: Pendente
            this.status = "PENDENTE";
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

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
