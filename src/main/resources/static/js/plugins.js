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
		// console.log("response:" + JSON.stringify(aResponse.data));
		return (aResponse.data);
	},
	httpError : function(aResponse, aPromise) {
		if (!angular.isObject(aResponse.serverError)) {
			// console.log("error1: An unknown error occurred.");
			return (aPromise.reject({
				"accelerateMessage" : {
					"messageText" : "An unknown error occurred."
				}
			}));
		}

		// Otherwise, use expected error message.
		// console.log("error2:" + JSON.stringify(aResponse.data.message));
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