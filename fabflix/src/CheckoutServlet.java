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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * Servlet implementation class checkout
 */
@WebServlet(name = "checkout", urlPatterns = "/api/checkout")
public class CheckoutServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private DataSource dataSource;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		HttpSession session = request.getSession();
		int userId = ((User) session.getAttribute("user")).getUserId();
		String saleDate = java.time.LocalDate.now().toString();
		Cookie[] cookies = request.getCookies();
		String movie = "";
		for (int i = 0; i < cookies.length; ++i)
		{
			if (cookies[i].getName().equals("cart"))
			{
				movie = cookies[i].getValue();
			}
		}

		response.setContentType("application/json");
		String cardId = request.getParameter("cid");
		String firstName = request.getParameter("first");
		String lastName = request.getParameter("last");
		String expiration = request.getParameter("expire");
		PrintWriter out = response.getWriter();
		try
		{
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			if (envCtx == null)
				out.println("envCtx is NULL");
			dataSource = (DataSource) envCtx.lookup("jdbc/master");
			Connection connection = dataSource.getConnection();
			String query = "SELECT COUNT(*) AS num "
				     + "FROM creditcards c "
				     + "WHERE c.id = ? AND c.firstName = ? AND c.lastName = ? "
				     + "      AND c.expiration = ?;";
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setString(1,cardId);
			statement.setString(2,firstName);
			statement.setString(3,lastName);
			statement.setString(4,expiration);
			ResultSet results = statement.executeQuery();
			results.next();
			if (results.getInt("num") > 0 && movie != null)
			{
				String[] movies = movie.split("[.]");
				String saleIds = "";
				String movieTitles = "";
				for (int i = 0; i < movies.length; ++i)
				{
					String insert = "INSERT INTO sales(customerId, movieId, saleDate) "
						      + "VALUES (" + userId + ", '" + movies[i] + "', '" + saleDate +"');";
					statement.executeUpdate(insert);
					results = statement.executeQuery("SELECT LAST_INSERT_ID() AS sid;");
					results.next();
					int saleId = results.getInt("sid");
					saleIds += saleId;
					if (i != movies.length - 1)
					{
						saleIds += "~";
					}
					String saleQuery = "SELECT m.title "
							 + "FROM sales s, movies m "
							 + "WHERE s.id = " + saleId + " AND s.movieId = m.id;";
					results = statement.executeQuery(saleQuery);
					results.next();
					movieTitles += results.getString("title");
					if (i != movies.length - 1)
					{
						movieTitles += "~";
					}
				}
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("status","success");
				jsonObject.addProperty("message","success");
				jsonObject.addProperty("sale_ids",saleIds);
				jsonObject.addProperty("movie_titles",movieTitles);
				out.write(jsonObject.toString());
			}
			else
			{
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("status","failure");
				if (movie == null)
				{
					jsonObject.addProperty("message","Shopping cart is empty.");
				}
				else
				{
					jsonObject.addProperty("message","Credit card info is incorrect.");
				}
				out.write(jsonObject.toString());
			}
			response.setStatus(200);
			results.close();
			statement.close();
			connection.close();
		}
		catch (Exception e)
		{
			System.out.println(e);
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("error_message",e.getMessage());
			out.write(jsonObject.toString());
			response.setStatus(500);
		}
	}
}
