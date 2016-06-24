'use strict';

/*
 * Controllers
 */
apollo.controllers = {};

/**
 * errorController
 * 
 * @param $scope
 * @param $$routeParams
 */
apollo.controllers.errorController = function($rootScope, $scope, $routeParams) {
	$rootScope.pageHeader = "Application Error";
	$rootScope.pageContentText = "";
	$rootScope.showPageControl = false;
	apollo.plugins.AlertUtil.hidePageAlert();

	var _statusCode = $routeParams.statusCode;
	if (_statusCode.match(/^(403|404)$/)) {
		$scope.statusCode = _statusCode;
	} else {
		$scope.statusCode = "ALL";
	}
}

/**
 * mainController
 */
apollo.controllers.mainController = function($rootScope, $scope, $http,
		$window, $q, $compile) {
	$rootScope.pageHeader = "Home";
	$rootScope.pageContentText = "This application provides a set of utilities to manage your music library";
	$rootScope.context = apollo.context;
	apollo.angularSvc = {};
	apollo.angularSvc.http = $http;
	apollo.angularSvc.window = $window;
	apollo.angularSvc.q = $q;
	apollo.angularSvc.compile = $compile;

	$scope.logout = function() {
		$("#_logoutForm").submit();
	}
}

/**
 * addAlbumController
 */
