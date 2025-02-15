document.addEventListener("DOMContentLoaded", function () {

    // ================= MODAL DE ADICIONAR EMPRÉSTIMO =================
    const addButton = document.querySelector(".add");
    const modalAdd = document.getElementById("modal-add-emprestimo");
    const closeAddModalButton = document.querySelector("#modal-add-emprestimo .close-button");

    if (addButton && modalAdd && closeAddModalButton) {
        addButton.addEventListener("click", () => {
            modalAdd.classList.add("show");
            modalAdd.classList.remove("hide");
        });

        closeAddModalButton.addEventListener("click", () => {
            modalAdd.classList.add("hide");
            modalAdd.classList.remove("show");
        });
    } else {
        console.error("❌ Verifique se os IDs e classes do modal de empréstimo estão corretos!");
    }

    // ================= MODAL DE NOTIFICAÇÕES =================
    const openModalButton = document.querySelector("#open-modal .notification");
    const modalNotification = document.getElementById("modal");
    const fade = document.getElementById("fade");
    const closeModalButton = document.getElementById("close-modal");

    if (openModalButton && modalNotification && fade && closeModalButton) {
        openModalButton.addEventListener("click", function () {
            modalNotification.classList.add("show");
            modalNotification.classList.remove("hide");
            fade.classList.add("show");
            fade.classList.remove("hide");
        });

        closeModalButton.addEventListener("click", function () {
            modalNotification.classList.add("hide");
            modalNotification.classList.remove("show");
            fade.classList.add("hide");
            fade.classList.remove("show");
        });

        fade.addEventListener("click", function () {
            modalNotification.classList.add("hide");
            modalNotification.classList.remove("show");
            fade.classList.add("hide");
            fade.classList.remove("show");
        });
    } else {
        console.error("❌ Elementos do modal de notificações não encontrados!");
    }

    // ================= MODAL DE ADICIONAR CLIENTE =================
    const addClienteButton = document.querySelector(".addC");
    const modalCliente = document.getElementById("modal-add-cliente");
    const closeClienteButton = modalCliente?.querySelector(".close");

    if (addClienteButton && modalCliente && closeClienteButton) {
        addClienteButton.addEventListener("click", () => {
            modalCliente.classList.add("show");
            modalCliente.classList.remove("hide");
        });

        closeClienteButton.addEventListener("click", () => {
            modalCliente.classList.add("hide");
            modalCliente.classList.remove("show");
        });

        modalCliente.addEventListener("click", (event) => {
            if (event.target === modalCliente) {
                modalCliente.classList.add("hide");
                modalCliente.classList.remove("show");
            }
        });
    }

    // ================= TOGGLE DO MENU DO PERFIL =================
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
    } else {
        console.error("❌ Elemento 'profile' ou 'subMenu' não encontrado!");
    }

    // =================== PAGAMENTO DE PARCELA ===================
    document.querySelectorAll(".pagar-parcela").forEach(button => {
        button.addEventListener("click", function () {
            const parcelaId = this.getAttribute("data-id");

            fetch(`/pagar-parcela/${parcelaId}`, {
                method: "POST"
            })
                .then(response => response.text())
                .then(message => {
                    alert(message);
                    location.reload();
                })
                .catch(error => console.error("Erro ao pagar parcela:", error));
        });
    });

    // =================== MODAL LISTA DE CLIENTES ===================
    const clientsListWindow = document.getElementById('clientsListWindow');
    const openClientsButton = document.getElementById('openClientsModal');
    const closeClientsButton = document.getElementById('closeClientsList');

    if (clientsListWindow && openClientsButton && closeClientsButton) {
        openClientsButton.addEventListener('click', () => {
            clientsListWindow.style.display = 'block';
        });

        closeClientsButton.addEventListener('click', () => {
            clientsListWindow.style.display = 'none';
        });

        window.addEventListener('click', (e) => {
            if (e.target === clientsListWindow) {
                clientsListWindow.style.display = 'none';
            }
        });
    }

    // =================== MODAL LISTA DE SÓCIOS ===================
    const sociosListWindow = document.getElementById('sociosListWindow');
    const openSociosButton = document.getElementById('openEmployeesModal');
    const closeSociosButton = document.getElementById('closeSociosList');

    if (sociosListWindow && openSociosButton && closeSociosButton) {
        openSociosButton.addEventListener('click', () => {
            sociosListWindow.style.display = 'block';
        });

        closeSociosButton.addEventListener('click', () => {
            sociosListWindow.style.display = 'none';
        });

        window.addEventListener('click', (e) => {
            if (e.target === sociosListWindow) {
                sociosListWindow.style.display = 'none';
            }
        });
    }

    // ================= MODAL DE ADICIONAR FUNCIONÁRIO =================
    const openModalFuncionario = document.getElementById("openAddFuncionario");
    const closeModalFuncionario = document.getElementById("closeAddFuncionario");
    const modalFuncionario = document.getElementById("modal-add-funcionario");

    if (openModalFuncionario && closeModalFuncionario && modalFuncionario) {
        openModalFuncionario.addEventListener("click", function () {
            modalFuncionario.classList.add("show");
            modalFuncionario.classList.remove("hide");
        });

        closeModalFuncionario.addEventListener("click", function () {
            modalFuncionario.classList.add("hide");
            modalFuncionario.classList.remove("show");
        });

        window.addEventListener("click", function (event) {
            if (event.target === modalFuncionario) {
                modalFuncionario.classList.add("hide");
                modalFuncionario.classList.remove("show");
            }
        });
    } else {
        console.error("❌ Elementos do modal de funcionário não foram encontrados!");
    }

});

document.addEventListener("DOMContentLoaded", function () {

    // ================= MODAL DE ADICIONAR EMPRÉSTIMO =================
    const addButton = document.querySelector(".add");
    const modalAdd = document.getElementById("modal-add-emprestimo");
    const closeAddModalButton = modalAdd?.querySelector(".close-button");

    if (addButton && modalAdd && closeAddModalButton) {
        // Abrir Modal
        addButton.addEventListener("click", () => {
            modalAdd.classList.add("show");
            modalAdd.classList.remove("hide");
        });

        // Fechar Modal ao clicar no botão X
        closeAddModalButton.addEventListener("click", () => {
            modalAdd.classList.add("hide");
            modalAdd.classList.remove("show");
        });

        // Fechar Modal ao clicar fora do conteúdo
        modalAdd.addEventListener("click", (event) => {
            if (event.target === modalAdd) {
                modalAdd.classList.add("hide");
                modalAdd.classList.remove("show");
            }
        });
    } else {
        console.error("❌ Verifique se os elementos do modal de empréstimo estão corretos.");
    }
});

