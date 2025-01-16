// Espera a página carregar para associar os eventos aos elementos
document.addEventListener("DOMContentLoaded", function () {

    // Modal de Adicionar Emprestimo
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

    // Fechar modal clicando fora dele (Adicione essa condição só no caso de você querer)
    modalAdd.addEventListener("click", (event) => {
        if (event.target === modalAdd) {
            modalAdd.classList.remove("show");
            modalAdd.classList.add("hide");
        }
    });

    // -------------------------------------------
    // Modal de Notificação
    const openModalButton = document.querySelector("#open-modal .notification");
    const modalNotification = document.getElementById("modal");
    const fade = document.querySelector("#fade");
    const closeModalButton = document.getElementById("close-modal");

    // Função para abrir o modal de notificações
    openModalButton.addEventListener("click", () => {
        console.log("Botão de notificações clicado!");
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
});


function toggleSubMenu() {
    const subMenu = document.getElementById('subMenu'); // Pegando a referência do submenu
    subMenu.classList.toggle('show'); // Alterna entre visível e invisível
}

// Caso deseje fechar o submenu ao clicar fora dele, adicione a lógica:
document.addEventListener('click', function(event) {
    const profile = document.querySelector('.profile'); // Div que contém o botão de clique
    const subMenu = document.getElementById('subMenu');

    // Se o clique ocorrer fora do submenu e do perfil, feche o submenu
    if (!profile.contains(event.target) && !subMenu.contains(event.target)) {
        subMenu.classList.remove('show'); // Esconde o submenu
    }
});

