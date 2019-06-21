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

function setButtons(){
	var current_url = window.location.toString();
	var si = current_url.indexOf("&sort");
	var pi = current_url.indexOf("&page");
	var noPage = current_url.substring(0,pi);
	var title_url = current_url.substring(0, si);
	var rat_url = current_url.substring(0, si);
	var sortType = current_url.substring(si+7, si+9);
	var page = getParameterByName("page");
	var nextPage = (parseInt(page)+1).toString();
	var prevPage = (parseInt(page)-1).toString();
	var nextUrl = window.location.toString().replace("&page="+page, "&page="+nextPage);
	var prevUrl = window.location.toString().replace("&page="+page, "&page="+prevPage);
	var ten_url = noPage + "&page=1&limit=10";
	var twt_url = noPage + "&page=1";
	var fft_url = noPage + "&page=1&limit=50";
	var hdd_url = noPage + "&page=1&limit=100";
	if (current_url.includes("&sort=ra")){
		document.getElementById("ratingSort").innerHTML="Rating↑";
		title_url += "&sort=ta";
		rat_url += "&sort=rd";
	}
	else if (current_url.includes("&sort=rd")){
		document.getElementById("ratingSort").innerHTML="Rating↓";
		title_url += "&sort=ta";
		rat_url += "&sort=ra";
	}
	else if (current_url.includes("&sort=ta")){
		document.getElementById("titleSort").innerHTML="Title↑";
		title_url += "&sort=td";
		rat_url += "&sort=rd";
	}
	else if (current_url.includes("&sort=td")){
		document.getElementById("titleSort").innerHTML="Title↓";
		title_url += "&sort=ta";
		rat_url += "&sort=rd";
	}
	rat_url += "&page=1";
	title_url += "&page=1";
	document.getElementById("titleSort").href = title_url;
	document.getElementById("ratingSort").href = rat_url;
	document.getElementById("tenBtn").href = ten_url;
	document.getElementById("twtBtn").href = twt_url;
	document.getElementById("fftBtn").href = fft_url;
	document.getElementById("hddBtn").href = hdd_url;
	document.getElementById("prevBtn").href = prevUrl;
	document.getElementById("nextBtn").href = nextUrl;
	if (parseInt(page) == 1)
		document.getElementById("prevBtn").style.display="none"; 
}

function handleMovieResult(resultData) {
	let movieTableBodyElement = jQuery("#movie_table_body");
	var i = 0;
    for (i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + 
        		   '<a href="single-movie.html?id=' +
        		   resultData[i]["movie_id"] + '">' +
        		   resultData[i]["movie_title"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        
        var genres = resultData[i]["movie_genres"].split(", ");
        var star_ids = resultData[i]["star_ids"].split(", ");
        var stars = resultData[i]["movie_stars"].split(", ");
        rowHTML += "<th>";
        for (let k = 0; k < genres.length; k++) {
        	rowHTML += '<a href="movie-list.html?view=browse&genre=' +
    					genres[k] + '&sort=rd&page=1">' +
    					genres[k];
            if (k != (genres.length - 1)) {
            	rowHTML += ", ";
            }
        }
        rowHTML += "</th>";
        rowHTML += "<th>";
        for (let j = 0; j < star_ids.length; j++) {
        	rowHTML += '<a href="single-star.html?id=' +
						star_ids[j] + '">' +
						stars[j];
        	if (j != (star_ids.length - 1)) {
        		rowHTML += ", ";
        	}
        }
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>"
        rowHTML += '<th>';
        rowHTML += '<input type="button" onClick="setCart(\'' + resultData[i]["movie_id"] + '\',\'' + resultData[i]["movie_title"] + '\')" VALUE="Add">';
        rowHTML += '</th>';
        rowHTML += "</tr>";
        
        movieTableBodyElement.append(rowHTML);
    }
    var limit = getParameterByName("limit");
    if (limit != null){
    	if (i < parseInt(limit) - 1){
    		document.getElementById("nextBtn").style.display="none"; 
    	}
    }
    else if (i < 19){
    	document.getElementById("nextBtn").style.display="none"; 
    }
}

let movie_url = window.location.toString().replace("movie-list.html", "movies")
setButtons();
document.cookie="lastURL=" + window.location.href;
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: movie_url,
    success: (resultData) => handleMovieResult(resultData)});
