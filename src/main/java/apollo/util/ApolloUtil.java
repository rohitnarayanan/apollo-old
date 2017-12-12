package apollo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import accelerate.utils.CommonUtils;
import accelerate.web.Message;
import accelerate.web.Message.MessageType;
import accelerate.web.Response;

/**
 * PUT DESCRIPTION HERE
 * 
 * @version 1.0 Initial Version
 * @author Rohit Narayanan
 * @since Apr 17, 2016
 */
public class ApolloUtil {
	/**
	 * {@link Logger} instance
	 */
	private static final Logger _LOGGER = LoggerFactory.getLogger(ApolloUtil.class);

	/**
	 * hidden constructor
	 */
	private ApolloUtil() {
	}

	/**
	 * @param aReturnCode
	 * @param aError
	 * @return
	 */
	public static final Response prepareResponse(int aReturnCode, Exception aError) {
		Response response = new Response();
		response.setReturnCode(aReturnCode);
		response.setServerError(true);
		// response.setError(aError);

		if (aError != null) {
			response.setMessage(new Message(MessageType.ERROR, "serverError",
					"Server encountered an error while completing the operation."));
			response.put("errorMessage", aError.getMessage());
			response.put("errorDetails", CommonUtils.getErrorLog(aError));
		}

		return response;
	}
}
