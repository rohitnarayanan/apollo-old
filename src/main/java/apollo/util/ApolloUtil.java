package apollo.util;

import accelerate.databean.AccelerateMessage;
import accelerate.databean.AccelerateMessage.MessageType;
import accelerate.databean.AccelerateModel;
import accelerate.util.AppUtil;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 17, 2016
 */
public class ApolloUtil {
	/**
	 * 
	 */
	private ApolloUtil() {
	}

	/**
	 * @param aReturnCode
	 * @param aError
	 * @return
	 */
	public static final AccelerateModel prepareResponse(int aReturnCode, Exception aError) {
		AccelerateModel model = new AccelerateModel();
		model.setReturnCode(aReturnCode);

		if (aError != null) {
			model.setMessage(new AccelerateMessage(MessageType.ERROR, "serverError",
					"Server encountered an error while completing the operation."));
			model.addAttribute("errorMessage", aError.getMessage());
			model.addAttribute("errorDetails", AppUtil.getErrorLog(aError));
			model.addAttribute("serverError", true);
		}

		return model;
	}
}
