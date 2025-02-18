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

document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("modal-add-banco");
    const openButton = document.querySelector(".addB");
    const closeButton = modal?.querySelector(".close-button");

    if (modal && openButton && closeButton) {
        // Abrir o modal ao clicar no botão "Add Banco"
        openButton.addEventListener("click", function () {
            modal.classList.add("show");
            modal.classList.remove("hide");
        });

        // Fechar o modal ao clicar no botão de fechar (X)
        closeButton.addEventListener("click", function () {
            modal.classList.add("hide");
            modal.classList.remove("show");
        });

        // Fechar o modal ao clicar fora dele
        window.addEventListener("click", function (event) {
            if (event.target === modal) {
                modal.classList.add("hide");
                modal.classList.remove("show");
            }
        });
    } else {
        console.error("❌ O modal de adicionar banco ou seus botões não foram encontrados!");
    }
});


document.addEventListener("DOMContentLoaded", () => {
    const modalBanco = document.getElementById("modal-add-banco");
    const closeModalBanco = document.getElementById("close-modal-add-banco");
    const formBanco = document.getElementById("form-add-banco");

    // Fecha o modal ao clicar no botão de fechar
    closeModalBanco.addEventListener("click", () => {
        modalBanco.classList.add("hide");
    });

    // Intercepta o envio do formulário e faz uma requisição AJAX
    formBanco.addEventListener("submit", async (event) => {
        event.preventDefault(); // Impede o envio tradicional do formulário

        const formData = new FormData(formBanco);
        const jsonData = {
            nome: formData.get("nome"),
            descricao: formData.get("descricao")
        };

        try {
            const response = await fetch("/bancos", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(jsonData)
            });

            if (response.ok) {
                alert("Banco cadastrado com sucesso!");
                formBanco.reset(); // Limpa o formulário
                modalBanco.classList.add("hide"); // Fecha o modal
                window.location.reload(); // 🔄 Recarrega a página automaticamente
            } else {
                const errorText = await response.text();
                alert(`Erro: ${errorText}`);
            }
        } catch (error) {
            console.error("Erro ao salvar banco:", error);
            alert("Erro ao cadastrar banco. Tente novamente.");
        }
    });
});
