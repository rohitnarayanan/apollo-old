package apollo.controller;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import accelerate.utils.bean.DataMap;
import accelerate.utils.logging.AutowireLogger;
import apollo.config.ApolloConfigProps;
import apollo.config.SecurityUtil;

/**
 * Base controller for the application
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since December 11, 2017
 */
@Controller
public class ApolloController {
	/**
	 * {@link Logger} instance
	 */
	@AutowireLogger
	private Logger _logger = null;

	/**
	 * {@link ServletContext} instance
	 */
	@Autowired
	private final ServletContext servletContext = null;

	/**
	 * {@link ApolloConfigProps} instance
	 */
	@Autowired
	private ApolloConfigProps apolloConfigProps = null;

	/**
	 * @param aModel
	 * @return
	 */
	@RequestMapping(path = "/home")
	public String home(Model aModel) {
		aModel.addAttribute("context_info", DataMap.buildMap("path", this.servletContext.getContextPath(), "session",
				SecurityUtil.getUserSession(), "configProps", this.apolloConfigProps).toString());

		return "home";
	}
}
