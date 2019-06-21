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

let salesElement = jQuery("#sales");

let sa = getCookieByName("sale").split("~").join(", ");

salesElement.append(sa);

let moviesElement = jQuery("#movies");

let movies = getCookieByName("tit").split("~");

let sm = movies.join(", ");

var dict = {};

for (let i = 0; i < movies.length; ++i)
{
	if (movies[i] in dict)
	{
		dict[movies[i]] += 1;
	}
	else
	{
		dict[movies[i]] = 1;
	}
}

let s = "";

for (var key in dict)
	{
		s += key + "(" + dict[key] + ")" + " ";
	}

moviesElement.append(s);
