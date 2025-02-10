document.addEventListener("DOMContentLoaded", function () {

    // ================= MODAL DE ADICIONAR EMPRÉSTIMO =================
    const addButton = document.querySelector(".add");
    const modalAdd = document.getElementById("modal-add-emprestimo");
    const closeAddModalButton = document.getElementById("close-modal");

    if (addButton && modalAdd && closeAddModalButton) {
        addButton.addEventListener("click", () => {
            modalAdd.classList.remove("hide");
            modalAdd.classList.add("show");
        });

        closeAddModalButton.addEventListener("click", () => {
            modalAdd.classList.remove("show");
            modalAdd.classList.add("hide");
        });

        modalAdd.addEventListener("click", (event) => {
            if (event.target === modalAdd) {
                modalAdd.classList.remove("show");
                modalAdd.classList.add("hide");
            }
        });
    }

    // ================= MODAL DE NOTIFICAÇÕES =================
    const openModalButton = document.querySelector("#open-modal .notification");
    const modalNotification = document.getElementById("modal");
    const fade = document.getElementById("fade");
    const closeModalButton = document.getElementById("close-modal");

    if (openModalButton && modalNotification && fade && closeModalButton) {
        openModalButton.addEventListener("click", function () {
            modalNotification.classList.remove("hide");
            fade.classList.remove("hide");
        });

        closeModalButton.addEventListener("click", function () {
            modalNotification.classList.add("hide");
            fade.classList.add("hide");
        });

        fade.addEventListener("click", function () {
            modalNotification.classList.add("hide");
            fade.classList.add("hide");
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
            modalCliente.classList.remove("hide");
            modalCliente.classList.add("show");
        });

        closeClienteButton.addEventListener("click", () => {
            modalCliente.classList.remove("show");
            modalCliente.classList.add("hide");
        });

        modalCliente.addEventListener("click", (event) => {
            if (event.target === modalCliente) {
                modalCliente.classList.remove("show");
                modalCliente.classList.add("hide");
            }
        });
    }

    // ================= TOGGLE DO MENU DO PERFIL =================
    const profile = document.querySelector(".profile");
    const subMenu = document.getElementById("subMenu");

    if (profile && subMenu) {
        // Evento de clique no perfil para abrir/fechar o submenu
        profile.addEventListener("click", function (event) {
            event.stopPropagation(); // Evita fechamento imediato
            subMenu.classList.toggle("show");

            console.log("🔹 Submenu status:", subMenu.classList.contains("show")); // 🔹 Debug no console
        });

        // Evento para fechar o submenu ao clicar fora dele
        document.addEventListener("click", function (event) {
            if (!profile.contains(event.target) && !subMenu.contains(event.target)) {
                subMenu.classList.remove("show");
            }
        });
    } else {
        console.error("❌ Elemento 'profile' ou 'subMenu' não encontrado!");
    }

});

// =================== FUNÇÃO GERAL PARA O SUBMENU ===================
function toggleSubMenu() {
    const subMenu = document.getElementById("subMenu");
    if (subMenu) {
        subMenu.classList.toggle("show");
    } else {
        console.error("❌ Elemento 'subMenu' não encontrado!");
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const profile = document.querySelector(".profile");
    const subMenu = document.getElementById("subMenu");

    if (profile && subMenu) {
        // Evento de clique no perfil para abrir/fechar o submenu
        profile.addEventListener("click", function (event) {
            event.stopPropagation(); // Evita fechamento imediato
            subMenu.classList.toggle("show");

            console.log("🔹 Submenu status:", subMenu.classList.contains("show")); // 🔹 Debug no console
        });

        // Evento para fechar o submenu ao clicar fora dele
        document.addEventListener("click", function (event) {
            if (!profile.contains(event.target) && !subMenu.contains(event.target)) {
                subMenu.classList.remove("show");
            }
        });
    } else {
        console.error("❌ Elemento 'profile' ou 'subMenu' não encontrado!");
    }
});
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".pagar-parcela").forEach(button => {
        button.addEventListener("click", function () {
            const parcelaId = this.getAttribute("data-id");

            fetch(`/pagar-parcela/${parcelaId}`, {
                method: "POST"
            })
                .then(response => response.text())
                .then(message => {
                    alert(message);
                    location.reload(); // 🔄 Atualiza a página para refletir a mudança no status
                })
                .catch(error => console.error("Erro ao pagar parcela:", error));
        });
    });
});


