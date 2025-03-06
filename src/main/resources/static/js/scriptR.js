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

    // =================== TOGGLE DE COLUNAS (Juros e Amortização) ===================
    function alternarColuna(classeColuna, botao, textoAtivar, textoDesativar) {
        botao.addEventListener("click", () => {
            const colunas = document.querySelectorAll(`.${classeColuna}`);
            let visivel = colunas[0].style.display !== "none";

            colunas.forEach(coluna => {
                coluna.style.display = visivel ? "none" : "";
            });

            botao.textContent = visivel ? textoAtivar : textoDesativar;
        });
    }

    alternarColuna("col-juros", document.getElementById("toggle-juros"), "👁 Mostrar Juros", "🙈 Ocultar Juros");
    alternarColuna("col-amortizacao", document.getElementById("toggle-amortizacao"), "👁 Mostrar Amortização", "🙈 Ocultar Amortização");

    // =================== ATUALIZAÇÃO DOS VALORES ===================
    const totalEntradasElement = document.getElementById("total-entradas");
    const totalSaidasElement = document.getElementById("total-saidas");
    const saldoFinalElement = document.getElementById("saldo-final");

    function calcularTotaisTabela() {
        let totalEntradasValor = 0;
        let totalSaidasValor = 0;

        document.querySelectorAll("#example tbody tr").forEach((linha) => {
            if (linha.style.display !== "none") { // Só calcula os valores visíveis
                const valorTexto = linha.cells[5]?.innerText.replace("R$", "").trim().replace(",", ".");
                const valor = parseFloat(valorTexto) || 0;
                const status = linha.cells[8]?.innerText.trim().toLowerCase();

                if (status === "entrada") {
                    totalEntradasValor += valor;
                } else if (status === "saida") {
                    totalSaidasValor += valor;
                }
            }
        });

        totalEntradasElement.textContent = `R$ ${totalEntradasValor.toFixed(2).replace(".", ",")}`;
        totalSaidasElement.textContent = `R$ ${totalSaidasValor.toFixed(2).replace(".", ",")}`;
        saldoFinalElement.textContent = `R$ ${(totalEntradasValor - totalSaidasValor).toFixed(2).replace(".", ",")}`;
    }

    // =================== FILTRO ===================
    const formFiltro = document.getElementById("form-filtro");
    const tabela = document.querySelector("#example tbody");

    formFiltro.addEventListener("submit", function (event) {
        event.preventDefault();

        const tipo = document.getElementById("filtro-tipo").value.toLowerCase();
        const valorMin = parseFloat(document.getElementById("filtro-valor-min").value) || Number.MIN_VALUE;
        const valorMax = parseFloat(document.getElementById("filtro-valor-max").value) || Number.MAX_VALUE;
        const mesSelecionado = document.getElementById("filtro-mes").value.toLowerCase();
        const clienteFiltro = document.getElementById("filtro-cliente").value.trim().toLowerCase();
        const funcionarioFiltro = document.getElementById("filtro-funcionario").value.trim().toLowerCase();
        const bancoFiltro = document.getElementById("filtro-banco").value.trim().toLowerCase();

        let linhasVisiveis = 0;

        tabela.querySelectorAll("tr").forEach((linha) => {
            const valor = parseFloat(linha.cells[5]?.innerText.replace("R$", "").trim().replace(",", ".")) || 0;
            const status = linha.cells[8]?.innerText.trim().toLowerCase();
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

        console.log(`📌 ${linhasVisiveis} linhas visíveis após filtragem.`);

        if (linhasVisiveis === 0) {
            console.warn("⚠ Nenhum resultado encontrado com os filtros aplicados!");
        }

        calcularTotaisTabela();
        atualizarCoresStatus();
    });

    // =================== COLORIR STATUS (Entrada = Verde, Saída = Vermelho) ===================
    function atualizarCoresStatus() {
        tabela.querySelectorAll("tr").forEach((linha) => {
            const statusCell = linha.cells[8]; // Coluna do status
            if (statusCell) {
                const statusTexto = statusCell.textContent.trim().toLowerCase();
                if (statusTexto === "entrada") {
                    statusCell.style.color = "green";
                } else if (statusTexto === "saida") {
                    statusCell.style.color = "red";
                } else {
                    statusCell.style.color = "black";
                }
            }
        });
    }

    calcularTotaisTabela();
    atualizarCoresStatus();
});
