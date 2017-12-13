wmt.plugins = {};

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
wmt.plugins.AngularUtil = {
	"httpSuccess" : function(aResponse) {
		var _responseData = aResponse.data;
		if (!_responseData) {
			// service call with no return data
			return null;
		}

		if (_responseData.returnCode != 0 || aResponse.serverError) {
			wmt.plugins.AngularUtil.serverError(aResponse);
			return (wmt.angularSvc.q.reject(aResponse));
		}

		return _responseData;
	},

	"httpError" : function(aResponse) {
		if (!angular.isObject(aResponse.serverError)) {
			return (wmt.angularSvc.q.reject({
				"message" : {
					"messageText" : "An unknown error occurred."
				}
			}));
		}

		return (wmt.angularSvc.q.reject(aResponse));
	},

	"serverError" : function(aResponse) {
		var _responseData = aResponse.data;

		console.log("Error:" + _responseData.errorMessage);
		console.log(_responseData.errorDetails);
		wmt.plugins.AlertUtil.showPageAlert(
			_responseData.message.messageText, "error");
	},

	"changeRoute" : function(aPath) {
		$("#routingLink").attr("href", aPath).get(0).click();
	}
};

/**
 * Alert helper functions object
 */
wmt.plugins.AlertUtil = {
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
wmt.plugins.FileSystemUtil = {
	"showModal" : function(aFileSystemService, aFileType, aSelectTypes,
		aSelectCallback) {
		var _old = $("#_fileTree").jstree(true);
		if (_old) {
			_old.destroy();
		}

		$("#_fileTree")
			.jstree(
				{
					"conditionalselect" : function(node, event) {
						if (aSelectTypes === "all") {
							return true;
						}

						if (aSelectTypes === "anyFile"
							&& node.data.type !== "folder") {
							return true;
						}

						return aSelectTypes.indexOf(node.data.type) !== -1;
					},
					"plugins" : [ "conditionalselect" ],
					"core" : {
						"multiple" : false,
						"data" : function(aNode, aCallback) {
							var customRoot = $("#_fileTreeRoot").val();
							var dirPath = (aNode.id === "#") ? (customRoot ? customRoot
								: "")
								: aNode.data.path;
							var dirName = (aNode.id === "#") ? ""
								: aNode.text;

							aFileSystemService
								.getFileTree(dirPath, dirName,
									aFileType)
								.then(
									function(aResponse) {
										if (aResponse.message) {
											wmt.plugins.AlertUtil
												.showModalAlert(
													"_fileSystemModal",
													aResponse.message,
													"error");
										} else {
											aCallback(aResponse.fileTree);
											wmt.plugins.AlertUtil
												.hideModalAlert("_fileSystemModal");
										}
									},
									function(aResponse) {
										$("#_fileSystemModal")
											.modal("hide");
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

	"selectItem" : function() {
		var _jstree = $('#_fileTree').jstree(true);
		if (_jstree.selectCallback) {
			var _selectedNode = _jstree.get_selected(true)[0];
			if (!_selectedNode) {
				return;
			}

			_jstree.selectCallback(_selectedNode.data.path + "/"
				+ _selectedNode.text);
			_jstree.selectCallback = null;
		}
	},

	"selectArtwork" : function(aSuccessCallback, aFailureCallback) {
		$("#_artworkFileForm")[0].reset();
		$("#_artworkFile").click().change(
			function(aEvent) {
				var imgFile = this.files[0];

				if (!imgFile.type.match('image.*')) {
					aFailureCallback("Selected file not an image: "
						+ imgFile.name);
					return;
				}

				var reader = new FileReader();
				reader.onerror = function(evt) {
					var errorMsg = null;

					switch (evt.target.error.code) {
					case evt.target.error.NOT_FOUND_ERR:
						errorMsg = "File Not Found!";
					case evt.target.error.NOT_READABLE_ERR:
						errorMsg = "File is not readable";
					default:
						errorMsg = "An error occurred reading this file.";
					}

					aFailureCallback(errorMsg);
				};

				reader.onload = function() {
					aSuccessCallback(reader.result);
				};

				reader.readAsDataURL(imgFile);
			});
	}
};

/**
 * Alert helper functions object
 */
wmt.plugins.ParseTagsUtil = {
	"showModal" : function(aReset, aSampleFilname) {
		if (aReset) {
			$("#_customParseTokenSelect").text(aSampleFilname);
			wmt.plugins.ParseTagsUtil.reset();
		}

		$("#_parseTagExprModal").modal("show");
	},

	"reset" : function() {
		$("#_customParseTokenInput").val("");
		wmt.utils.clearSelectedText();
		$("#_customParseTokenOptionSelect").prop("checked", true);
		$("#_standardTokens a").show();
		$("#_tokenContainer").empty().html("<br /> <br />");
	},

	"addToken" : function(aToken) {
		var tokenText = null;
		var tokenVal = null;

		if (aToken) { // Called from standard token click
			tokenText = "<" + aToken + ">";
			tokenVal = aToken
		} else { // Called from add custom token button
			var tokenType = $("input[name='customParseTokenOption']:checked")
				.val();
			if (tokenType == "input") {
				tokenVal = tokenText = $("#_customParseTokenInput").val();
			} else {
				tokenVal = tokenText = wmt.utils.getSelectedText();
			}

			if (!tokenText) {
				return;
			}
		}

		if ($("#_tokenContainer span").length == 0) {
			$("#_tokenContainer").empty();
		}

		var span = $("<span />").addClass("label label-primary")
			.text(tokenText).data("token", tokenVal).appendTo(
			$("#_tokenContainer"));
		$("<span />").addClass("glyphicon glyphicon-remove").appendTo(span);

		$("#_customParseTokenInput").val("");
		wmt.utils.clearSelectedText();
	},

	"getTokens" : function(aFileControlId, aSuccessCallback, aFailureCallback) {
		var parseTokenList = "";
		$("#_tokenContainer span").each(function(aIdx, aSpan) {
			parseTokenList += $(aSpan).text();
		});

		return parseTokenList;
	}
};