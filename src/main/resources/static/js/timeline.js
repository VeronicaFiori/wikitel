//import { json } from "d3-fetch";


let rows=[];
let name;
let user=null;
let users = [];
let one=true;


 	export function type(u){
	users.push(u)
	user = u
	console.log(user)
	
}
 
    export function timeline(array,u){

	
       google.charts.load("current", {packages:["timeline"]});
    google.charts.setOnLoadCallback(drawChart);
  
   
   
 function drawChart() {
		
	
	/*rows =[];
	
	array.forEach(function(s) {
       document.getElementById("lista_obiettivi").innerHTML+= '<li  id="b'+ s.arg+'"\>' +
            '<span className="d-inline-block" tabIndex="0" data-bs-toggle="popover" data-bs-trigger="hover focus" '+
                  'data-bs-content="Clicca per vissualizzare la pagina Wikipedia">' +

            '<button  style="background:none;border:none;" onclick="wikilink(\'' + s.arg + '\')"><strong >'+ s.arg +'</strong></button></span>\n' +
            '</li>';
rows.push(['rule', s.arg , new Date(0,0,0,0,0,s.from),new Date(0,0,0,0,0,s.to)  ])
}); */

// MODIFICA
rows = [];

array.forEach(function(s) {
	
	 

    var listItemContent = '';

    // Aggiungi il contenuto dell'elemento lista con popover

    listItemContent += '<li id="b' + s.arg + '">'+'<span className="d-inline-block" tabIndex="0" data-bs-toggle="popover" data-bs-trigger="hover focus" ' + 
     'data-bs-content="Clicca per visualizzare la pagina Wikipedia">';
     listItemContent += '<button style="background:none;border:none;" <strong>' + s.arg + '</strong></button>';

    listItemContent += '</span></li>';


    document.getElementById("lista_obiettivi").innerHTML += listItemContent;
;
rows.push(['rule', s.arg , new Date(0,0,0,0,0,s.from),new Date(0,0,0,0,0,s.to)  ])
});


if(u!=null){
var container = document.getElementById('timeline'+u);}
else{
    var container = document.getElementById('timeline');
}

      console.log(container)
      
      var chart = new google.visualization.Timeline(container);
      var dataTable = new google.visualization.DataTable();
      dataTable.addColumn({ type: 'string', id: 'Room' });
      dataTable.addColumn({ type: 'string', id: 'Name' });
      dataTable.addColumn({ type: 'date', id: 'Start' });
      dataTable.addColumn({ type: 'date', id: 'End' });
      dataTable.addRows(rows);

      var options = {
        timeline: { showRowLabels: false },
        avoidOverlappingGridLines: false, rowLabelStyle: { fontSize: 24, color: '#603913' },
                     barLabelStyle: {  fontSize: 14 } 
      };

      chart.draw(dataTable, options);
      
      
    }
   
	
	
	
	
	function getName(link){
		console.log(link.substring(3,link.length));
		 var model = {
        id: link.substring(3,link.length)
    }
		   $.ajax({
        type: "POST",
        contentType: "application/json",
        url: "/getname",
        data: JSON.stringify(model),
        dataType: "json",
         async: false,
        success: function(data) {
	console.log(data);
            name=data.status;
        },
        error: function(e) {
            alert("Error!")
            console.log("ERROR: ", e);
            name="error";
        }
    });
	}
	
	


}



  