function getParameterByName(target){
	let url = window.location.href;
	target = target.replace(/[\[\]]/g, "\\$&");
	
	let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
		results = regex.exec(url);
	if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function getCookieByName(name){
	var cookies = "; " + document.cookie;
	var parts = cookies.split("; "+ name + "=");
	if (parts.length == 2)
		var c = parts.pop().split(";").shift();
	else
		var c = parts.shift().split(";").shift();
	return c;
}

function setCart(movie_id, movie_title){
	var name = "cart";
	var title = "title";
	if (document.cookie.indexOf(name) == -1){
		document.cookie = name + "=" + movie_id;
		document.cookie = title + "=" + movie_title;
	}
	else {
		var c = getCookieByName(name);
		var t = getCookieByName(title);
		document.cookie = "cart=" + c + "." + movie_id;
		document.cookie = "title=" + t + "&&" + movie_title;
	}
	console.log(document.cookie);
	alert("Movie added to cart");
}

function handleResult(resultData){
	let mi = jQuery("#movie_info");
	mi.append('<h5 class="text-muted"> Movie Title: ' + resultData[0]["movie_title"] + "</h5>" +
			   '<h5 class="text-muted"> Year: ' + resultData[0]["movie_year"] + "</h5>" +
			   '<h5 class="text-muted"> Director: ' + resultData[0]["movie_director"] + "</h5>" +
			   '<h5 class="text-muted"> Genres: ' + resultData[0]["movie_genre"] + "</h5>" + 
			   '<h5 class="text-muted"> Rating: ' + resultData[0]["movie_rating"] + "</h5>");
	let stb = jQuery("#star_table_body");
	document.getElementById("addBtn").onclick=function(){setCart(resultData[0]["movie_id"], resultData[0]["movie_title"])};
	for (let i = 0; i < resultData.length; i++) {
		let rowHTML = "";
		rowHTML += "<tr>" + "<th>" +
				   '<a href = "single-star.html?id=' + 
				   resultData[i]["movie_starId"] + '">' +
				   resultData[i]["movie_star"] + "</th>" + "</tr>";
		stb.append(rowHTML);
	}
}

document.getElementById("goBackBtn").href=getCookieByName("lastURL");
let movieId = getParameterByName('id');
jQuery.ajax({
	dataType: "json",
	method: "GET",
	url: "single-movie?id=" + movieId,
	success: (resultData) => handleResult(resultData)
});
