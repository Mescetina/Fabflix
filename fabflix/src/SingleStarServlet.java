import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;


@WebServlet(name = "SingleStarServlet", urlPatterns = "/single-star")
public class SingleStarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private DataSource dataSource;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		String id = request.getParameter("id");
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			if (envCtx == null)
				out.println("envCtx is NULL");
			String pool = (Math.random() >= 0.5 ? "jdbc/master" : "jdbc/slave");
			dataSource = (DataSource) envCtx.lookup(pool);
			Connection dbcon = dataSource.getConnection();
			String query = "select s.id as id, s.name as name, birthYear, m.id as movieId, title from"
					+ " stars as s join stars_in_movies as sm on s.id = sm.starId"
					+ " join movies as m on sm.movieId = m.id"
					+ " where s.id = ?;";
			PreparedStatement statement = dbcon.prepareStatement(query);
			statement.setString(1,id);
			ResultSet rs = statement.executeQuery();
			JsonArray ja = new JsonArray();
			while(rs.next()) {
				String star_id = rs.getString("id");
				String star_name = rs.getString("name");
				String star_birthYear = rs.getString("birthYear");
				String movie_id = rs.getString("movieId");
				String movie_title = rs.getString("title");
				
				JsonObject jo = new JsonObject();
				jo.addProperty("star_id", star_id);
				jo.addProperty("star_name", star_name);
				jo.addProperty("star_birthYear", star_birthYear);
				jo.addProperty("movie_id", movie_id);
				jo.addProperty("movie_title", movie_title);
				
				ja.add(jo);
			}
			out.write(ja.toString());
			response.setStatus(200);
			rs.close();
			statement.close();
			dbcon.close();
		} catch (Exception e) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
			response.setStatus(500);
		}
		out.close();
	}
}
