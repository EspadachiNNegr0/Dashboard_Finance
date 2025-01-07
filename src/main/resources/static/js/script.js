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


