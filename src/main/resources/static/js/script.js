document.addEventListener("DOMContentLoaded", () => {
    console.log("🚀 Script carregado!");

    // =================== MODAIS ===================
    function configurarModal(openButton, modal, closeButton) {
        if (openButton && modal && closeButton) {
            openButton.addEventListener("click", () => {
                modal.classList.add("show");
                modal.classList.remove("hide");
            });

            closeButton.addEventListener("click", () => {
                modal.classList.add("hide");
                modal.classList.remove("show");
            });

            modal.addEventListener("click", (event) => {
                if (event.target === modal) {
                    modal.classList.add("hide");
                    modal.classList.remove("show");
                }
            });
        } else {
            console.error(`❌ Elementos do modal não encontrados para ${modal?.id || "desconhecido"}`);
        }
    }

    configurarModal(document.getElementById("openModalFiltro"), document.getElementById("modalFiltro"), document.getElementById("btnFecharModal"));
    configurarModal(document.getElementById("open-modal"), document.getElementById("modal"), document.getElementById("close-modal"));
    configurarModal(document.querySelector(".add"), document.getElementById("modal-add-emprestimo"), document.querySelector("#modal-add-emprestimo .close-button"));
    configurarModal(document.querySelector(".addC"), document.getElementById("modal-add-cliente"), document.querySelector("#modal-add-cliente .close"));
    configurarModal(document.querySelector(".addB"), document.getElementById("modal-add-banco"), document.getElementById("close-modal-add-banco"));
    configurarModal(document.getElementById("openAddFuncionario"), document.getElementById("modal-add-funcionario"), document.getElementById("closeAddFuncionario"));

    /** =================== FILTRAR A TABELA =================== **/
    const formFiltro = document.getElementById("form-filtro");
    const tabela = document.querySelector("#example tbody");

    if (!formFiltro || !tabela) {
        console.error("❌ Formulário ou tabela não encontrados!");
        return;
    }

    formFiltro.addEventListener("submit", (event) => {
        event.preventDefault();
        console.log("📌 Evento de filtragem acionado!");

        const mesSelecionado = document.getElementById("filtro-mes").value; // Formato YYYY-MM
        const funcionarioSelecionado = document.getElementById("funcionario").value.trim().toLowerCase();
        const clienteSelecionado = document.getElementById("cliente").value.trim().toLowerCase();

        let linhasVisiveis = 0;

        tabela.querySelectorAll("tr").forEach((linha) => {
            const celulas = linha.getElementsByTagName("td");

            if (celulas.length < 9) return; // Evita erro caso a linha não tenha todas as células esperadas

            const dataEmprestimoTexto = celulas[6]?.innerText.trim(); // 📌 Data do Empréstimo está na 7ª coluna (índice 6)
            const funcionario = celulas[5]?.innerText.trim().toLowerCase();
            const cliente = celulas[4]?.innerText.trim().toLowerCase();

            let mesAnoEmprestimo = "";
            if (dataEmprestimoTexto) {
                const partesData = dataEmprestimoTexto.split("/");
                if (partesData.length === 3) {
                    const dia = partesData[0].padStart(2, "0");
                    const mes = partesData[1].padStart(2, "0");
                    const ano = partesData[2];
                    mesAnoEmprestimo = `${ano}-${mes}`; // Transforma para formato YYYY-MM
                }
            }

            console.log(`📌 Comparando: ${mesAnoEmprestimo} com ${mesSelecionado}`);

            // 🔹 Verifica se a linha corresponde aos filtros aplicados
            const correspondeMes = !mesSelecionado || mesAnoEmprestimo === mesSelecionado;
            const correspondeFuncionario = !funcionarioSelecionado || funcionario.includes(funcionarioSelecionado);
            const correspondeCliente = !clienteSelecionado || cliente.includes(clienteSelecionado);

            const mostrar = correspondeMes && correspondeFuncionario && correspondeCliente;

            linha.style.display = mostrar ? "" : "none";
            if (mostrar) linhasVisiveis++;
        });

        console.log(`📌 ${linhasVisiveis} linhas visíveis após filtragem.`);
        if (linhasVisiveis === 0) console.warn("⚠ Nenhum resultado encontrado com os filtros aplicados!");

        document.getElementById("modalFiltro").classList.remove("show"); // Fecha o modal
    });

    // =================== LISTA DE CLIENTES E SÓCIOS ===================
    function configurarListaDeModal(janela, abrirBotao, fecharBotao) {
        if (!janela || !abrirBotao || !fecharBotao) {
            console.error(`❌ Elemento(s) do modal não encontrados para ${janela ? janela.id : "N/A"}`);
            return;
        }

        abrirBotao.addEventListener("click", () => {
            console.log(`📌 Tentando abrir modal: ${janela.id}`);
            janela.style.display = 'block';
            console.log(`📌 Estado do modal ${janela.id}: `, getComputedStyle(janela).display);
        });

        fecharBotao.addEventListener("click", () => {
            console.log(`📌 Fechando modal: ${janela.id}`);
            janela.style.display = 'none';
        });

        window.addEventListener("click", (e) => {
            if (e.target === janela) {
                console.log(`📌 Fechando modal ao clicar fora: ${janela.id}`);
                janela.style.display = 'none';
            }
        });
    }

    // =================== LIMPAR NOTIFICAÇÕES ===================
    const clearNotificationsButton = document.getElementById("clear-notifications");

    if (clearNotificationsButton) {
        clearNotificationsButton.addEventListener("click", function () {
            if (confirm("Tem certeza que deseja apagar todas as notificações?")) {
                fetch("/notifications/clear", { method: "DELETE", headers: { 'Content-Type': 'application/json' } })
                    .then(() => {
                        alert("Notificações apagadas com sucesso!");
                        location.reload();
                    })
                    .catch(error => {
                        console.error("Erro ao apagar notificações:", error);
                        alert("Erro ao apagar notificações.");
                    });
            }
        });
    }

    // =================== EXIBIR MENSAGEM DE SUCESSO/ERRO ===================
    const alertBox = document.getElementById("cliente-mensagem");
    const alertText = document.getElementById("cliente-alert-text");
    const mensagemSucesso = "[[${success}]]";
    const mensagemErro = "[[${error}]]";

    if (alertBox && alertText) {
        if (mensagemSucesso && mensagemSucesso !== "null") {
            alertBox.classList.remove("hide");
            alertBox.classList.add("alert-success");
            alertText.textContent = mensagemSucesso;
        } else if (mensagemErro && mensagemErro !== "null") {
            alertBox.classList.remove("hide");
            alertBox.classList.add("alert-danger");
            alertText.textContent = mensagemErro;
        }
    }

    // =================== FORMATAÇÃO DE DATA ===================
    function formatarDataParaComparacao(data) {
        if (!data) return null;
        let partes = data.split("/");
        if (partes.length === 3) {
            return new Date(`${partes[2]}-${partes[1]}-${partes[0]}`);
        }
        return null;
    }
});