apollo.controllers.addAlbumController = function($rootScope, $scope,
		fileSystemService, albumService, tagService) {
	$rootScope.pageHeader = "Add Album";
	$rootScope.pageContentText = "Add a new album to the library";
	$rootScope.showPageControl = true;
	$rootScope.pageControlName = "Select Album";

	$scope.albumSelected = false;
	$scope.tagsParsed = false;

	/**
	 * Function to open the file browser to select an album
	 */
	$scope.selectAlbum = function() {
		$scope.albumSelected = false;
		apollo.plugins.FileSystemUtil.showModal(fileSystemService, function(
				aAlbumPath) {
			$scope.loadTracks(aAlbumPath);
		});
	};
	$rootScope.handlePageControl = $scope.selectAlbum;

	/**
	 * Function to fetch and diplay tags for the selected album
	 */
	$scope.loadTracks = function(aAlbumPath) {
		albumService.fetchTracks(aAlbumPath).then(function(aResponse) {
			$scope.loadAlbumTracksDT(aResponse);
		}, apollo.plugins.AngularUtil.serverError);
	};

	/**
	 * Reusable function to actually diplay the tags
	 */
	$scope.loadAlbumTracksDT = function(aResponse) {
		var responseData = aResponse.dataMap;
		$scope.albumPath = responseData.albumPath;
		$scope.albumTag = responseData.albumTag;
		$scope.trackTags = responseData.trackTags;
		$scope.trackTagMap = {};

		if (!$.fn.DataTable.isDataTable("#albumTracksDT")) {
			$scope.albumTracksDT = apollo.datatables.albumTracksDT(
					$scope.trackTagMap, function(aRow) {
						// $scope.showEditTrackTag(aRow);
					});

			$("#albumTracksDTOptions").prependTo(
					$("#albumTracksDT_wrapper div.top")).show();
			$("#albumTracksDTActions").appendTo(
					$("#albumTracksDT_wrapper div.bottom")).show();
		}

		$scope.albumTracksDT.clear();
		$scope.albumTracksDT.rows.add(responseData.trackTags);

		$scope.albumSelected = true;
		apollo.plugins.AlertUtil.hidePageAlert();

		$scope.albumTracksDT.columns.adjust().draw();
		$scope.albumTracksDT.responsive.rebuild();
		$scope.albumTracksDT.responsive.recalc();
	}

	/**
	 * Function to show the parse tag modal
	 */
	$scope.showParseTags = function(aReset) {
		if (aReset) {
			$scope.sampleFileName = $scope.trackTags[0].fileName;
			$scope.resetParseTagTokens();
		}

		$("#_parseTagExprModal").modal("show");
	};

	/**
	 * Function to add a custom parse token either by entering or selecting
	 */
	$scope.addParseTagToken = function(aToken) {
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
				tokenVal = tokenText = apollo.utils.getSelectedText();
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
		apollo.utils.clearSelectedText();
	}

	/**
	 * Function to reset all changes within parse tags modal
	 */
	$scope.resetParseTagTokens = function() {
		$scope.parseTagTokens = [];
		$("#_customParseTokenInput").val("");
		apollo.utils.clearSelectedText();
		$("#_customParseTokenOptionSelect").prop("checked", true);
		$("#_standardTokens a").show();
		$("#_tokenContainer").empty().html("<br /> <br />");
	}

	/**
	 * Function to parse the tags and display the result
	 */
	$scope.parseTags = function() {
		if ($("#_tokenContainer span").length == 0) {
			return;
		}

		$scope.parseTokenList = "";
		$("#_tokenContainer span").each(function(aIdx, aSpan) {
			$scope.parseTokenList += $(aSpan).text();
		});

		albumService.parseTags($scope.albumPath, $scope.parseTokenList).then(
				function(aResponse) {
					var responseData = aResponse.dataMap;
					$scope.parsedCommonTag = responseData.commonTag;
					$scope.parsedTags = responseData.trackTags;
					$scope.tagsParsed = true;
				});
		$("#_parseTagExprModal").modal("hide");
	};

	/**
	 * Function to discard the parsed tags and return to previous view
	 */
	$scope.discardParsedTags = function() {
		$scope.tagsParsed = false;
	};

	/**
	 * Function to re-show to parse tag model for editing
	 */
	$scope.reparseTags = function() {
		$scope.showParseTags(false);
	};

	/**
	 * Function to accept the parsed tags and save them
	 */
	$scope.saveParsedTags = function() {
		albumService.saveParsedTags($scope.albumPath, $scope.parseTokenList)
				.then(function(aResponse) {
					$scope.loadAlbumTracksDT(aResponse);
				});

		$scope.tagsParsed = false;
	};

	/**
	 * Function to open modal for editing album tag
	 */
	$scope.showEditAlbumTag = function() {
		$scope.tmpAlbumTag = angular.copy($scope.albumTag);
		$scope.updatedAlbumTag = {};
		$("#_editAlbumTagModal").modal("show");
	};

	/**
	 * Function to capture changes is album tag
	 */
	$scope.captureAlbumTagUpdate = function(aFieldName) {
		var currentValue = $scope.tmpAlbumTag[aFieldName];
		$scope.updatedAlbumTag[aFieldName] = (currentValue) ? currentValue
				: "|~|";
	};

	/**
	 * Function to choose an artwork
	 */
	$scope.chooseArtwork = function() {
		$("#_artworkFileForm")[0].reset();
		$("#_artworkFile").click().change(
				function() {
					var readResult = apollo.plugins.FileSystemUtil.readImage(
							"_artworkFile", function(aImageData) {
								$scope.tmpAlbumTag.artwork = aImageData;
								$scope.captureAlbumTagUpdate("artwork");
								$scope.$apply();
							}, function(aErrorMsg) {
								apollo.plugins.AlertUtil.showModalAlert(
										"_editAlbumTagModal", aErrorMsg,
										"error");
							});
				});
	};

	/**
	 * Function to delete artwork
	 */
	$scope.deleteArtwork = function() {
		$scope.tmpAlbumTag.artwork = "|~|";
		$scope.captureAlbumTagUpdate("artwork");
	};

	/**
	 * Function to save album tag
	 */
	$scope.saveAlbumTag = function() {
		if (jQuery.isEmptyObject($scope.updatedAlbumTag)) {
			return;
		}

		var _updatedTag = $scope.updatedAlbumTag;
		_updatedTag.filePath = $scope.albumPath;
		albumService.saveAlbumTag(_updatedTag).then(
				function(aResponse) {
					$.each(_updatedTag, function(aKey, aValue) {
						$scope.albumTag[aKey] = (aValue === "|~|") ? ""
								: aValue;
					});

					apollo.plugins.AlertUtil.showPageAlert(
							"Tags saved successfully for album tracks",
							"success");
				});

		$("#_editAlbumTagModal").modal("hide");
		$("#_editAlbumTagModal div.form-group").removeClass("bg-warning");
	};

	/**
	 * Function to open modal for editing track tag
	 */
	$scope.showEditTrackTag = function(aRow) {
		$scope.selectedRows = $scope.albumTracksDT.rows({
			selected : true
		}).data();

		if ($scope.selectedRows.length == 1) {
			$scope.tmpTrackTag = $scope.selectedRows[0];
		} else {
			$scope.tmpTrackTag = {};
			$.each($scope.selectedRows, function(aIdx, aRowData) {
				tagService.extractCommonTag($scope.tmpTrackTag,
						$scope.trackTagMap[aRowData.filePath]);
			});
		}

		// $scope.currentRow = aRow;
		// $scope.tmpTrackTag = angular.copy($scope.trackTagMap[aRow.id()]);
		// $scope.$apply();

		$scope.updatedTrackTag = {};
		$("#_editTrackTagModal").modal("show");
	};

	/**
	 * Function to capture changes is track tag
	 */
	$scope.captureTrackTagUpdate = function(aFieldName) {
		var currentValue = $scope.tmpTrackTag[aFieldName];
		$scope.updatedTrackTag[aFieldName] = (currentValue) ? currentValue
				: "|~|";
		$("#_" + aFieldName).parent().parent().addClass("bg-warning");
	};

	/**
	 * Function to save track tag
	 */
	$scope.saveTrackTag = function() {
		if (jQuery.isEmptyObject($scope.updatedTrackTag)) {
			return;
		}

		var _selectedRows = $scope.selectedRows;
		var _updatedTag = $scope.updatedTrackTag;

		if (_selectedRows.length == 1) {
			$scope.tmpTrackTag = $scope.selectedRows[0];
		} else {
			$scope.tmpTrackTag = {};
			$.each($scope.selectedRows, function(aIdx, aRowData) {
				tagService.extractCommonTag($scope.tmpTrackTag,
						$scope.trackTagMap[aRowData.filePath]);
			});
		}

		_updatedTag.filePath = $scope.tmpTrackTag.filePath;

		albumService.saveTrackTag(_updatedTag).then(
				function(aResponse) {
					var _tag = $scope.trackTagMap[_currentRow.id()];
					$.each(_updatedTag, function(aKey, aValue) {
						_tag[aKey] = (aValue === "|~|") ? "" : aValue;
					});

					_currentRow.data(_tag).draw();
					apollo.plugins.AlertUtil.showPageAlert(
							"Tags saved successfully", "success");
				});

		$("#_editTrackTagModal").modal("hide");
		$("#_editTrackTagModal div.form-group").removeClass("bg-warning");
	};

	/**
	 * 
	 */
	$scope.addToLibrary = function() {
		alert("adding to library");
	};

	/*
	 * Setup parse tags token add/remove functionality: start
	 */
	// add a standard token on click
	$("#_standardTokens a").click(function(aEvent) {
		var token = $(this).data("token");
		$scope.addParseTagToken(token);
		$("#_token-" + token).hide();
	});

	// remove a token on 'X' click
	$("#_tokenContainer").on("click", "span.glyphicon", function(aEvent) {
		var tokenSpan = $(this).parent();
		var token = tokenSpan.data("token");
		tokenSpan.remove();

		$("#_token-" + token).show();
	});

	// select custom token type on focus/click
	$("#_customParseTokenInput").focus(function(aEvent) {
		$("#_customParseTokenOptionInput").prop("checked", true);
	});

	$("#_customParseTokenSelect").click(function(aEvent) {
		$("#_customParseTokenOptionSelect").prop("checked", true);
	});

	// allow reordering of tokens
	$("#_tokenContainer").sortable();
	$("#_tokenContainer").disableSelection();
	/*
	 * Setup parse tags token add/remove functionality: end
	 */

	/*
	 * Initial call
	 */
	setTimeout(
			function() {
				// $scope.selectAlbum();
				$scope
						.loadTracks("C:/Temp/Raman-Raghav-2.0-320Kbps-2016[Songspk.LIVE]");
				// $scope
				// .loadTracks("/Users/rohitnarayanan/Music/Unorganized/Bollywood/Azhar-320Kbps-2016(Songspk.LINK)");
			}, 200);
};

/**
 * addAlbumController
 */
apollo.controllers.replaceTrackController = function($rootScope, $scope) {
	$rootScope.pageHeader = "Replace Track";
	$rootScope.pageContentText = "Replace a track file with one with better quality";
	$rootScope.showPageControl = false;
};