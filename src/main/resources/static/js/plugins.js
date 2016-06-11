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
apollo.plugins.AngularUtil = {
	"httpSuccess" : function(aResponse) {
		var _responseData = aResponse.data;
		if (!_responseData || _responseData.returnCode != 0
				|| aResponse.serverError) {
			apollo.plugins.AngularUtil.serverError(aResponse);
			return null;
		}

		return (aResponse.data);
	},

	"httpError" : function(aResponse) {
		if (!angular.isObject(aResponse.serverError)) {
			return (apollo.angularSvc.q.reject({
				"accelerateMessage" : {
					"messageText" : "An unknown error occurred."
				}
			}));
		}

		return (apollo.angularSvc.q.reject(aResponse));
	},

	"serverError" : function(aResponse) {
		console.log("Error:" + aResponse.errorMessage);
		console.log(aResponse.errorDetails);
		apollo.plugins.AlertUtil.showPageAlert(
				aResponse.accelerateMessage.messageText, "error");
	},

	"changeRoute" : function(aPath) {
		$("#routingLink").attr("href", aPath).get(0).click();
	}
};

/**
 * Alert helper functions object
 */
apollo.plugins.AlertUtil = {
	"msgClass" : {
		"info" : "msg-info",
		"success" : "msg-success",
		"warn" : "msg-warn",
		"error" : "msg-error"
	},

	"iconClass" : {
		"info" : "glyphicon-info-sign",
		"success" : "glyphicon-ok-sign",
		"warn" : "glyphicon-alert",
		"error" : "glyphicon-remove"
	// glyphicon-exclamation-sign
	},

	"showPageAlert" : function(aMessage, aMessageType) {
		$("#_pageAlertIcon").removeClassRegex(/^glyphicon-/).addClass(
				this.iconClass[aMessageType]);
		$("#_pageAlertMsg").empty().text(aMessage).parent().removeClassRegex(
				/^msg-/).addClass(this.msgClass[aMessageType]).show().get(0)
				.scrollIntoView();
	},

	"hidePageAlert" : function() {
		$('#_pageAlertMsg').parent().hide();
	},

	"showModalAlert" : function(aModalId, aMessage, aMessageType) {
		$("#" + aModalId + " ._modalAlertIcon").removeClassRegex(/^glyphicon-/)
				.addClass(this.iconClass[aMessageType]);
		$("#" + aModalId + " ._modalAlertMsg").empty().text(aMessage).parent()
				.removeClassRegex(/^msg-/)
				.addClass(this.msgClass[aMessageType]).show().get(0)
				.scrollIntoView();
	},

	"hideModalAlert" : function(aModalId) {
		$("#" + aModalId + " ._modalAlertMsg").parent().hide();
	}
};

/**
 * Alert helper functions object
 */
apollo.plugins.FileSystemUtil = {
	"showModal" : function(aFileSystemService, aSelectCallback) {
		$("#_fileTree").jstree(
				{
					"core" : {
						"multiple" : false,
						"data" : function(aNode, aCallback) {
							var _core = this;
							var dirPath = (aNode.id === "#") ? "" : _core.path;
							var dirName = (aNode.id === "#") ? "" : aNode.text;

							aFileSystemService.listFolders(dirPath, dirName)
									.then(function(aResponse) {
										_core.path = aResponse.dataMap.path;
										aCallback(aResponse.dataMap.folders);
									}, apollo.plugins.AngularUtil.serverError);
						}
					}
				});

		$('#_fileTree').jstree(true).selectCallback = aSelectCallback;
		$("#_fileSystemModal").modal("show");
	},

	"selectFolder" : function() {
		var _jstree = $('#_fileTree').jstree(true);
		if (_jstree.selectCallback) {
			_jstree.selectCallback(_jstree.path + "/"
					+ _jstree.get_selected(true)[0].text);
			_jstree.selectCallback = null;
		}

		$("#_fileSystemModal").modal("hide");
	}
};