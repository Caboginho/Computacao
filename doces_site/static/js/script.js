// Cards fade-in
const cards = document.querySelectorAll('.card');

function checkCards() {
    const triggerBottom = window.innerHeight * 0.85;

    cards.forEach(card => {
        const cardTop = card.getBoundingClientRect().top;
        if(cardTop < triggerBottom) {
            card.classList.add('show');
        }
    });
}

window.addEventListener('scroll', checkCards);
window.addEventListener('load', checkCards);

function toggleMenu() {
    const menu = document.getElementById('nav-menu');
    menu.classList.toggle('show');
}
