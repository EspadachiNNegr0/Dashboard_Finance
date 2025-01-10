const allSideMenu = document.querySelectorAll('#sidebar .side-menu.top li a');

allSideMenu.forEach(item=> {
    const li = item.parentElement;

    item.addEventListener('click', function () {
        allSideMenu.forEach(i=> {
            i.parentElement.classList.remove('active');
        })
        li.classList.add('active');
    })
});


// TOGGLE SIDEBAR
const menuBar = document.querySelector('#content nav .bx.bx-menu');
const sidebar = document.getElementById('sidebar');

menuBar.addEventListener('click', function () {
    sidebar.classList.toggle('hide');
})


const searchButton = document.querySelector('#content nav form .form-input button');
const searchButtonIcon = document.querySelector('#content nav form .form-input button .bx');
const searchForm = document.querySelector('#content nav form');

searchButton.addEventListener('click', function (e) {
    if(window.innerWidth < 576) {
        e.preventDefault();
        searchForm.classList.toggle('show');
        if(searchForm.classList.contains('show')) {
            searchButtonIcon.classList.replace('bx-search', 'bx-x');
        } else {
            searchButtonIcon.classList.replace('bx-x', 'bx-search');
        }
    }
})





if(window.innerWidth < 768) {
    sidebar.classList.add('hide');
} else if(window.innerWidth > 576) {
    searchButtonIcon.classList.replace('bx-x', 'bx-search');
    searchForm.classList.remove('show');
}


window.addEventListener('resize', function () {
    if(this.innerWidth > 576) {
        searchButtonIcon.classList.replace('bx-x', 'bx-search');
        searchForm.classList.remove('show');
    }
})



const switchMode = document.getElementById('switch-mode');

// Verifica se existe uma preferência no localStorage e aplica
if (localStorage.getItem('theme') === 'dark') {
    document.body.classList.add('dark');
    switchMode.checked = true; // Marca o switch para indicar que o modo escuro está ativo
}

// Adiciona evento para alternar entre os temas e salvar a preferência
switchMode.addEventListener('change', function () {
    if (this.checked) {
        document.body.classList.add('dark');
        localStorage.setItem('theme', 'dark'); // Salva a preferência no localStorage
    } else {
        document.body.classList.remove('dark');
        localStorage.setItem('theme', 'light'); // Salva a preferência no localStorage
    }
});

// O restante do código permanece o mesmo



// JS para o submenu
function toggleSubMenu() {
    const subMenu = document.getElementById("subMenu");
    subMenu.classList.toggle("show");
}

// Fecha o submenu ao clicar fora
window.addEventListener("click", function (e) {
    const subMenu = document.getElementById("subMenu");
    if (!e.target.closest(".profile") && !e.target.closest(".sub-menu-wrap")) {
        subMenu.classList.remove("show");
    }
});

// Código para alternar a visibilidade do submenu
document.querySelector('.profile').addEventListener('click', function() {
    const submenu = document.querySelector('.sub-menu-wrap');
    submenu.style.visibility = submenu.style.visibility === 'visible' ? 'hidden' : 'visible';
    submenu.style.opacity = submenu.style.opacity === '1' ? '0' : '1';
});

const openModalButton = document.querySelector("#open-modal");
const closeModalButton = document.querySelector("#close-modal");
const modal = document.querySelector("#modal");
const fade = document.querySelector("#fade");

const toggleModal = () => {
    modal.classList.toggle("hide");
    fade.classList.toggle("hide");
}

[openModalButton, closeModalButton, fade].forEach((el) => {
    el.addEventListener("click", () => toggleModal());
});

document.addEventListener('DOMContentLoaded', () => {
    const icon = document.querySelector('.searchT .icon');
    const search = document.querySelector('.searchT');

    if (icon && search) {
        icon.onclick = function () {
            console.log("Botão foi clicado!"); // Mensagem de teste no console
            search.classList.toggle('active');
            console.log("Classe 'active':", search.classList.contains('active')); // Verifica se a classe foi adicionada ou removida
        };
    } else {
        console.error("Elementos '.searchT' ou '.icon' não encontrados no DOM!");
    }
});


document.addEventListener("DOMContentLoaded", () => {
    const addButton = document.querySelector(".add");
    const modal = document.getElementById("modal-add-emprestimo");
    const closeButton = document.getElementById("close-modal");

    // Função para abrir o modal
    addButton.addEventListener("click", () => {
        modal.classList.remove("hide");
        modal.classList.add("show");
    });

    // Função para fechar o modal
    closeButton.addEventListener("click", () => {
        modal.classList.remove("show");
        modal.classList.add("hide");
    });

    // Fechar modal clicando fora dele
    modal.addEventListener("click", (event) => {
        if (event.target === modal) {
            modal.classList.remove("show");
            modal.classList.add("hide");
        }
    });
});

