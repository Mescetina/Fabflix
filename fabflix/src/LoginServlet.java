import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.jasypt.util.password.StrongPasswordEncryptor;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private DataSource dataSource;

	private void sendError(PrintWriter out) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("status","failure");
		jsonObject.addProperty("message","Username or password is incorrect.");
		out.write(jsonObject.toString());
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String userAgent = request.getHeader("User-Agent");
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");

		if (userAgent != null && !userAgent.contains("Android"))
		{
			String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
			try {
				RecaptchaVerifyUtils.verify(gRecaptchaResponse);
			} catch (Exception e) {
				if (!e.getMessage().contains("[\"timeout-or-duplicate\"]"))
				{
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("status","failure");
					jsonObject.addProperty("message",e.getMessage());
					out.write(jsonObject.toString());
					response.setStatus(200);
					out.close();
					return;
				}
			}
		}

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		try
		{
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			if (envCtx == null)
				out.println("envCtx is NULL");
			String pool = (Math.random() >= 0.5 ? "jdbc/master" : "jdbc/slave");
			dataSource = (DataSource) envCtx.lookup(pool);
			Connection connection = dataSource.getConnection();
			String query = String.format("SELECT * FROM customers where email = ?;");
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1,username);
			ResultSet results = statement.executeQuery();
			if (results.next())
			{
				String encryptedPassword = results.getString("password");
				if (new StrongPasswordEncryptor().checkPassword(password, encryptedPassword))
				{
					int userId = results.getInt("id");
					User user = new User(userId);
					request.getSession().setAttribute("user",user);
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("status","success");
					jsonObject.addProperty("message","success");
					out.write(jsonObject.toString());
				}
				else
				{
					sendError(out);
				}
			}
			else
			{
				sendError(out);
			}
			response.setStatus(200);
			results.close();
			statement.close();
			connection.close();
		}
		catch (Exception e)
		{
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("error_message",e.getMessage());
			out.write(jsonObject.toString());
			response.setStatus(500);
		}
		out.close();
	}
}
