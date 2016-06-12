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
			return (apollo.angularSvc.q.reject(aResponse));
		}

		return (aResponse.data);
	},

	"httpError" : function(aResponse) {
		if (!angular.isObject(aResponse.serverError)) {
			return (apollo.angularSvc.q.reject({
				"message" : {
					"messageText" : "An unknown error occurred."
				}
			}));
		}

		return (apollo.angularSvc.q.reject(aResponse));
	},

	"serverError" : function(aResponse) {
		var _responseData = aResponse.data;

		console.log("Error:" + _responseData.dataMap.errorMessage);
		console.log(_responseData.dataMap.errorDetails);
		apollo.plugins.AlertUtil.showPageAlert(
				_responseData.message.messageText, "error");
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
		$("#_fileTree")
				.jstree(
						{
							"core" : {
								"multiple" : false,
								"data" : function(aNode, aCallback) {
									var customRoot = $("#_fileTreeRoot").val();
									var dirPath = (aNode.id === "#") ? (customRoot ? customRoot
											: "")
											: aNode.data;
									var dirName = (aNode.id === "#") ? ""
											: aNode.text;

									aFileSystemService.listFolders(dirPath,
											dirName).then(function(aResponse) {
										aCallback(aResponse.dataMap.folders);
									}, function(aResponse) {
										$("#_fileSystemModal").modal("hide");
									});
								}
							}
						});

		$('#_fileTree').jstree(true).selectCallback = aSelectCallback;
		$("#_fileSystemModal").modal("show");
	},

	"loadTree" : function() {
		$('#_fileTree').jstree(true).refresh();
	},

	"selectFolder" : function() {
		var _jstree = $('#_fileTree').jstree(true);
		if (_jstree.selectCallback) {
			var _selectedNode = _jstree.get_selected(true)[0];
			_jstree.selectCallback(_selectedNode.data + "/"
					+ _selectedNode.text);
			_jstree.selectCallback = null;
		}

		$("#_fileSystemModal").modal("hide");
	}
};