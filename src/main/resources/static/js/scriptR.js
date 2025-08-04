document.addEventListener("DOMContentLoaded", () => {
    console.log("🚀 Script carregado!");

    const inputMesAno = document.getElementById("filtro-mes");

    inputMesAno.addEventListener("change", function () {
        // Garante que o valor fique no formato "YYYY-MM"
        if (this.value.length >= 7) {
            this.value = this.value.substring(0, 7);
        }
    });



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
    const totalProjetadasElement = document.getElementById("total-projetadas"); // 🔹 Novo elemento

    function formatarMoeda(valor) {
        return `R$ ${valor.toFixed(2).replace(".", ",")}`;
    }

    function calcularTotaisTabela() {
        let totalEntradas = 0, totalSaidas = 0, totalJuros = 0, totalAmortizacao = 0, totalProjetadas = 0;

        document.querySelectorAll("#example tbody tr:not([style*='display: none'])").forEach((linha) => {
            const obterValor = (index) => parseFloat(linha.cells[index]?.innerText.replace("R$", "").trim().replace(",", ".")) || 0;

            const valor = obterValor(5);
            const juros = obterValor(6);
            const amortizacao = obterValor(7);
            const status = linha.cells[8]?.innerText.trim().toLowerCase();

            if (status === "entrada") totalEntradas += valor;
            if (status === "saida") totalSaidas += valor;
            if (status === "projetada") totalProjetadas += valor;

            totalJuros += juros;
            totalAmortizacao += amortizacao;
        });

        totalEntradasElement.textContent = formatarMoeda(totalEntradas);
        totalSaidasElement.textContent = formatarMoeda(totalSaidas);
        saldoFinalElement.textContent = formatarMoeda(totalEntradas - totalSaidas);
        totalJurosElement.textContent = formatarMoeda(totalJuros);
        totalAmortizacaoElement.textContent = formatarMoeda(totalAmortizacao);
        totalProjetadasElement.textContent = formatarMoeda(totalProjetadas); // 🔹 Atualiza o box
    }

    /** =================== FILTRO =================== **/
    const formFiltro = document.getElementById("form-filtro");
    const tabela = document.querySelector("#example tbody");

    formFiltro.addEventListener("submit", (event) => {
        event.preventDefault();

        const tipo = document.getElementById("filtro-tipo").value.toLowerCase();
        const mesSelecionado = document.getElementById("filtro-mes").value; // Formato YYYY-MM
        const bancoFiltro = document.getElementById("filtro-banco").value.trim().toLowerCase();
        const funcionarioFiltro = document.getElementById("filtro-funcionario").value.trim().toLowerCase();
        const clienteFiltro = document.getElementById("filtro-cliente").value.trim().toLowerCase();

        let linhasVisiveis = 0;

        tabela.querySelectorAll("tr").forEach((linha) => {
            const status = linha.cells[8]?.innerText.trim().toLowerCase();
            const dataTexto = linha.cells[4]?.innerText.trim(); // Supondo que a data esteja na coluna 4
            const banco = linha.cells[1]?.innerText.trim().toLowerCase();
            const funcionario = linha.cells[2]?.innerText.trim().toLowerCase();
            const cliente = linha.cells[3]?.innerText.trim().toLowerCase();

            let mesAnoLinha = "";
            if (dataTexto) {
                const partesData = dataTexto.split("/");
                if (partesData.length === 3) {
                    const [dia, mes, ano] = partesData;
                    mesAnoLinha = `${ano}-${mes.padStart(2, "0")}`; // Converte para formato YYYY-MM
                }
            }

            // 🔹 Verifica se o filtro "Tipo" corresponde ao status da linha
            const correspondeTipo = !tipo || status === tipo;

            // 🔹 Verifica se a data da linha corresponde ao mês/ano selecionado
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
                const statusTexto = statusCell.textContent.trim().toLowerCase();

                if (statusTexto === "entrada") {
                    statusCell.style.color = "green";
                } else if (statusTexto === "projetada") {
                    statusCell.style.color = "orange"; // 🔹 Projetada em laranja
                } else {
                    statusCell.style.color = "red"; // 🔹 Outros status em vermelho
                }
            }
        });
    }

    calcularTotaisTabela();
    atualizarCoresStatus();
});

document.addEventListener("DOMContentLoaded", function () {
    const profilePhoto = document.querySelector(".profile-photo");
    const subMenu = document.getElementById("subMenu");

    if (profilePhoto && subMenu) {
        profilePhoto.addEventListener("click", (event) => {
            event.stopPropagation(); // Evita conflito com clique fora
            subMenu.classList.toggle("show");
        });

        // Fecha se clicar fora
        window.addEventListener("click", (event) => {
            if (!subMenu.contains(event.target) && !profilePhoto.contains(event.target)) {
                subMenu.classList.remove("show");
            }
        });
    } else {
        console.warn("⚠️ Elementos do menu de perfil não encontrados.");
    }
});

    function gerarPDF() {
    // Pegar os valores dos filtros
    document.getElementById("pdf-tipo").value = document.getElementById("filtro-tipo").value;
    document.getElementById("pdf-mes").value = document.getElementById("filtro-mes").value;
    document.getElementById("pdf-banco").value = document.getElementById("filtro-banco").value;
    document.getElementById("pdf-funcionario").value = document.getElementById("filtro-funcionario").value;
    document.getElementById("pdf-cliente").value = document.getElementById("filtro-cliente").value;

    // Enviar o formulário (abre em nova aba)
    document.getElementById("form-gerar-pdf").submit();
}

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
// Controle modal notificações
const openNotifBtn = document.querySelector("#open-modal button.notification");
const modalNotif = document.getElementById("modal");
const closeNotifBtn = document.getElementById("close-modal");
const fade = document.getElementById("fade");

if (openNotifBtn && modalNotif && closeNotifBtn && fade) {
    openNotifBtn.addEventListener("click", () => {
        modalNotif.classList.remove("hide");
        fade.classList.remove("hide");
    });

    closeNotifBtn.addEventListener("click", () => {
        modalNotif.classList.add("hide");
        fade.classList.add("hide");
    });

    fade.addEventListener("click", () => {
        modalNotif.classList.add("hide");
        fade.classList.add("hide");
    });
} else {
    console.warn("Elementos do modal de notificações não encontrados.");
}
