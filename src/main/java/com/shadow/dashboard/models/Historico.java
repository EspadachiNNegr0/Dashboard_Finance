package com.shadow.dashboard.models;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Tb_History")
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = true)
    private Long id;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int percentage;

    @Enumerated(EnumType.STRING)  // Para armazenar o status como uma string
    @Column(nullable = false)
    private Status status;

    @Column(length = 255)
    private String description;

    @ManyToOne
    private Socios socios;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)  // A chave estrangeira
    private Clientes cliente; // Cliente associado a esse History

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date created;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @Column(nullable = true)
    private Date creationF;

    @ManyToOne
    private Banco banco;

    @Column(nullable = false)
    private int parcelamento;

    @ElementCollection
    @CollectionTable(name = "payment_dates", joinColumns = @JoinColumn(name = "history_id"))
    @Column(name = "payment_date")
    private List<Date> datasPagamentos = new ArrayList<>();


    @Column(nullable = true)
    private Double jurosPagos = 0.0;


    // Getters e setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
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

    public Socios getSocios() {
        return socios;
    }

    public void setSocios(Socios socios) {
        this.socios = socios;
    }

    public int getParcelamento() {
        return parcelamento;
    }

    public void setParcelamento(int parcelamento) {
        this.parcelamento = parcelamento;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    public List<Date> getDatasPagamentos() {
        return datasPagamentos;
    }

    public void setDatasPagamentos(List<Date> datasPagamentos) {
        this.datasPagamentos = datasPagamentos;
    }

    public Double getJurosPagos() {
        return jurosPagos;
    }

    public void setJurosPagos(Double jurosPagos) {
        this.jurosPagos = jurosPagos;
    }
}
