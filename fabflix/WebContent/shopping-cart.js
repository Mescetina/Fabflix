function getCookieByName(name)
{
    var cookies = "; " + document.cookie;
    var parts = cookies.split("; "+ name + "=");
    if (parts.length == 2)
        var c = parts.pop().split(";").shift();
    else
        var c = parts.shift().split(";").shift();
    return c;
}

function deleteItem(i){
	document.getElementById("qty"+i).value = 0;
	document.getElementById("row"+i).style.display = "none";	
}

function confirm(movies, index){
	var cart = "cart=";
	var need = 0;
	var x = 0;
	for (var i = 0; i < index; i++){
		x = parseInt(document.getElementById("qty"+i).value);
		for (var j = 0; j < x; j++) {
			if (need == 0){
				cart += movies[i];
			}
			else {
				cart += "." + movies[i];
			}
			need = 1;
		}
	}
	if (need == 0){
		alert("Your cart is empty! Please Go Back!");
		document.cookie = "cart=; Max-Age=-1;";
		console.log(getCookieByName("cart"));
	}
	else{
		document.cookie = cart;
		window.location.href="checkout.html";
	}
}

let sce = jQuery("#cart_table_body");
let cart = getCookieByName("cart");
if (cart != ""){
	var ids = cart.split(".");
	var titles = getCookieByName("title").split("&&");
	var movies = new Array(ids.length);
	var qty = new Array(ids.length);
	var index = 0;
	for (var i = 0; i < ids.length; i++){
		if (movies.includes(ids[i])) {
			qty[movies.indexOf(ids[i])] += 1;
		}
		else {
			movies[index] = ids[i];
			qty[index++] = 1;
		}
	}

	for (var i = 0; i < index; i++){
		let rowh = "";
		rowh += "<tr id=\"row" + i.toString() + "\">";
		rowh += "<th>" + titles[i] + "</th>";
		rowh += "<th>" +
				'<input type="text"' + 
				' id="qty' + i.toString() + 
				'" VALUE="' + qty[i].toString() + '">' + 
				"</th>";
		rowh += '<th><input type="button" onClick="deleteItem(\'' + i.toString() + '\')" VALUE="delete"></th>';
		rowh += "</tr>";
		sce.append(rowh);
	}
}
document.getElementById("confirmBtn").onclick=function(){confirm(movies, index);};