document.addEventListener("DOMContentLoaded", () => {
    console.log("🚀 Script de modais e edição carregado!");

    function configurarModal(janelaId, abrirId, fecharId) {
        const janela = document.getElementById(janelaId);
        const abrirBotao = document.getElementById(abrirId);
        const fecharBotao = document.getElementById(fecharId);

        if (!janela || !abrirBotao || !fecharBotao) {
            console.error(`❌ Elementos do modal ${janelaId} não encontrados!`);
            return;
        }

        abrirBotao.addEventListener("click", () => {
            janela.style.display = 'block';
        });

        fecharBotao.addEventListener("click", () => {
            janela.style.display = 'none';
        });

        window.addEventListener("click", (event) => {
            if (event.target === janela) {
                janela.style.display = 'none';
            }
        });
    }

    configurarModal("clientsListWindow", "openClientsModal", "closeClientsList");
    configurarModal("sociosListWindow", "openEmployeesModal", "closeSociosList");
});

document.addEventListener("DOMContentLoaded", () => {
    console.log("🚀 Script carregado!");

    // Função para abrir o modal de edição e preencher os campos
    window.abrirModalEdicao = function (botao) {
        // Pega os dados do botão "Editar"
        const id = botao.getAttribute("data-id");
        const nome = botao.getAttribute("data-nome");
        const cpf = botao.getAttribute("data-cpf");
        const contato = botao.getAttribute("data-contato");
        const endereco = botao.getAttribute("data-endereco");

        // Preenche os campos do modal com os dados do cliente
        document.getElementById("edit-id").value = id;
        document.getElementById("edit-nome").value = nome;
        document.getElementById("edit-cpf").value = cpf;
        document.getElementById("edit-contato").value = contato;
        document.getElementById("edit-endereco").value = endereco;

        // Exibe o modal
        document.getElementById("editModal").style.display = "block";
    };

    // Fecha o modal ao clicar no botão de fechar
    document.getElementById("closeEditModal").addEventListener("click", () => {
        document.getElementById("editModal").style.display = "none";
    });

    // Fecha o modal ao clicar fora dele
    window.addEventListener("click", (event) => {
        const modal = document.getElementById("editModal");
        if (event.target === modal) {
            modal.style.display = "none";
        }
    });
});

