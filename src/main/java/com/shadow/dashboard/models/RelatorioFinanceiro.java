package com.shadow.dashboard.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tb_RelatorioFinanceiro")
public class RelatorioFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private int codigo;

    private double valor;
    private String banco;

    @Temporal(TemporalType.TIMESTAMP)
    private Date data;

    @Enumerated(EnumType.STRING)
    private StatusR status;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "historico_id", nullable = false)
    private Historico historico;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "relatorio_entrada_id")
    private RelatorioEntrada relatorioEntrada;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "relatorio_saida_id")
    private RelatorioSaida relatorioSaida;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getCodigo() { return codigo; }
    public void setCodigo(int codigo) { this.codigo = codigo; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public StatusR getStatus() { return status; }
    public void setStatus(StatusR status) { this.status = status; }

    public Historico getHistorico() { return historico; }
    public void setHistorico(Historico historico) { this.historico = historico; }

    public RelatorioEntrada getRelatorioEntrada() { return relatorioEntrada; }
    public void setRelatorioEntrada(RelatorioEntrada relatorioEntrada) { this.relatorioEntrada = relatorioEntrada; }

    public RelatorioSaida getRelatorioSaida() { return relatorioSaida; }
    public void setRelatorioSaida(RelatorioSaida relatorioSaida) { this.relatorioSaida = relatorioSaida; }
}
