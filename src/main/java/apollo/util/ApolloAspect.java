package apollo.util;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import accelerate.utils.JSONUtil;
import accelerate.utils.exception.AccelerateException;
import accelerate.web.Response;

/**
 * This class contains aop configuration for logging purpose
 * 
 * @version 1.0 Initial Version
 * @author TCS
 * @since Feb 8, 2016
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
public class ApolloAspect {
	/**
	 * {@link Logger} instance
	 */
	private static final Logger _logger = LoggerFactory.getLogger(ApolloAspect.class);

	/**
	 * {@link Logger} instance
	 */
	private static final Logger requestLogger = LoggerFactory.getLogger("RequestLogger");

	/**
	 * @param aJoinPoint
	 * @return
	 */
	@Around("execution(* apollo.controller.*.*(..)) and @annotation(apollo.util.HandleError)")
	public static Object handleError(ProceedingJoinPoint aJoinPoint) {
		Response response = null;

		List<Object> requestArgs = new ArrayList<>();
		for (Object arg : aJoinPoint.getArgs()) {
			if ((arg instanceof ServletRequest) || (arg instanceof ServletResponse) || (arg instanceof HttpSession)) {
				continue;
			}

			requestArgs.add(arg);
		}
		requestLogger.debug("Request:{}", JSONUtil.serialize(requestArgs));

		try {
			response = (Response) aJoinPoint.proceed();
		} catch (Throwable error) {
			if (error instanceof Error) {
				throw new AccelerateException(error);
			}

			_logger.error("Unhandled error [{}] in Apollo.", error.getMessage(), error);
			response = ApolloUtil.prepareResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, (Exception) error);
		}

		requestLogger.debug("Response:{}", JSONUtil.serialize(response));
		return response;
	}
}
