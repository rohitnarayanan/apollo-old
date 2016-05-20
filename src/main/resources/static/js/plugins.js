apollo.plugins = {};

/**
 * Function to remove class by wildcard
 */
$.fn.removeClassRegex = function(aRegExp) {
	return $(this).removeClass(function(index, classes) {
		return classes.split(/\s+/).filter(function(c) {
			return aRegExp.test(c);
		}).join(" ");
	});
};

/**
 * Angular helper functions object
 */
apollo.plugins.angularUtils = {
	httpSuccess : function(aResponse) {
		var _responseData = aResponse.data;
		if (!_responseData || _responseData.returnCode != 0 || aResponse.serverError) {
			apollo.plugins.angularUtils.serverError(aResponse);
			return null;
		}

		// console.log("apollo.plugins.angularUtils.httpSuccess:" +
		// JSON.stringify(aResponse.data));
		return (aResponse.data);
	},

	httpError : function(aResponse) {
		if (!angular.isObject(aResponse.serverError)) {
			// console.log("apollo.plugins.angularUtils.httpError: An unknown
			// error occurred.");
			return (apollo.angularSvc.q.reject({
				"accelerateMessage" : {
					"messageText" : "An unknown error occurred."
				}
			}));
		}

		// console.log("apollo.plugins.angularUtils.httpError:" + JSON.stringify(aResponse.data.message));
		return (aPromise.reject(aResponse));
	},

	serverError : function(aResponse) {
		
		console.log("Error:" + aResponse.errorMessage);
		console.log(aResponse.errorDetails);
		$("#_alertMessage").empty().text(
				aResponse.accelerateMessage.messageText).removeClassRegex(
				/^bg-/).addClass("bg-danger").show()
	},

	changeRoute : function(aPath) {
		$("#routingLink").attr("href", aPath).get(0).click();
	}
};