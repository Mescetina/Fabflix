/**
 * Handles the data returned by LoginServlet.
 * @param resultData jsonObject
 */
function handleResult(resultData)
{
    if (resultData["status"] === "success")
    {
    	var sale = "sale";
    	var tit = "tit";
    	document.cookie = "sale=" + resultData['sale_ids'];
    	document.cookie = "tit=" + resultData['movie_titles'];
    	window.location.replace("confirmation.html");
    }
    else
    {
        $("#login_error_message").text(resultData['message']);
    }
}

/**
 * Submits the form content with POST method.
 * @param formSubmitEvent
 */
function submitForm(formSubmitEvent)
{
    formSubmitEvent.preventDefault();
    $.post("api/checkout",
           $("#checkout_form").serialize(),
           (resultData) => handleResult(resultData))
}

$("#checkout_form").submit((event) => submitForm(event));
