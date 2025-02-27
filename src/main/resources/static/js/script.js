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

    // =================== FILTRAR A TABELA ===================
    const btnAplicarFiltro = document.getElementById("btn-modal-filtro");
    const tabela = document.querySelector("#example tbody");

    if (btnAplicarFiltro && tabela) {
        btnAplicarFiltro.addEventListener("click", (event) => {
            event.preventDefault();

            let dataInicio = document.getElementById("data-inicio").value;
            let dataPagamento = document.getElementById("data-pagamento").value;
            let valorMin = parseFloat(document.getElementById("valor-min").value) || 0;
            let valorMax = parseFloat(document.getElementById("valor-max").value) || Number.MAX_VALUE;
            let funcionarioSelecionado = document.getElementById("funcionario").value.trim().toLowerCase();
            let bancoSaidaSelecionado = document.getElementById("bancoSaida").value.trim().toLowerCase();

            console.log("📝 Filtros aplicados:", { dataInicio, dataPagamento, valorMin, valorMax, funcionarioSelecionado, bancoSaidaSelecionado });

            Array.from(tabela.getElementsByTagName("tr")).forEach((linha) => {
                let dataInicioLinha = linha.cells[6]?.innerText.trim(); // Data de Empréstimo
                let dataPagamentoLinha = linha.cells[7]?.innerText.trim(); // Data Final
                let valorLinha = parseFloat(linha.cells[1]?.innerText.replace(",", ".")) || 0;
                let funcionarioLinha = linha.cells[5]?.innerText.trim().toLowerCase(); // Funcionário
                let bancoSaidaLinha = linha.cells[4]?.innerText.trim().toLowerCase(); // Banco de Saída

                let mostrar = true;

                // Filtro por Data de Início
                if (dataInicio) {
                    let dataInicioFormatada = formatarDataParaComparacao(dataInicioLinha);
                    let filtroDataInicio = new Date(dataInicio);
                    if (dataInicioFormatada < filtroDataInicio) mostrar = false;
                }

                // Filtro por Data de Pagamento
                if (dataPagamento) {
                    let dataPagamentoFormatada = formatarDataParaComparacao(dataPagamentoLinha);
                    let filtroDataPagamento = new Date(dataPagamento);
                    if (dataPagamentoFormatada > filtroDataPagamento) mostrar = false;
                }

                // Filtro por Valor
                if (valorLinha < valorMin || valorLinha > valorMax) {
                    mostrar = false;
                }

                // Filtro por Funcionário
                if (funcionarioSelecionado && funcionarioLinha !== funcionarioSelecionado) {
                    mostrar = false;
                }

                // Filtro por Banco de Saída
                if (bancoSaidaSelecionado && bancoSaidaLinha !== bancoSaidaSelecionado) {
                    mostrar = false;
                }

                linha.style.display = mostrar ? "" : "none";
            });

            console.log("✅ Filtro aplicado!");
            document.getElementById("modalFiltro")?.classList.remove("show");
        });
    } else {
        console.error("❌ Elementos do filtro não encontrados!");
    }

    // =================== TOGGLE DO MENU DO PERFIL ===================
    const profile = document.querySelector(".profile");
    const subMenu = document.getElementById("subMenu");

    if (profile && subMenu) {
        profile.addEventListener("click", function (event) {
            event.stopPropagation();
            subMenu.classList.toggle("show");
        });

        document.addEventListener("click", function (event) {
            if (!profile.contains(event.target) && !subMenu.contains(event.target)) {
                subMenu.classList.remove("show");
            }
        });

        console.log("🔹 Submenu configurado com sucesso!");
    } else {
        console.error("❌ Elemento 'profile' ou 'subMenu' não encontrado!");
    }

    // =================== LISTA DE CLIENTES E SÓCIOS ===================
    function configurarListaDeModal(janela, abrirBotao, fecharBotao) {
        if (janela && abrirBotao && fecharBotao) {
            abrirBotao.addEventListener("click", () => janela.style.display = 'block');
            fecharBotao.addEventListener("click", () => janela.style.display = 'none');

            window.addEventListener("click", (e) => {
                if (e.target === janela) janela.style.display = 'none';
            });
        }
    }

    configurarListaDeModal(document.getElementById('clientsListWindow'), document.getElementById('openClientsModal'), document.getElementById('closeClientsList'));
    configurarListaDeModal(document.getElementById('sociosListWindow'), document.getElementById('openEmployeesModal'), document.getElementById('closeSociosList'));

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
