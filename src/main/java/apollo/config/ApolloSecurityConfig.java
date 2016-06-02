package apollo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.csrf.CsrfFilter;

import accelerate.util.AngularJSUtil;
import accelerate.util.SecurityUtil;
import apollo.security.ApolloUserDetailsService;

/**
 * Main {@link Configuration} class for accelerate
 *
 * @author Rohit Narayanan
 * @version 1.0 Initial Version
 * @since Jul 20, 2014
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class ApolloSecurityConfig extends WebSecurityConfigurerAdapter {

	/**
	 * 
	 */
	@Autowired
	private ApolloUserDetailsService userDetailsService = null;

	/**
	 * @return
	 */
	@Bean
	public static SessionRegistry sessionRegistry() {
		return SecurityUtil.SESSION_REGISTRY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.config.annotation.web.configuration.
	 * WebSecurityConfigurerAdapter#configure(org.springframework.security.
	 * config.annotation.web.builders.WebSecurity)
	 */
	/**
	 * @param aWebSecurity
	 * @throws Exception
	 */
	@Override
	public void configure(WebSecurity aWebSecurity) throws Exception {
		aWebSecurity.ignoring().antMatchers("/webjars/**", "/css/**", "/js/**", "/img/**", "/", "/index",
				"/errorPage/**");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.config.annotation.web.configuration.
	 * WebSecurityConfigurerAdapter#configure(org.springframework.security.
	 * config.annotation.web.builders.HttpSecurity)
	 */
	/**
	 * @param aHttp
	 * @throws Exception
	 */
	@Override
	protected void configure(HttpSecurity aHttp) throws Exception {
		// configure login logout
		aHttp.formLogin().defaultSuccessUrl("/home", true)
				.failureHandler(SecurityUtil.authenticationFailureHandler("/login")).permitAll();

		// configure logout
		aHttp.logout().clearAuthentication(true).deleteCookies("JSESSIONID").invalidateHttpSession(true)
				.logoutSuccessUrl("/login?logout=Y").permitAll();

		// session management
		aHttp.sessionManagement().maximumSessions(2).expiredUrl("/login?sessionExpired=Y")
				.sessionRegistry(SecurityUtil.SESSION_REGISTRY);

		// csrf protection with angular compatibility
		aHttp.csrf().csrfTokenRepository(AngularJSUtil.csrfTokenRepository()).and()
				.addFilterAfter(AngularJSUtil.csrfHeaderFilter(), CsrfFilter.class);

		// URL security
		aHttp.authorizeRequests().antMatchers("/login/**", "/logout/**").permitAll().anyRequest().fullyAuthenticated();

		// exception handling on authentication or authorization errors
		aHttp.exceptionHandling()
				.authenticationEntryPoint(SecurityUtil.configureAuthenticationEntryPoint("/login", true))
				.accessDeniedHandler(SecurityUtil.configureAccessDeniedHandler("/login", "/logout", true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.config.annotation.web.configuration.
	 * WebSecurityConfigurerAdapter#configure(org.springframework.security.
	 * config.annotation.authentication.builders.AuthenticationManagerBuilder)
	 */
	/**
	 * @param aAuth
	 * @throws Exception
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder aAuth) throws Exception {
		aAuth.eraseCredentials(true).userDetailsService(this.userDetailsService)
				.passwordEncoder(NoOpPasswordEncoder.getInstance());
	}
}