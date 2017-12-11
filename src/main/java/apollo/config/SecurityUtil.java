package apollo.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.web.util.UrlPathHelper;

import accelerate.web.Session;
import accelerate.web.WebUtil;

/**
 * Utility class with helper methods to handle IO operations
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Mar 10, 2016
 */
public final class SecurityUtil {
	/**
	 * {@link Logger} instance
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtil.class);

	/**
	 * static instance
	 */
	static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

	/**
	 * 
	 */
	public static final RedirectStrategy REDIRECT_STRATEGY = new DefaultRedirectStrategy();

	/**
	 * 
	 */
	public static final SessionRegistry SESSION_REGISTRY = new SessionRegistryImpl();

	/**
	 * hidden constructor
	 */
	private SecurityUtil() {
	}

	/**
	 * @return
	 */
	public static final Session getUserSession() {
		return (Session) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	/**
	 * @param aURLPrefix
	 * @param aAuthenticationException
	 * @return
	 */
	public static final String getAuthErrorParam(String aURLPrefix, AuthenticationException aAuthenticationException) {
		StringBuilder errorURL = new StringBuilder(aURLPrefix);

		if (aAuthenticationException instanceof InsufficientAuthenticationException) {
			errorURL.append("?notLoggedIn=Y");
		} else if (aAuthenticationException instanceof BadCredentialsException) {
			errorURL.append("?errorType=incorrectLogin");
		} else if (aAuthenticationException instanceof DisabledException) {
			errorURL.append("?errorType=userDisabled");
		} else if (aAuthenticationException instanceof AccountExpiredException) {
			errorURL.append("?errorType=userAccountExpired");
		} else if (aAuthenticationException instanceof CredentialsExpiredException) {
			errorURL.append("?errorType=userCredentialsAccount");
		} else if (aAuthenticationException instanceof LockedException) {
			errorURL.append("?errorType=userAccountLocked");
		} else {
			errorURL.append("?errorType=other");
		}

		return errorURL.toString();
	}

	/**
	 * @param aAuthErrorURLPrefix
	 * @param aAnglularJSFlag
	 * @return
	 */
	public static final AuthenticationEntryPoint configureAuthenticationEntryPoint(final String aAuthErrorURLPrefix,
			final boolean aAnglularJSFlag) {
		return new AuthenticationEntryPoint() {
			@Override
			public void commence(HttpServletRequest aRequest, HttpServletResponse aResponse,
					AuthenticationException aAuthException) throws IOException {
				LOGGER.info("AuthError:{}#@#URL:{}", aAuthException.getClass().getName(), aRequest.getRequestURL());
				if (aAnglularJSFlag && AngularJSUtil.handleAuthEntry(aRequest, aResponse, aAuthException)) {
					return;
				}

				REDIRECT_STRATEGY.sendRedirect(aRequest, aResponse,
						getAuthErrorParam(aAuthErrorURLPrefix, aAuthException));
			}
		};
	}

	/**
	 * @param aAuthErrorURLPrefix
	 * @return
	 */
	public static final AuthenticationFailureHandler authenticationFailureHandler(final String aAuthErrorURLPrefix) {
		return new SimpleUrlAuthenticationFailureHandler() {
			@Override
			public void onAuthenticationFailure(HttpServletRequest aRequest, HttpServletResponse aResponse,
					AuthenticationException aAuthException) throws IOException, ServletException {
				setDefaultFailureUrl(getAuthErrorParam(aAuthErrorURLPrefix, aAuthException));
				super.onAuthenticationFailure(aRequest, aResponse, aAuthException);
			}
		};
	}

	/**
	 * @param aLoginURL
	 * @param aLogoutURL
	 * @param aAnglularJSFlag
	 * @return
	 */
	public static final AccessDeniedHandler configureAccessDeniedHandler(final String aLoginURL,
			final String aLogoutURL, final boolean aAnglularJSFlag) {
		return new AccessDeniedHandlerImpl() {
			@Override
			public void handle(HttpServletRequest aRequest, HttpServletResponse aResponse,
					AccessDeniedException aAccessDeniedException) throws IOException, ServletException {
				if (aAccessDeniedException instanceof CsrfException) {
					if (StringUtils.startsWithAny(URL_PATH_HELPER.getPathWithinApplication(aRequest), aLoginURL,
							aLogoutURL)) {
						LOGGER.info("Ignoring Login/Logout failure due to invalid CSRF token, "
								+ "User will be logged out and sent to index(/) page");
						WebUtil.logout(aRequest, aResponse);
						REDIRECT_STRATEGY.sendRedirect(aRequest, aResponse, "/");
						return;
					}
				}

				if (aAnglularJSFlag && AngularJSUtil.handleAccessDenied(aRequest, aResponse, aAccessDeniedException)) {
					return;
				}

				super.handle(aRequest, aResponse, aAccessDeniedException);
			}
		};
	}
}
