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


@WebServlet(name = "SingleMovieServlet", urlPatterns = "/single-movie")
public class SingleMovieServlet extends HttpServlet {
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
			String query = "select * from (select a.id as id, title, year, director, genre, rating, name as star, s.id as starId"
					+ " from "
					+ "(select m.id, title, year, director, group_concat(name separator ', ') as genre, rating"
					+ " from movies as m left outer join genres_in_movies as gm on m.id = gm.movieId"
					+ " left outer join genres as g on g.id = gm.genreId"
					+ " join ratings as r on m.id = r.movieId"
					+ " group by id) as a"
					+ " join stars_in_movies as sm on a.id = sm.movieId"
					+ " join stars as s on sm.starId = s.id"
					+ " ) as a"
					+ " where id = ?;";
			PreparedStatement statement = dbcon.prepareStatement(query);
			statement.setString(1,id);
			ResultSet rs = statement.executeQuery();
			JsonArray ja = new JsonArray();
			while(rs.next()) {
				String movie_id = rs.getString("id");
				String movie_title = rs.getString("title");
				String movie_year = rs.getString("year");
				String movie_director = rs.getString("director");
				String movie_genre = rs.getString("genre");
				String movie_rating = rs.getString("rating");
				String movie_star = rs.getString("star");
				String movie_starId = rs.getString("starId");
				
				JsonObject jo = new JsonObject();
				jo.addProperty("movie_id", movie_id);
				jo.addProperty("movie_title", movie_title);
				jo.addProperty("movie_year", movie_year);
				jo.addProperty("movie_director", movie_director);
				jo.addProperty("movie_rating", movie_rating);
				jo.addProperty("movie_genre", movie_genre);
				jo.addProperty("movie_star", movie_star);
				jo.addProperty("movie_starId", movie_starId);
				
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
