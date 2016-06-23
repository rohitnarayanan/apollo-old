package apollo.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SpringBootWebSecurityConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import accelerate.cache.PropertyCache;
import accelerate.util.JSONUtil;

/**
 * Main {@link Configuration} class for accelerate
 *
 * @author Rohit Narayanan
 * @version 1.0 Initial Version
 * @since Jul 20, 2014
 */
@SpringBootApplication(scanBasePackages = { "accelerate", "apollo" }, exclude = { DataSourceAutoConfiguration.class,
		SpringBootWebSecurityConfiguration.class })
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableMongoRepositories(basePackages = { "apollo.data" })
@EnableSpringDataWebSupport
public class ApolloConfig extends SpringBootServletInitializer {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.boot.context.web.SpringBootServletInitializer#
	 * configure(org.springframework.boot.builder.SpringApplicationBuilder)
	 */
	/**
	 * @param aBuilder
	 * @return
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder aBuilder) {
		return aBuilder.sources(ApolloConfig.class);
	}

	/**
	 * @return
	 */
	@Bean
	public static PropertyCache apolloConfigCache() {
		return new PropertyCache("ApolloConfig", "classpath:config/application.properties");
	}

	/**
	 * @return
	 */
	@Bean
	@Primary
	public static ObjectMapper objectMapper() {
		return JSONUtil.objectMapper();
	}

	/**
	 * @return
	 */
	@Bean
	public static EmbeddedServletContainerCustomizer customizeEmbeddedContainer() {
		return aContainer -> aContainer.addErrorPages(new ErrorPage("/acl/util/error"));
	}

	/**
	 * Configuration for Accelerate UI templates
	 * 
	 * @version 1.0 Initial Version
	 * @author Rohit Narayanan
	 * @since Feb 20, 2016
	 */
	@Configuration
	public static class ApolloWebConfig extends WebMvcConfigurerAdapter {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.web.servlet.config.annotation.
		 * WebMvcConfigurerAdapter#addViewControllers(org.springframework.web.
		 * servlet.config.annotation.ViewControllerRegistry)
		 */
		/**
		 * @param aRegistry
		 */
		@Override
		public void addViewControllers(ViewControllerRegistry aRegistry) {
			aRegistry.addViewController("/").setViewName("index");
			aRegistry.addViewController("/login").setViewName("login");
			aRegistry.addViewController("/errorPage/view").setViewName("error");

			aRegistry.addViewController("/view/main").setViewName("main");
			aRegistry.addViewController("/view/addAlbum").setViewName("tracks/addAlbum");
			aRegistry.addViewController("/view/replaceTrack").setViewName("tracks/replaceTrack");
		}
	}
}