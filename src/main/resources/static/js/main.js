/*
 * Global object to wrap everything
 */
var apollo = {};

if (window.context_info) {
	apollo.context = context_info;
}

apollo.utils = {
	"showPageAlert" : function(aMessage, aMessageClass) {
		$('#_alertMessage').empty().text(aMessage).removeClassRegex(/^alert-/)
				.addClass(aMessageClass).show().get(0).scrollIntoView();
	},

	"hidePageAlert" : function() {
		$('#_alertMessage').hide();
	},

	"showModalAlert" : function(aMessage, aMessageClass) {
		$('#_modalAlert').empty().text(aMessage).removeClassRegex(/^alert-/)
				.addClass(aMessageClass).show().get(0).scrollIntoView();
	},

	"hideModalAlert" : function() {
		$('#_modalAlert').hide();
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