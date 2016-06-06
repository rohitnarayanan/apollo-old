package apollo.controller;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import accelerate.util.JSONUtil;
import accelerate.web.security.SecurityUtil;

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
	 * @param aModel
	 * @return
	 */
	@RequestMapping(path = "/home")
	public String home(Model aModel) {
		Map<String, Object> modelMap = new TreeMap<>();
		modelMap.put("path", this.servletContext.getContextPath());
		modelMap.put("session", SecurityUtil.getUserSession());

		aModel.addAttribute("context_info", JSONUtil.serialize(modelMap));

		return "home";
	}
}
