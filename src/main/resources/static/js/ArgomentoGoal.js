let initialState = [];
function saveInitialState() {
    const boxes = document.querySelectorAll('.goal_box');
    initialState = Array.from(boxes).map(box => ({
        id: box.id,
        backgroundColor: box.style.backgroundColor,
        textContent: box.querySelector('span').textContent,
    }));
}

function applyEventListeners() {
    const boxElements = document.querySelectorAll(".goal_box");
    boxElements.forEach(elem => {
        elem.addEventListener("dragstart", dragStart);
        elem.addEventListener("dragend", dragEnd);
        elem.addEventListener("dragenter", dragEnter);
        elem.addEventListener("dragover", dragOver);
        elem.addEventListener("dragleave", dragLeave);
        elem.addEventListener("drop", drop);
    });

    const trash = document.getElementById("trash");
    trash.addEventListener("dragover", dragOver);
    trash.addEventListener("drop", drop);

    const restoreBtn = document.querySelector('.restore-btn');
    restoreBtn.addEventListener('click', restoreInitialState);
}

function dragStart(event) {
    event.target.classList.add("drag-start");
    event.dataTransfer.setData("text", event.target.id);
}

function dragEnd(event) {
    event.target.classList.remove("drag-start");
}

function dragEnter(event) {
    if (!event.target.classList.contains("drag-start")) {
        event.target.classList.add("drag-enter");
    }
}

function dragOver(event) {
    event.preventDefault();
}

function dragLeave(event) {
    event.target.classList.remove("drag-enter");
}

function drop(event) {
    event.preventDefault();
    event.target.classList.remove("drag-enter");
    const draggableElementId = event.dataTransfer.getData("text");
    const droppableElementId = event.target.id;
    if (droppableElementId === "trash" || event.currentTarget.id === "trash") {
        const draggableElement = document.getElementById(draggableElementId);
        draggableElement.parentNode.removeChild(draggableElement);
        draggableElement.classList.add("removed");
        setTimeout(() => {
            draggableElement.parentNode.removeChild(draggableElement);
            updateContainerOrder();
        }, 300);
    } else if (draggableElementId !== droppableElementId) {
        const draggableElement = document.getElementById(draggableElementId);
        const droppableElementBgColor = event.target.style.backgroundColor;
        const droppableElementTextContent = event.target.querySelector("span").textContent;

        event.target.style.backgroundColor = draggableElement.style.backgroundColor;
        event.target.querySelector("span").textContent = draggableElement.querySelector("span").textContent;
        event.target.id = draggableElementId;
        draggableElement.style.backgroundColor = droppableElementBgColor;
        draggableElement.querySelector("span").textContent = droppableElementTextContent;
        draggableElement.id = droppableElementId;
    }
    updateContainerOrder();
}

function updateContainerOrder() {
    const containerOrder = Array.from(document.querySelectorAll(".goal_box")).map(elem => elem.id);
    console.log("Ordine attuale dei container:", containerOrder);
    return containerOrder;
}

function restoreInitialState() {
    const container = document.getElementById('containerGoal');
    container.innerHTML = '';
    initialState.forEach(state => {
        const box = document.createElement('div');
        box.className = 'goal_box restored';
        box.draggable = true;
        box.id = state.id;
        box.style.backgroundColor = state.backgroundColor;
        box.innerHTML = `<span>${state.textContent}</span>`;
        container.appendChild(box);
        setTimeout(() => {
            box.classList.remove('restored');
        }, 300);
    });
    applyEventListeners();
    console.log("Stato ripristinato");
}

$(document).ready(function () {
    saveInitialState();
    applyEventListeners();
});
