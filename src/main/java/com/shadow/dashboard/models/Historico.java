package com.shadow.dashboard.models;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Entity
@Table(name = "Tb_History")
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int codigo;

    @Column(nullable = false)
    private Double price; // Valor do empréstimo atual

    @Column(nullable = false)
    private Integer percentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status status;

    @Column(length = 255)
    private String description;

    @ManyToOne
    private Socios socios;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Clientes cliente;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date created;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(nullable = true)
    private Date creationF;

    // 🔹 Adicionando Banco de Entrada e Banco de Saída
    @ManyToOne
    @JoinColumn(name = "banco_saida", nullable = false) // 🔹 Banco de onde o dinheiro sai
    private Banco bancoSaida;


    @Column(nullable = false)
    private Integer parcelamento;

    @OneToMany(mappedBy = "historico", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Parcelas> parcelas;

    public Historico() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Socios getSocios() {
        return socios;
    }

    public void setSocios(Socios socios) {
        this.socios = socios;
    }

    public Clientes getCliente() {
        return cliente;
    }

    public void setCliente(Clientes cliente) {
        this.cliente = cliente;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getCreationF() {
        return creationF;
    }

    public void setCreationF(Date creationF) {
        this.creationF = creationF;
    }

    public Banco getBancoSaida() {
        return bancoSaida;
    }

    public void setBancoSaida(Banco bancoSaida) {
        this.bancoSaida = bancoSaida;
    }

    public Integer getParcelamento() {
        return parcelamento;
    }

    public void setParcelamento(Integer parcelamento) {
        this.parcelamento = parcelamento;
    }

    public List<Parcelas> getParcelas() {
        return parcelas;
    }

    public void setParcelas(List<Parcelas> parcelas) {
        this.parcelas = parcelas;
    }
}
