/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package apollo.security;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 9, 2016
 */
public class SimpleSignInAdapter implements SignInAdapter {
	/**
	 * 
	 */
	private final RequestCache requestCache;

	/**
	 * @param aRequestCache
	 */
	@Inject
	public SimpleSignInAdapter(RequestCache aRequestCache) {
		this.requestCache = aRequestCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.social.connect.web.SignInAdapter#signIn(java.lang.
	 * String, org.springframework.social.connect.Connection,
	 * org.springframework.web.context.request.NativeWebRequest)
	 */
	/**
	 * @param localUserId
	 * @param connection
	 * @param request
	 * @return
	 */
	@Override
	public String signIn(String localUserId, Connection<?> connection, NativeWebRequest request) {
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(localUserId, null, null));
		return extractOriginalUrl(request);
	}

	/**
	 * @param request
	 * @return
	 */
	private String extractOriginalUrl(NativeWebRequest request) {
		HttpServletRequest nativeReq = request.getNativeRequest(HttpServletRequest.class);
		HttpServletResponse nativeRes = request.getNativeResponse(HttpServletResponse.class);
		SavedRequest saved = this.requestCache.getRequest(nativeReq, nativeRes);
		if (saved == null) {
			return null;
		}

		this.requestCache.removeRequest(nativeReq, nativeRes);
		removeAutheticationAttributes(nativeReq.getSession(false));
		return saved.getRedirectUrl();
	}

	/**
	 * @param session
	 */
	private static void removeAutheticationAttributes(HttpSession session) {
		if (session == null) {
			return;
		}

		session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	}
}
