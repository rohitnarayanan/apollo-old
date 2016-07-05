package apollo.controller;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import accelerate.databean.DataMap;
import accelerate.web.security.SecurityUtil;
import apollo.config.ApolloConfigProps;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 15, 2016
 */
@Controller
public class ApolloController {
	/**
	 * {@link ServletContext} instance
	 */
	@Autowired
	private final ServletContext servletContext = null;

	/**
	 * 
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
