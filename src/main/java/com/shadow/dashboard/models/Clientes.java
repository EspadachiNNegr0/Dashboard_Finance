package com.shadow.dashboard.models;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Tb_Cliente")
public class Clientes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String nome;

    @Column(nullable = true, unique = true)
    private String cpf;

    @Column(nullable = true)
    private String telefone;

    @Column(nullable = true)
    private String endereco;

    @OneToMany(mappedBy = "cliente") // "cliente" é o nome do atributo na classe History
    private Set<History> history;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Set<History> getHistory() {
        return history;
    }

    public void setHistory(Set<History> history) {
        this.history = history;
    }
}
