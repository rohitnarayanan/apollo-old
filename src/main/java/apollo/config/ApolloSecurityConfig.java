package apollo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CsrfFilter;

import accelerate.util.AngularUtil;
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
		// configure login / logout
		aHttp.formLogin().loginPage("/login").defaultSuccessUrl("/home", true).permitAll().and().logout().permitAll()
				.invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID").and()
				.authorizeRequests().anyRequest().fullyAuthenticated();

		// csrf protection with angular compatibility
		aHttp.csrf().csrfTokenRepository(AngularUtil.csrfTokenRepository()).and()
				.addFilterAfter(AngularUtil.csrfHeaderFilter(), CsrfFilter.class);
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
		aAuth.userDetailsService(this.userDetailsService);
	}

}