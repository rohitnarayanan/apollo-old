/*
 * Global object to wrap everything
 */
var apollo = {};

if (window.context_info) {
	apollo.context = context_info;
}

apollo.utils = {
	"toJSON" : function(aJSONString) {
		return JSON.parse(aJSONString);
	},

	"createMessage" : function(aMessage, aArgs) {
		var msg = aMessage + ":";
		$.each(aArgs, function(aObj, aIdx) {
			msg += JSON.stringify(aObj) + "|~|";
		});

		return msg;
	},

	"alert" : function(aMessage) {
		alert(apollo.utils.createMessage(aMessage, Array.prototype.slice.call(
				arguments, 1)));
	},

	"log" : function(aMessage) {
		console.log(apollo.utils.createMessage(aMessage, Array.prototype.slice
				.call(arguments, 1)));
	},

	"escapeHTML" : function(aText) {
		return aText.replace('\'', '&apos;').replace('\"', '&quot;');
	},

	"isEmpty" : function(aVal) {
		if (!aVal || aVal.trim().length == 0) {
			return true;
		}

		return false;
	}
};
