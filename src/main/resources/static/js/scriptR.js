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
    // =================== FILTRAR A TABELA ===================
    const formFiltro = document.getElementById("form-filtro");
    const tabela = document.querySelector("#example tbody");
    const totalEntradas = document.getElementById("total-entradas");
    const totalSaidas = document.getElementById("total-saidas");
    const saldoFinal = document.getElementById("saldo-final");

    function atualizarCoresStatus() {
        tabela.querySelectorAll("tr").forEach((linha) => {
            const statusCell = linha.cells[6]; // Coluna do status
            if (statusCell) {
                const statusTexto = statusCell.textContent.trim().toLowerCase();
                statusCell.style.color = statusTexto === "saida" ? "red" : "green";
            }
        });
    }

    function calcularTotaisTabela() {
        let totalEntradasValor = 0;
        let totalSaidasValor = 0;

        tabela.querySelectorAll("tr").forEach((linha) => {
            if (linha.style.display !== "none") { // Somar apenas linhas visíveis
                const valorTexto = linha.cells[5]?.innerText.replace("R$", "").trim().replace(",", ".");
                const valor = parseFloat(valorTexto) || 0;
                const status = linha.cells[6]?.innerText.trim().toLowerCase();

                if (status === "entrada") {
                    totalEntradasValor += valor;
                } else if (status === "saida") {
                    totalSaidasValor += valor;
                }
            }
        });

        totalEntradas.textContent = `R$ ${totalEntradasValor.toFixed(2).replace(".", ",")}`;
        totalSaidas.textContent = `R$ ${totalSaidasValor.toFixed(2).replace(".", ",")}`;
        saldoFinal.textContent = `R$ ${(totalEntradasValor - totalSaidasValor).toFixed(2).replace(".", ",")}`;
    }

    formFiltro.addEventListener("submit", function (event) {
        event.preventDefault();

        const tipo = document.getElementById("filtro-tipo").value.toLowerCase();
        const valorMin = parseFloat(document.getElementById("filtro-valor-min").value) || Number.MIN_VALUE;
        const valorMax = parseFloat(document.getElementById("filtro-valor-max").value) || Number.MAX_VALUE;
        const mesSelecionado = document.getElementById("filtro-mes").value.toLowerCase();

        // Novos filtros
        const clienteFiltro = document.getElementById("filtro-cliente").value.trim().toLowerCase();
        const funcionarioFiltro = document.getElementById("filtro-funcionario").value.trim().toLowerCase();
        const bancoFiltro = document.getElementById("filtro-banco").value.trim().toLowerCase();

        let linhasVisiveis = 0;

        tabela.querySelectorAll("tr").forEach((linha) => {
            const valorTexto = linha.cells[5]?.innerText.replace("R$", "").trim().replace(",", ".");
            const valor = parseFloat(valorTexto) || 0;
            const status = linha.cells[6]?.innerText.trim().toLowerCase();
            const dataTexto = linha.cells[4]?.innerText.trim();
            const banco = linha.cells[1]?.innerText.trim().toLowerCase();
            const funcionario = linha.cells[2]?.innerText.trim().toLowerCase();
            const cliente = linha.cells[3]?.innerText.trim().toLowerCase();

            let mesAnoLinha = "";
            if (dataTexto) {
                let [dia, mes, ano] = dataTexto.split("/");
                mesAnoLinha = `${ano}-${mes}`;
            }

            const correspondeTipo = tipo === "" || status === tipo;
            const correspondeValor = valor >= valorMin && valor <= valorMax;
            const correspondeMes = mesSelecionado === "" || mesSelecionado === mesAnoLinha;
            const correspondeBanco = bancoFiltro === "" || banco.includes(bancoFiltro);
            const correspondeFuncionario = funcionarioFiltro === "" || funcionario.includes(funcionarioFiltro);
            const correspondeCliente = clienteFiltro === "" || cliente.includes(clienteFiltro);

            const mostrar = correspondeTipo && correspondeValor && correspondeMes && correspondeBanco && correspondeFuncionario && correspondeCliente;

            if (mostrar) {
                linha.style.display = "";
                linhasVisiveis++;
            } else {
                linha.style.display = "none";
            }
        });

        if (linhasVisiveis === 0) {
            console.log("⚠ Nenhum resultado encontrado com os filtros aplicados!");
        }

        calcularTotaisTabela(); // Recalcular totais apenas com as linhas visíveis
        atualizarCoresStatus(); // Aplicar cores aos status filtrados

        fecharModal(modalFiltro); // Fechar modal após filtro
    });

    atualizarCoresStatus();
    calcularTotaisTabela(); // Calcular totais ao carregar a página
});