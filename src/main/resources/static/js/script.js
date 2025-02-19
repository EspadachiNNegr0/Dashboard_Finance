document.addEventListener("DOMContentLoaded", function () {
    const openModalButton = document.getElementById("open-modal");
    const closeModalButton = document.getElementById("close-modal");
    const fade = document.getElementById("fade");
    const modal = document.getElementById("modal");
    const clearNotificationsButton = document.getElementById("clear-notifications");

    // Função para abrir o modal
    const openModal = () => {
        modal.classList.remove("hide");
        fade.classList.remove("hide");
    };

    // Função para fechar o modal
    const closeModal = () => {
        modal.classList.add("hide");
        fade.classList.add("hide");
    };

    // Eventos para abrir e fechar o modal
    openModalButton.addEventListener("click", openModal);
    closeModalButton.addEventListener("click", closeModal);
    fade.addEventListener("click", closeModal);

    // Requisição DELETE para limpar notificações
    if (clearNotificationsButton) {
        clearNotificationsButton.addEventListener("click", function () {
            if (confirm("Tem certeza que deseja apagar todas as notificações?")) {
                fetch("/notifications/clear", {
                    method: "DELETE",
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                    .then(response => {
                            alert("Notificações apagadas com sucesso!");
                            location.reload(); // Recarrega a página após apagar as notificações

                    })
                    .catch(error => {
                        console.error("Erro ao apagar notificações:", error);
                        alert("Erro ao apagar notificações. Verifique o console para mais detalhes.");
                    });
            }
        });
    }
});

document.addEventListener("DOMContentLoaded", function () {

    // ================= MODAL DE ADICIONAR EMPRÉSTIMO =================
    const addButton = document.querySelector(".add");
    const modalAdd = document.getElementById("modal-add-emprestimo");
    const closeAddModalButton = modalAdd?.querySelector(".close-button");

    if (addButton && modalAdd && closeAddModalButton) {
        addButton.addEventListener("click", () => {
            modalAdd.classList.add("show");
            modalAdd.classList.remove("hide");
        });

        closeAddModalButton.addEventListener("click", () => {
            modalAdd.classList.add("hide");
            modalAdd.classList.remove("show");
        });

        modalAdd.addEventListener("click", (event) => {
            if (event.target === modalAdd) {
                modalAdd.classList.add("hide");
                modalAdd.classList.remove("show");
            }
        });
    } else {
        console.error("❌ Verifique se os elementos do modal de empréstimo estão corretos.");
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

    // ================= EXIBIR MENSAGEM DE SUCESSO/ERRO NO MODAL DE CLIENTE =================
    const mensagemSucesso = "[[${success}]]";
    const mensagemErro = "[[${error}]]";
    const alertBox = document.getElementById("cliente-mensagem");
    const alertText = document.getElementById("cliente-alert-text");

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
    // ================= TOGGLE DO MENU DO PERFIL =================
    const profile = document.querySelector(".profile");
    const subMenu = document.getElementById("subMenu");

    if (profile && subMenu) {
        profile.addEventListener("click", function (event) {
            event.stopPropagation(); // Impede o fechamento imediato
            subMenu.classList.toggle("show"); // Alterna a exibição do submenu
        });

        // Fecha o submenu ao clicar fora dele
        document.addEventListener("click", function (event) {
            if (!profile.contains(event.target) && !subMenu.contains(event.target)) {
                subMenu.classList.remove("show");
            }
        });

        console.log("🔹 Submenu configurado com sucesso!"); // Debug no console
    } else {
        console.error("❌ Elemento 'profile' ou 'subMenu' não encontrado!");
    }
});


document.addEventListener("DOMContentLoaded", function () {
    const openModalBanco = document.querySelector(".addB"); // Botão de abrir modal
    const closeModalBanco = document.getElementById("close-modal-add-banco"); // Botão de fechar modal
    const modalBanco = document.getElementById("modal-add-banco"); // Modal
    const formBanco = document.getElementById("form-add-banco"); // Formulário

    if (openModalBanco && closeModalBanco && modalBanco && formBanco) {
        // Abrir o modal ao clicar no botão "Add Banco"
        openModalBanco.addEventListener("click", function () {
            modalBanco.classList.add("show");
            modalBanco.classList.remove("hide");
        });

        // Fechar o modal ao clicar no botão "X"
        closeModalBanco.addEventListener("click", function () {
            modalBanco.classList.add("hide");
            modalBanco.classList.remove("show");
        });

        // Fechar o modal ao clicar fora dele
        window.addEventListener("click", function (event) {
            if (event.target === modalBanco) {
                modalBanco.classList.add("hide");
                modalBanco.classList.remove("show");
            }
        });

        // Confirmação de envio do formulário
        formBanco.addEventListener("submit", function (event) {
            const nome = document.getElementById("nome-banco").value;
            const descricao = document.getElementById("descricao-banco").value;

            console.log("Enviando formulário...");
            console.log("Nome:", nome);
            console.log("Descrição:", descricao);
        });
    } else {
        console.error("❌ O modal de adicionar banco ou seus botões não foram encontrados!");
    }
});

