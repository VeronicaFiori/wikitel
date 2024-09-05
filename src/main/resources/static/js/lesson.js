var max=0;
var value=0;
let currentQuestionIndex = 0;
function showQuestion(index) {
    const questionCards = document.querySelectorAll('.question-card');
    questionCards.forEach((card, i) => {
        card.classList.toggle('active', i === index);
    });

    updateProgress(index);

    document.getElementById('prev-btn').disabled = index === 0;
    if(index === (questionCards.length - 1)){
        document.getElementById('next-btn').innerText = "Submit"
        document.getElementById('next-btn').onclick = checkQuiz
    }else{
        document.getElementById('next-btn').innerText = "Avanti"
        document.getElementById('next-btn').removeAttribute("onclick")

    }
}
function getAllSelectedAnswers() {
    // Seleziona tutti i gruppi di radio buttons
    const radioGroups = document.querySelectorAll('.question-card');

    // Array per memorizzare le risposte selezionate
    const selectedAnswers = [];

    // Itera su ogni gruppo di domande
    radioGroups.forEach((group, index) => {
        // Cerca l'opzione selezionata in ogni gruppo
        const selectedOption = group.querySelector('input[type="radio"]:checked');

        // Se c'è un'opzione selezionata, ottiene il valore
        if (selectedOption) {
            selectedAnswers.push({
                question: selectedOption.name,
                value: selectedOption.value
            });
        } else {
            // Se nessuna opzione è selezionata, salva come non risposta
            selectedAnswers.push({
                question: group.id,
                value: null
            });
        }
    });

    return selectedAnswers;
}

function checkQuiz(){
    document.getElementById("QuizLoaderWrapper").style.display = "none";
    document.getElementById("loaderBackground").style.display = "grid"
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/chekQuiz",
        data:  JSON.stringify(getAllSelectedAnswers()),

        success: function(data) {
            console.log("SUCCESS : ", data);
            createQuestionTemplate(data);
            document.getElementById('statuss').className="badge bg-success";
            document.getElementById('statuss').innerText="Running";


        },
        error: function(e) {
            alert("Error!")
            console.log("ERROR: ", e);
        }
    });
}
function updateProgress(currentStep) {
    const steps = document.querySelectorAll('.stepper-item');

    steps.forEach((step, index) => {
        step.classList.toggle('active', index === currentStep);
        step.classList.toggle('completed', document.querySelector(`input[name="${step.id}"]:checked`));


    });

}

// Event Listener per il pulsante "Indietro"
document.getElementById('prev-btn').addEventListener('click', () => {
    if (currentQuestionIndex > 0) {
        currentQuestionIndex--;
        showQuestion(currentQuestionIndex);
    }
});

// Event Listener per il pulsante "Avanti"
document.getElementById('next-btn').addEventListener('click', () => {
    const questionCards = document.querySelectorAll('.question-card');
    if (currentQuestionIndex < questionCards.length - 1) {
        currentQuestionIndex++;
        showQuestion(currentQuestionIndex);
    }
});
const container = document.getElementById('quizModalBody');

// Funzione per creare la visualizzazione delle domande e risposte
function createQuestionTemplate(questions) {
    document.getElementById("quizModalBody").innerText="";
    let score = 0;
    questions.forEach((item, index) => {
        // Creazione del contenitore della domanda
        const questionContainer = document.createElement('div');
        questionContainer.classList.add('question-container-result');

        // Testo della domanda (dinamico, qui rappresentato da un esempio)
        const questionText = document.createElement('div');
        questionText.classList.add('question');
        questionText.textContent = `Domanda ${index + 1}: ${item.question}`;

        // Creazione del contenitore delle risposte
        const optionsList = document.createElement('ul');
        optionsList.classList.add('options');

        for (const [key, value] of Object.entries(item.options)) {
            const optionItem = document.createElement('li');
            optionItem.textContent = `${key.toUpperCase()}: ${value}`;
            optionsList.appendChild(optionItem);
            if (item.source === item.correct_answer && item.source === key) {
                // Caso risposta corretta
                optionItem.classList.add("correct-answer"); // Verde per la risposta corretta data
                optionItem.style.backgroundColor = "#d4edda"; // Verde chiaro
            } else if (item.source === key) {
                // Caso risposta data sbagliata
                optionItem.classList.add("wrong-answer"); // Rosso per la risposta sbagliata
                optionItem.style.backgroundColor = "#f8d7da"; // Rosso chiaro
            } else if (item.correct_answer === key && item.source == null) {
                optionItem.style.backgroundColor = "#6c757d"; // Verde chiaro per evidenziare la risposta giusta

            }else if (item.correct_answer === key) {
                // Caso risposta corretta non data
                optionItem.classList.add("correct-answer"); // Verde per la risposta corretta
                optionItem.style.backgroundColor = "#c3e6cb"; // Verde chiaro per evidenziare la risposta giusta

            }
            optionsList.appendChild(optionItem);
        }

        // Creazione della sezione dei risultati
        const resultDiv = document.createElement('div');
        resultDiv.classList.add('result');
        const correctAnswer = document.createElement('span');
        correctAnswer.classList.add('correct-answer');
        correctAnswer.textContent = `Risposta Corretta: ${item.correct_answer})`;

        // Visualizzazione della risposta data
        const givenAnswer = document.createElement('span');
        givenAnswer.classList.add(item.source ? 'given-answer' : 'no-answer');
        givenAnswer.textContent = `Risposta Data: ${item.source ? item.source + ')' : 'Nessuna risposta fornita'}`;

        // Aggiunta delle risposte corrette e date alla sezione dei risultati
        resultDiv.appendChild(correctAnswer);
        resultDiv.appendChild(document.createElement('br'));
        resultDiv.appendChild(givenAnswer);

        // Aggiunta dei componenti al contenitore della domanda
        questionContainer.appendChild(questionText);
        questionContainer.appendChild(optionsList);
        questionContainer.appendChild(resultDiv);

        // Aggiunta della domanda al contenitore principale
        container.appendChild(questionContainer);
    });
}

