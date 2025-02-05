// Espera a página carregar para associar os eventos aos elementos
document.addEventListener("DOMContentLoaded", function () {

    // ================= MODAL DE ADICIONAR EMPRESTIMO =================
    const addButton = document.querySelector(".add");
    const modalAdd = document.getElementById("modal-add-emprestimo");
    const closeAddModalButton = document.getElementById("close-modal");

    // Função para abrir o modal de Add Emprestimo
    addButton.addEventListener("click", () => {
        modalAdd.classList.remove("hide");
        modalAdd.classList.add("show");
    });

    // Função para fechar o modal de Add Emprestimo
    closeAddModalButton.addEventListener("click", () => {
        modalAdd.classList.remove("show");
        modalAdd.classList.add("hide");
    });

    // Fechar modal clicando fora dele
    modalAdd.addEventListener("click", (event) => {
        if (event.target === modalAdd) {
            modalAdd.classList.remove("show");
            modalAdd.classList.add("hide");
        }
    });

    // ================= MODAL DE NOTIFICAÇÕES =================
    const openModalButton = document.querySelector("#open-modal .notification");
    const modalNotification = document.getElementById("modal");
    const fade = document.querySelector("#fade");
    const closeModalButton = document.getElementById("close-modal");

    // Função para abrir o modal de notificações
    openModalButton.addEventListener("click", () => {
        modalNotification.classList.remove("hide");
        fade.classList.remove("hide");
    });

    // Função para fechar o modal de notificações
    closeModalButton.addEventListener("click", () => {
        modalNotification.classList.add("hide");
        fade.classList.add("hide");
    });

    // Fechar o modal de notificações clicando fora dele (no fade)
    fade.addEventListener("click", () => {
        modalNotification.classList.add("hide");
        fade.classList.add("hide");
    });

    // ================= MODAL DE ADICIONAR CLIENTE =================
    const addClienteButton = document.querySelector(".addC"); // Botão que abre o modal
    const modalCliente = document.getElementById("modal-add-cliente"); // O próprio modal
    const closeClienteButton = modalCliente.querySelector(".close"); // Botão de fechar

    // Função para abrir o modal de adicionar cliente
    addClienteButton.addEventListener("click", () => {
        modalCliente.classList.remove("hide");
        modalCliente.classList.add("show");
    });

    // Função para fechar o modal de adicionar cliente
    closeClienteButton.addEventListener("click", () => {
        modalCliente.classList.remove("show");
        modalCliente.classList.add("hide");
    });

    // Fechar modal clicando fora dele
    modalCliente.addEventListener("click", (event) => {
        if (event.target === modalCliente) {
            modalCliente.classList.remove("show");
            modalCliente.classList.add("hide");
        }
    });

    // ================= FILTRAR EMPRESTIMOS POR MÊS =================
    const monthSelect = document.getElementById("month-select");
    const tabelaContainer = document.getElementById("tabelas-container");
    let dadosEmprestimos = JSON.parse(document.getElementById("dados-emprestimos").textContent);

    function carregarTabela(mes) {
        tabelaContainer.innerHTML = ""; // Limpa todas as tabelas antes de atualizar

        for (let dia = 1; dia <= 31; dia++) {
            let tabelaDia = document.createElement("div");
            tabelaDia.classList.add("tabela-dia");
            tabelaDia.innerHTML = `
                <h3>Dia ${dia}</h3>
                <table class="tabela-historico">
                    <thead>
                        <tr>
                            <th>Cliente</th>
                            <th>Valor Mensal</th>
                            <th>Dia do Empréstimo</th>
                            <th>Datas de Pagamento</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody id="tbody-${dia}"></tbody>
                </table>
            `;
            tabelaContainer.appendChild(tabelaDia);
        }
        preencherDados(mes);
    }

    function preencherDados(mes) {
        for (let dia = 1; dia <= 31; dia++) {
            let tbody = document.getElementById(`tbody-${dia}`);
            if (tbody) tbody.innerHTML = ""; // Limpa as linhas antes de inserir novas
        }

        dadosEmprestimos.forEach(emprestimo => {
            const dataCriacao = new Date(emprestimo.created);
            const dia = dataCriacao.getDate();
            const mesEmprestimo = dataCriacao.getMonth() + 1;

            if (mes === 0 || mes === mesEmprestimo) {
                const tbody = document.getElementById(`tbody-${dia}`);
                if (tbody) {
                    const tr = document.createElement("tr");
                    tr.innerHTML = `
                        <td>${emprestimo.cliente}</td>
                        <td>${emprestimo.valorMensal}</td>
                        <td>${emprestimo.created}</td>
                        <td>
                            <ul>
                                ${emprestimo.datasPagamento
                        .filter(data => new Date(data).getMonth() + 1 === mes || mes === 0)
                        .map(data => `<li>${data}</li>`)
                        .join('')}
                            </ul>
                        </td>
                        <td>${emprestimo.status}</td>
                    `;
                    tbody.appendChild(tr);
                }
            }
        });
    }

    monthSelect.addEventListener("change", function () {
        const mesSelecionado = parseInt(monthSelect.value);
        carregarTabela(mesSelecionado);
    });

    carregarTabela(0); // Carrega todos os meses por padrão
});



// ================= TOGGLE DO MENU DO PERFIL =================
document.addEventListener("DOMContentLoaded", function () {

    // ==================== TOGGLE DO MENU DO PERFIL ====================
    const profile = document.querySelector(".profile");  // O botão do perfil
    const subMenu = document.getElementById("subMenu"); // O submenu

    // Função para abrir e fechar o submenu
    function toggleSubMenu(event) {
        event.stopPropagation(); // Impede que o clique se propague
        subMenu.classList.toggle("show"); // Alterna entre visível e invisível
    }

    // Evento para abrir o submenu ao clicar no perfil
    profile.addEventListener("click", toggleSubMenu);

    // Fechar o submenu ao clicar fora dele
    document.addEventListener("click", function (event) {
        if (!profile.contains(event.target) && !subMenu.contains(event.target)) {
            subMenu.classList.remove("show"); // Esconde o submenu
        }
    });

});





