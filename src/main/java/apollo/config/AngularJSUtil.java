package apollo.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import accelerate.utils.CommonConstants;
import accelerate.utils.CommonUtils;
import accelerate.utils.JSONUtil;

/**
 * Utility class with helper methods to handle IO operations
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Mar 10, 2016
 */
public final class AngularJSUtil {
	/**
	 * {@link Logger} instance
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AngularJSUtil.class);

	/**
	 * 
	 */
	private static final String ANGULAR_AJAX_HEADER = "AJAX-REQUEST";

	/**
	 * hidden constructor
	 */
	private AngularJSUtil() {
	}

	/**
	 * Filter to handle csrf token sent by Angular JS
	 * 
	 * @return
	 */
	public static Filter csrfHeaderFilter() {
		return new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest aRequest, HttpServletResponse aResponse,
					FilterChain aFilterChain) throws ServletException, IOException {

				CsrfToken csrf = (CsrfToken) aRequest.getAttribute(CsrfToken.class.getName());
				if (csrf != null) {
					Cookie cookie = WebUtils.getCookie(aRequest, "XSRF-TOKEN");
					String token = csrf.getToken();
					if ((cookie == null) || ((token != null) && !token.equals(cookie.getValue()))) {
						cookie = new Cookie("XSRF-TOKEN", token);
						cookie.setPath(aRequest.getContextPath());
						aResponse.addCookie(cookie);
					}
				}

				aFilterChain.doFilter(aRequest, aResponse);
			}
		};
	}

	/**
	 * Token repository configuration to identify csrf request header snet by
	 * Angular JS
	 * 
	 * @return
	 */
	public static CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName("X-XSRF-TOKEN");
		return repository;
	}

	/**
	 * @param aRequest
	 * @param aResponse
	 * @param aAuthException
	 * @return
	 * @throws IOException
	 */
	public static boolean handleAuthEntry(HttpServletRequest aRequest, HttpServletResponse aResponse,
			AuthenticationException aAuthException) throws IOException {
		if (CommonUtils.compare(aRequest.getHeader(ANGULAR_AJAX_HEADER), accelerate.utils.CommonConstants.TRUE)) {
			Map<String, Object> responseData = new HashMap<>();
			responseData.put("authenticated", "false");
			responseData.put("authError", aAuthException.getClass().getName());
			aResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			aResponse.getWriter().write(JSONUtil.serialize(responseData));
			return true;
		}

		return false;
	}

	/**
	 * @param aRequest
	 * @param aResponse
	 * @param aAccessDeniedException
	 * @return
	 * @throws IOException
	 */
	public static boolean handleAccessDenied(HttpServletRequest aRequest, HttpServletResponse aResponse,
			AccessDeniedException aAccessDeniedException) throws IOException {
		if (CommonUtils.compare(aRequest.getHeader(ANGULAR_AJAX_HEADER), CommonConstants.TRUE)) {
			LOGGER.info("Auth error[{}] for url[{}], method[{}]", aAccessDeniedException.getMessage(),
					aRequest.getRequestURL(), aRequest.getMethod());
			int errorCode = (aAccessDeniedException instanceof CsrfException) ? HttpServletResponse.SC_UNAUTHORIZED
					: HttpServletResponse.SC_FORBIDDEN;
			aResponse.sendError(errorCode, aAccessDeniedException.getMessage());
			return false;
		}

		return true;
	}
}
