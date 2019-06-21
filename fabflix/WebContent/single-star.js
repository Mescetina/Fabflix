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

function handleResult(resultData){
	let si = jQuery("#star_info");
	if (resultData[0]["star_birthYear"] != null) {
		si.append('<h5 class="text-muted"> Star Name: ' + resultData[0]["star_name"] + "</h5>" +
                          '<h5 class="text-muted"> Birth Year: ' + resultData[0]["star_birthYear"] + "</h5>");
	}
	else {
		si.append('<h5 class="text-muted"> Star Name: ' + resultData[0]["star_name"] + "</h5>" +
                          '<h5 class="text-muted"> Birth Year: unknown</h5>');
	}
	let mtb = jQuery("#movie_table_body");
	for (let i = 0; i < resultData.length; i++) {
		let rowHTML = "";
		rowHTML += "<tr>" + "<th>" +
				   '<a href = "single-movie.html?id=' + 
				   resultData[i]["movie_id"] + '">' +
				   resultData[i]["movie_title"] + "</th>" + "</tr>";
		mtb.append(rowHTML);
	}
}

document.getElementById("goBackBtn").href=getCookieByName("lastURL");
let starId = getParameterByName('id');
jQuery.ajax({
	dataType: "json",
	method: "GET",
	url: "single-star?id=" + starId,
	success: (resultData) => handleResult(resultData)
});
