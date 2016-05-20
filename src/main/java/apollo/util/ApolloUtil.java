package apollo.util;

import accelerate.databean.AccelerateMessage;
import accelerate.databean.AccelerateMessage.MessageType;
import accelerate.databean.AccelerateWebResponse;
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
	public static final AccelerateWebResponse prepareResponse(int aReturnCode, Throwable aError) {
		AccelerateWebResponse response = new AccelerateWebResponse();
		response.setReturnCode(aReturnCode);

		if (aError != null) {
			response.setMessage(new AccelerateMessage(MessageType.ERROR, "serverError",
					"Server encountered an error while completing the operation."));
			response.put("errorMessage", aError.getMessage());
			response.put("errorDetails", AppUtil.getErrorLog(aError));
			response.put("serverError", true);
		}

		return response;
	}
}
