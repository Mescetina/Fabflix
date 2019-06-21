/**
 * Submits the form content with GET method.
 * @param formSubmitEvent
 */
function submitSearchForm(formSubmitEvent)
{
    formSubmitEvent.preventDefault();
    let param = "";
    let params = $('#search_form').serialize().split("&");
    for (let i = 0; i < params.length; ++i)
    {
    	if (params[i].slice(-1) != "=")
    	{
    		param += "&" + params[i];
    	}
    }
    window.location.href = "movie-list.html?view=search" + param + "&sort=rd&page=1";
}

$("#search_form").submit((event) => submitSearchForm(event));


let pastResults = {};

function handleLookup(query, doneCallback)
{
	console.log("autocomplete initiated");
	if (query in pastResults)
	{
		console.log("using cached result");
		console.log(pastResults[query]);
		doneCallback({suggestions: pastResults[query]});
	}
	else
	{
		console.log("sending ajax request to server");
		jQuery.ajax({
			"method": "GET",
			"url": "movies?view=search&title=" + escape(query) + "&sort=rd&page=1",
			"success": function(data) {
				handleLookupAjaxSuccess(data,query,doneCallback);
			},
			"error": function(errorData) {
				console.log("lookup ajax error");
				console.log(errorData);
			}
		})
	}
}

function handleLookupAjaxSuccess(data, query, doneCallback)
{
	let resultData = [];
	for (let i = 0; i < (data.length > 10 ? 10 : data.length); ++i)
    {
		resultData[i] = {};
    	resultData[i].value = data[i]['movie_title'];
    	resultData[i].data = data[i]['movie_id'];
    }
	console.log(resultData);
	pastResults[query] = resultData;
	doneCallback({suggestions: resultData});
}

function handleSelectSuggestion(suggestion)
{
	window.location.href = "single-movie.html?id=" + suggestion['data'];
}

$('#autocomplete').autocomplete({
    lookup: function(query, doneCallback) {
    		handleLookup(query, doneCallback);
    },
    onSelect: function(suggestion) {
    		handleSelectSuggestion(suggestion);
    },
    deferRequestBy: 300,
    minChars: 3
});
