document.addEventListener("DOMContentLoaded", () => {
    console.log("🚀 Script carregado!");

    /** =================== MODAIS =================== **/
    function configurarModal(openButton, modal, closeButton) {
        if (openButton && modal && closeButton) {
            openButton.addEventListener("click", () => modal.classList.toggle("show"));
            closeButton.addEventListener("click", () => modal.classList.toggle("show"));
            modal.addEventListener("click", (event) => {
                if (event.target === modal) modal.classList.toggle("show");
            });
        } else {
            console.error(`❌ Elementos do modal não encontrados para ${modal?.id || "desconhecido"}`);
        }
    }

    configurarModal(
        document.getElementById("openModalFiltro"),
        document.getElementById("modalFiltro"),
        document.getElementById("btnFecharModal")
    );

    /** =================== TOGGLE DE COLUNAS =================== **/
    function alternarColuna(classeColuna, botao, textoAtivar, textoDesativar) {
        if (botao) {
            botao.addEventListener("click", () => {
                document.querySelectorAll(`.${classeColuna}`).forEach(coluna => {
                    coluna.style.display = coluna.style.display === "none" ? "" : "none";
                });
                botao.textContent = botao.textContent === textoAtivar ? textoDesativar : textoAtivar;
            });
        }
    }

    alternarColuna("col-juros", document.getElementById("toggle-juros"), "👁 Mostrar Juros", "🙈 Ocultar Juros");
    alternarColuna("col-amortizacao", document.getElementById("toggle-amortizacao"), "👁 Mostrar Amortização", "🙈 Ocultar Amortização");

    /** =================== ATUALIZAÇÃO DOS VALORES =================== **/
    const totalEntradasElement = document.getElementById("total-entradas");
    const totalSaidasElement = document.getElementById("total-saidas");
    const saldoFinalElement = document.getElementById("saldo-final");
    const totalJurosElement = document.getElementById("total-juros");
    const totalAmortizacaoElement = document.getElementById("total-amortizacao");

    function formatarMoeda(valor) {
        return `R$ ${valor.toFixed(2).replace(".", ",")}`;
    }

    function calcularTotaisTabela() {
        let totalEntradas = 0, totalSaidas = 0, totalJuros = 0, totalAmortizacao = 0;

        document.querySelectorAll("#example tbody tr:not([style*='display: none'])").forEach((linha) => {
            const obterValor = (index) => parseFloat(linha.cells[index]?.innerText.replace("R$", "").trim().replace(",", ".")) || 0;

            const valor = obterValor(5);
            const juros = obterValor(6);
            const amortizacao = obterValor(7);
            const status = linha.cells[8]?.innerText.trim().toLowerCase();

            if (status === "entrada") totalEntradas += valor;
            if (status === "saida") totalSaidas += valor;
            totalJuros += juros;
            totalAmortizacao += amortizacao;
        });

        totalEntradasElement.textContent = formatarMoeda(totalEntradas);
        totalSaidasElement.textContent = formatarMoeda(totalSaidas);
        saldoFinalElement.textContent = formatarMoeda(totalEntradas - totalSaidas);
        totalJurosElement.textContent = formatarMoeda(totalJuros);
        totalAmortizacaoElement.textContent = formatarMoeda(totalAmortizacao);
    }

    /** =================== FILTRO =================== **/
    const formFiltro = document.getElementById("form-filtro");
    const tabela = document.querySelector("#example tbody");

    formFiltro.addEventListener("submit", (event) => {
        event.preventDefault();

        const tipo = document.getElementById("filtro-tipo").value.toLowerCase();
        const mesSelecionado = document.getElementById("filtro-mes").value;
        const bancoFiltro = document.getElementById("filtro-banco").value.trim().toLowerCase();
        const funcionarioFiltro = document.getElementById("filtro-funcionario").value.trim().toLowerCase();
        const clienteFiltro = document.getElementById("filtro-cliente").value.trim().toLowerCase();

        let linhasVisiveis = 0;

        tabela.querySelectorAll("tr").forEach((linha) => {
            const status = linha.cells[8]?.innerText.trim().toLowerCase();
            const dataTexto = linha.cells[4]?.innerText.trim();
            const banco = linha.cells[1]?.innerText.trim().toLowerCase();
            const funcionario = linha.cells[2]?.innerText.trim().toLowerCase();
            const cliente = linha.cells[3]?.innerText.trim().toLowerCase();

            let mesAnoLinha = "";
            if (dataTexto) {
                let [dia, mes, ano] = dataTexto.split("/");
                mesAnoLinha = `${ano}-${mes.padStart(2, "0")}`;
            }

            const correspondeTipo = !tipo || status === tipo;
            const correspondeMes = !mesSelecionado || mesSelecionado === mesAnoLinha;
            const correspondeBanco = !bancoFiltro || banco.includes(bancoFiltro);
            const correspondeFuncionario = !funcionarioFiltro || funcionario.includes(funcionarioFiltro);
            const correspondeCliente = !clienteFiltro || cliente.includes(clienteFiltro);

            const mostrar = correspondeTipo && correspondeMes && correspondeBanco && correspondeFuncionario && correspondeCliente;

            linha.style.display = mostrar ? "" : "none";
            if (mostrar) linhasVisiveis++;
        });

        console.log(`📌 ${linhasVisiveis} linhas visíveis após filtragem.`);
        if (linhasVisiveis === 0) console.warn("⚠ Nenhum resultado encontrado com os filtros aplicados!");

        calcularTotaisTabela();
        atualizarCoresStatus();
    });

    /** =================== COLORIR STATUS =================== **/
    function atualizarCoresStatus() {
        tabela.querySelectorAll("tr").forEach((linha) => {
            const statusCell = linha.cells[8];
            if (statusCell) {
                statusCell.style.color = statusCell.textContent.trim().toLowerCase() === "entrada" ? "green" : "red";
            }
        });
    }

    calcularTotaisTabela();
    atualizarCoresStatus();
});