document.addEventListener("DOMContentLoaded", () => {
    console.log("🚀 Script carregado!");

    // Função para abrir o modal de edição de funcionários e preencher os campos
    window.abrirModalEdicaoFuncionario = function (botao) {
        // Pega os dados do botão "Editar"
        const id = botao.getAttribute("data-id");
        const nome = botao.getAttribute("data-nome");
        const endereco = botao.getAttribute("data-endereco");
        const idade = botao.getAttribute("data-idade");
        const telefone = botao.getAttribute("data-telefone");

        // Preenche os campos do modal com os dados do funcionário
        document.getElementById("edit-funcionario-id").value = id;
        document.getElementById("edit-funcionario-nome").value = nome;
        document.getElementById("edit-funcionario-endereco").value = endereco;
        document.getElementById("edit-funcionario-idade").value = idade;
        document.getElementById("edit-funcionario-telefone").value = telefone;

        // Exibe o modal
        document.getElementById("editFuncionarioModal").style.display = "block";
    };

    // Fecha o modal ao clicar no botão de fechar
    document.getElementById("closeEditFuncionarioModal").addEventListener("click", () => {
        document.getElementById("editFuncionarioModal").style.display = "none";
    });

    // Fecha o modal ao clicar fora dele
    window.addEventListener("click", (event) => {
        const modal = document.getElementById("editFuncionarioModal");
        if (event.target === modal) {
            modal.style.display = "none";
        }
    });
});

document.addEventListener("DOMContentLoaded", () => {
    console.log("🚀 Script carregado!");

    // Função para abrir o modal de edição de bancos e preencher os campos
    window.abrirModalEdicaoBanco = function (botao) {
        console.log("📌 Clicou no botão Editar Banco!");

        // Pega os dados do botão "Editar"
        const id = botao.getAttribute("data-id");
        const nome = botao.getAttribute("data-nome");
        const descricao = botao.getAttribute("data-descricao");

        console.log("📌 Banco ID:", id);
        console.log("📌 Nome:", nome);
        console.log("📌 Descrição:", descricao);

        // Verifica se os elementos existem antes de tentar preenchê-los
        const modal = document.getElementById("editBancoModal");
        const campoId = document.getElementById("edit-banco-id");
        const campoNome = document.getElementById("edit-banco-nome");
        const campoDescricao = document.getElementById("edit-banco-descricao");

        if (!modal || !campoId || !campoNome || !campoDescricao) {
            console.error("❌ Elementos do modal não encontrados!");
            return;
        }

        // Preenche os campos do modal com os dados do banco
        campoId.value = id;
        campoNome.value = nome;
        campoDescricao.value = descricao;

        // Exibe o modal
        modal.style.display = "block";
        console.log("✅ Modal de edição de banco aberto!");
    };

    // Fecha o modal ao clicar no botão de fechar
    const closeModal = document.getElementById("closeEditBancoModal");
    if (closeModal) {
        closeModal.addEventListener("click", () => {
            document.getElementById("editBancoModal").style.display = "none";
            console.log("✅ Modal de edição de banco fechado!");
        });
    } else {
        console.error("❌ Botão de fechar o modal não encontrado!");
    }

    // Fecha o modal ao clicar fora dele
    window.addEventListener("click", (event) => {
        const modal = document.getElementById("editBancoModal");
        if (event.target === modal) {
            modal.style.display = "none";
            console.log("✅ Modal de edição de banco fechado ao clicar fora!");
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const openModalButton = document.getElementById("openBanksModal");
    const closeModalButton = document.getElementById("closeBanksList");
    const modal = document.getElementById("banksListWindow");

    if (openModalButton && closeModalButton && modal) {
        openModalButton.addEventListener("click", function () {
            modal.style.display = "block"; // Exibe o modal
        });

        closeModalButton.addEventListener("click", function () {
            modal.style.display = "none"; // Fecha o modal
        });

        // Fecha o modal ao clicar fora dele
        window.addEventListener("click", function (event) {
            if (event.target === modal) {
                modal.style.display = "none";
            }
        });
    } else {
        console.error("Elementos do modal não encontrados!");
    }
});