document.addEventListener("DOMContentLoaded", () => {
    const addButton = document.querySelector(".openModalDescrip");
    const modals = document.getElementById("modals-emprestimo");
    const closeButton = document.getElementById("close-modals");

    // Função para abrir o modals
    addButton.addEventListener("click", () => {
        modals.classList.remove("hide");
        modals.classList.add("show");
    });

    // Função para fechar o modals
    closeButton.addEventListener("click", () => {
        modals.classList.remove("show");
        modals.classList.add("hide");
    });

    // Fechar modals clicando fora dele
    modals.addEventListener("click", (event) => {
        if (event.target === modals) {
            modals.classList.remove("show");
            modals.classList.add("hide");
        }
    });
});


document.querySelectorAll('.openModalDescrip').forEach(button => {
    button.addEventListener('click', function () {
        // Obtenha o modal
        const modal = document.getElementById('modals-emprestimo');

        // Preencha os campos do modal com os dados do botão
        document.getElementById('modal-id').innerText = this.getAttribute('data-id');
        document.getElementById('modal-cliente').innerText = this.getAttribute('data-cliente');
        document.getElementById('modal-price').innerText = this.getAttribute('data-price');
        document.getElementById('modal-percentage').innerText = this.getAttribute('data-percentage');
        document.getElementById('modal-status').innerText = this.getAttribute('data-status');
        document.getElementById('modal-description').innerText = this.getAttribute('data-description');
        document.getElementById('modal-created').innerText = this.getAttribute('data-created');
        document.getElementById('modal-parcelamento').innerText = this.getAttribute('data-parcelamento');
        document.getElementById('modal-socios').innerText = this.getAttribute('data-socios');
        document.getElementById('modal-banco').innerText = this.getAttribute('data-banco');

        // Exiba o modal
        modal.classList.remove('hide');
    });
});

// Fechar o modal
document.getElementById('close-modals').addEventListener('click', () => {
    document.getElementById('modals-emprestimo').classList.add('hide');
});


//JS DO MODAL

document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('user-modal');
    const fade = document.getElementById('fade-user-modal');
    const closeModalBtn = document.getElementById('close-user-modal');
    const openModalElements = document.querySelectorAll('.open-modal');

    function openModal(userId) {
        // Aqui você pode carregar os dados reais do usuário com base no ID
        console.log(`Abrindo modal para o usuário com ID: ${userId}`);

        // Exibir modal
        modal.classList.remove('hide');
        fade.classList.remove('hide');
    }

    function closeModal() {
        modal.classList.add('hide');
        fade.classList.add('hide');
    }

    openModalElements.forEach((element) => {
        element.addEventListener('click', () => {
            const userId = element.getAttribute('data-id'); // Pega o ID do usuário
            openModal(userId);
        });
    });

    closeModalBtn.addEventListener('click', closeModal);
    fade.addEventListener('click', closeModal);
});


// Função que abre o modal com os dados do cliente
function OpenModalOnTable(event) {
    const clienteNome = event.target.textContent.trim(); // Captura o texto do elemento clicado (nome do cliente)

    const cliente = {
        nome: clienteNome,
        idade: 34, // Exemplo fixo, pode ser substituído pelos dados reais
        cpf: '87556643556', // Exemplo fixo
        history: [
            { valor: 'R$ 500,00', parcelas: '5x', data: '12/12/2024', porcentagem: 5, codigo: 'ABC123', status: 'Pendente' },
            { valor: 'R$ 300,00', parcelas: '3x', data: '11/11/2024', porcentagem: 10, codigo: 'XYZ789', status: 'Pago' },
        ]
    };

    openModalDetails(cliente); // Chama a função para abrir o modal com os dados capturados
}

// Função para abrir o modal de cliente
function openModalDetails(cliente) {
    const modalContainer = document.querySelector('.modal-container-custom');
    modalContainer.classList.remove('hide'); // Exibe o modal

    // Preenche os dados do modal com as informações do cliente
    document.getElementById('modal-nome').innerText = cliente.nome || "Não informado";
    document.getElementById('modal-idade').innerText = cliente.idade || "Não informado";
    document.getElementById('modal-cpf').innerText = cliente.cpf || "Não informado";

    const historicoBody = document.getElementById('modal-historico-body');
    historicoBody.innerHTML = ''; // Limpa o histórico anterior

    if (cliente.history) {
        cliente.history.forEach(historia => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${historia.valor || "N/A"}</td>
                <td>${historia.parcelas || "N/A"}</td>
                <td>${historia.data || "N/A"}</td>
                <td>${historia.porcentagem || "N/A"}%</td>
                <td>${historia.codigo || "N/A"}</td>
                <td>${historia.status || "N/A"}</td>
            `;
            historicoBody.appendChild(row);
        });
    }
}

// Função para fechar o modal
function closeModalDetails() {
    document.querySelector('.modal-container-custom').classList.add('hide'); // Esconde o modal
}

// Evento para fechar o modal ao clicar fora do conteúdo
document.querySelector('.modal-container-custom').addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-container-custom')) {
        closeModalDetails();
    }
});