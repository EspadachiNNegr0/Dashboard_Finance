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
        const mesSelecionado = document.getElementById("filtro-mes").value;

        let linhasVisiveis = 0;

        tabela.querySelectorAll("tr").forEach((linha) => {
            const valorTexto = linha.cells[5]?.innerText.replace("R$", "").trim().replace(",", ".");
            const valor = parseFloat(valorTexto) || 0;
            const status = linha.cells[6]?.innerText.trim().toLowerCase();
            const dataTexto = linha.cells[4]?.innerText.trim();

            let mesAnoLinha = "";
            if (dataTexto) {
                let [dia, mes, ano] = dataTexto.split("/");
                mesAnoLinha = `${ano}-${mes}`;
            }

            const correspondeTipo = tipo === "" || status === tipo;
            const correspondeValor = valor >= valorMin && valor <= valorMax;
            const correspondeMes = mesSelecionado === "" || mesSelecionado === mesAnoLinha;

            const mostrar = correspondeTipo && correspondeValor && correspondeMes;

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

        // **Corrigido: O modal agora pode ser aberto novamente**
        fecharModal(modalFiltro);

        // **Garante que o botão de abrir modal continua funcionando após o filtro**
        setTimeout(() => {
            openModalFiltro.addEventListener("click", () => abrirModal(modalFiltro));
        }, 100);
    });

    atualizarCoresStatus();
    calcularTotaisTabela(); // Calcular totais ao carregar a página
});