/**
 * Handles the data returned by LoginServlet.
 * @param resultData jsonObject
 */
function handleResult(resultData)
{
    if (resultData["status"] === "success")
    {
        window.location.replace("index.html");
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
    $.post("api/login",
           $("#login_form").serialize(),
           (resultData) => handleResult(resultData))
}

$("#login_form").submit((event) => submitForm(event));
