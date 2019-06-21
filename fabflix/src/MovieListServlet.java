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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

/**
 * Servlet implementation class MovieServlet
 */
@WebServlet(name = "MovieListServlet", urlPatterns = "/movies")
public class MovieListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private DataSource dataSource;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long tsStart = System.nanoTime();

		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			if (envCtx == null)
				out.println("envCtx is NULL");
			String pool = (Math.random() >= 0.5 ? "jdbc/master" : "jdbc/slave");
			dataSource = (DataSource) envCtx.lookup(pool);
			Connection dbcon = dataSource.getConnection();
			String query = "select a.id as id, title, year, director, genre, rating, group_concat(s.id separator ', ') as star_id, "
					+ "group_concat(name separator ', ') as star "
					+ "from (select m.id, title, year, director, group_concat(name separator ', ') as genre, rating "
					+ "from movies as m left outer join genres_in_movies as gm on m.id = gm.movieId "
					+ "left outer join genres as g on g.id = gm.genreId join ratings as r on m.id = r.movieId group by id) "
					+ "as a join stars_in_movies as sm on a.id = sm.movieId join stars as s on sm.starId = s.id";
			String view = request.getParameter("view");
			String title = request.getParameter("title");
			String year = request.getParameter("year");
			String director = request.getParameter("director");
			String star = request.getParameter("star");
			String genre = request.getParameter("genre");
			String sortType = request.getParameter("sort");
			String page = request.getParameter("page");
			String limit = request.getParameter("limit");
			if (view.equals("search")) {
				if (title != null) {
					String[] keywords = title.split(" ");
					String fullText = "from (select * from movies where match (title) against (? in boolean mode)";
					String fuzzySearch = "";
					for (int i = 0; i < keywords.length; ++i)
					{
						int threshold = keywords[i].length() / 4;
						fuzzySearch += (fuzzySearch.equals("") ? " or edrec(" : " and edrec(") + "?, title, " + threshold + ")"; 
					}
					query = query.replace("from movies as m",fullText + fuzzySearch + ") as m");
				}
				if (year != null) {
					query += " where year = ?";
				}
				if (director != null) {
					if (year != null)
						query += " and ";
					else
						query += " where ";
					query += "director like ?";
				}
			}
			else {
				if (title != null)
					query += " where title like ?";
			}
			query += " group by a.id";
			if (view.equals("search")) {
				if (star != null)
					query += " having star like ?";
			}
			else {
				if (genre != null)
					query += " having genre like ?";
			}
			if (sortType.equals("rd") || sortType == null)
				query += " order by rating DESC";
			else if (sortType.equals("ra"))
				query += " order by rating ASC";
			else if (sortType.equals("td"))
				query += " order by title DESC";
			else if (sortType.equals("ta"))
				query += " order by title ASC";
			if (limit == null) {
				if (page == null || page.equals("1"))
					query += " limit 20;";
				else
					query += " limit 20 offset ?;";
			}
			else {
				if (page == null || page.equals("1"))
					query += " limit ?;";
				else
					query += " limit ? offset ?;";
			}
			PreparedStatement statement = dbcon.prepareStatement(query);
			int index = 0;
			if (view.equals("search")) {
				if (title != null) {
					String[] keywords = title.split(" ");
					String replace = "";
					for (int i = 0; i < keywords.length; ++i)
					{
						replace += "+" + keywords[i] + "* ";
					}
					statement.setString(++index,replace.trim());
					for (int i = 0; i < keywords.length; ++i)
					{
						statement.setString(++index,keywords[i]);
					}
				}
				if (year != null) {
					statement.setString(++index,year);
				}
				if (director != null) {
					statement.setString(++index,"%" + director + "%");
				}
			}
			else {
				if (title != null)
					statement.setString(++index,title + "%");
			}
			if (view.equals("search")) {
				if (star != null)
					statement.setString(++index,"%" + star + "%");
			}
			else {
				if (genre != null)
					statement.setString(++index,"%" + genre + "%");
			}
			if (limit == null) {
				if (page == null || page.equals("1")) {}
				else
					statement.setInt(++index,Integer.valueOf(page) * 20 - 20);
			}
			else {
				if (page == null || page.equals("1"))
					statement.setInt(++index,Integer.parseInt(limit));
				else
				{
					statement.setInt(++index,Integer.parseInt(limit));
					statement.setInt(++index,(Integer.valueOf(page) - 1) * Integer.parseInt(limit));
				}
			}

			long tjStart = System.nanoTime();
			ResultSet rs = statement.executeQuery();
			long tj = System.nanoTime() - tjStart;

			FileWriter fw = new FileWriter(new File("TJ"), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(Long.toString(tj) + "\n");
			bw.close();
			fw.close();

			JsonArray ja = new JsonArray();
			while (rs.next()) {
				String movie_id = rs.getString("id");
				String movie_title = rs.getString("title");
				String movie_year = rs.getString("year");
				String movie_director = rs.getString("director");
				String movie_genres = rs.getString("genre");
				String movie_rating = rs.getString("rating");
				String star_ids = rs.getString("star_id");
				String movie_stars = rs.getString("star");
				
				JsonObject jo = new JsonObject();
				jo.addProperty("movie_id", movie_id);
				jo.addProperty("movie_title", movie_title);
				jo.addProperty("movie_year", movie_year);
				jo.addProperty("movie_director", movie_director);
				jo.addProperty("movie_genres", movie_genres);
				jo.addProperty("movie_rating", movie_rating);
				jo.addProperty("star_ids",star_ids);
				jo.addProperty("movie_stars", movie_stars);
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

		long ts = System.nanoTime() - tsStart;
		FileWriter fw = new FileWriter(new File("TS"), true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(Long.toString(ts) + "\n");
		bw.close();
		fw.close();
	}
}
