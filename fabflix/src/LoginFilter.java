import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter
{
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException
	{
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		if (isUrlAllowedWithoutLogin(httpRequest.getRequestURI()) || httpRequest.getSession().getAttribute("user") != null)
		{
			if (httpRequest.getRequestURI().endsWith("project5/"))
			{
				String rootPath = httpRequest.getContextPath();
				httpResponse.sendRedirect(rootPath + "/index.html");
			}
			else
			{
				chain.doFilter(request,response);
			}
		}
		else
		{
			String rootPath = httpRequest.getContextPath();
			httpResponse.sendRedirect(rootPath + "/login.html");
		}
	}

	/**
	 * Allows access to login related requests without logged in.
	 * @param requestURI
	 * @return boolean
	 */
	private boolean isUrlAllowedWithoutLogin(String requestURI)
	{
		requestURI = requestURI.toLowerCase();
//		return requestURI.endsWith("login.html") || requestURI.endsWith("login.js") || requestURI.endsWith("login");
		return true;
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy()
	{
	}
}