export function file_upload(){
	var form = $("#formFileLg")[0].files[0];
	var data = new FormData();
            data.append("uploadfile", form);
              $.ajax({
                url: "/uploadFileLesson/"+document.getElementById('lesson_id').getAttribute('value'),
                type: "POST",
                data: data,
                DataType: 'json',
                processData: false,
                contentType: false,
                cache: false,
                success: function(res) {
                   toastr.info("File uploaded succesfully");
                   file_add(res);
                },
                error: function(err) {
                    console.error(err);
                    toastr.error("Errore durante l'upload dell'immagine");
                }
            });
            
         function file_add(res){
			document.getElementById("all_files").innerHTML+='<div class="tile form">  <i class="mdi mdi-file-document"></i> <h3><a href="/file/'+res.id+'">'+ res.name +'</a> </h3></div>'
}
}


export function play() {
	
	
   $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/play",
        data:  JSON.stringify({
			id: document.getElementById('lesson_id').getAttribute('value') ,
		    name: document.getElementById('lesson_name').getAttribute('value')  // Assicurati di inviare anche il nome
        }),
			
       success: function(data) {
			console.log("SUCCESS : ", data);
            document.getElementById('statuss').className="badge bg-success";
            document.getElementById('statuss').innerText="Running";
           

        },
        error: function(e) {
            alert("Error!")
            console.log("ERROR: ", e);
        }
    });
}

export function download() {
	
   $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/download",
        data:  JSON.stringify({id: document.getElementById('lesson_id').getAttribute('value')}) ,
       success: function(data) {
			console.log("SUCCESS : ", data);
        },
        error: function(e) {
            alert("Error!")
            console.log("ERROR: ", e);
        }
    });
}

export function pause() {
    $.ajax({

        type: "POST",
        contentType: "application/json",
        url: "/pause",
        data:  JSON.stringify({id: document.getElementById('lesson_id').getAttribute('value')}) ,
       success: function(data) {
            console.log("SUCCESS : ", data);
           document.getElementById('statuss').className="badge bg-warning text-dark";
           document.getElementById('statuss').innerText="Paused";
        },
        error: function(e) {
            alert("Error!")
            console.log("ERROR: ", e);
        }
    });

}

export function stop() {
    $.ajax({

        type: "POST",
        contentType: "application/json",
        url: "/stop",
        data:  JSON.stringify({id: document.getElementById('lesson_id').getAttribute('value')}) ,
       success: function(data) {
            console.log("SUCCESS : ", data);
           document.getElementById('statuss').className="badge bg-danger";
           document.getElementById('statuss').innerText="Stopped";
        },
        error: function(e) {
            alert("Error!")
            console.log("ERROR: ", e);
        }
    });

}
export function lesson_id(){
	var id=document.getElementById("lesson_id");
	if(id==null){
		return null;
	}
	return id.getAttribute("value");
}
export function horizon(horizon){
	max=horizon;
}

//export function loading(v){

  // var progressBar = document.getElementById("progressbar");
    
    
    // Aggiorna la larghezza della barra
    //progressBar.style.width = 5 + "%";

    // Aggiorna il valore di `aria-valuenow` per l'accessibilità
  //  progressBar.setAttribute("aria-valuenow", 5);

    // Aggiorna il testo visualizzato all'interno della progress bar
    //progressBar.innerText = 5 + "%";
           
//	console.log("v: "+v)
//	value = parseFloat(((v*100)/max).toFixed(1)); 
//	if(v=0){value=0;}
//		console.log(value);
//		document.getElementById("progressbar").style.width= value +"%";
//		document.getElementById("progressbar").innerText=value+"%";
//}










export function new_stimuli(src){
	document.getElementById("textarea").style.display="none"
	document.getElementById("iframe").style.display="block"
	document.getElementById("iframe").setAttribute("src",src);
	
	
}
export function new_stimuli_icon(id){
    const lis = document.querySelectorAll('.check-list li');
    for (let i = 0; i <= lis.length - 1; i++) {
        lis[i].className="";
    }
    console.log(document.querySelectorAll('.check-list li'))
    document.getElementById("b"+id).className="obiettivi_list"

}

export function new_stimulitext(text){
	document.getElementById("textarea").style.display="block"
		document.getElementById("iframe").style.display="none"
	document.getElementById("textarea").value = text;
}



