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
apollo.controllers.errorController = function($scope, $routeParams) {
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
	$rootScope.pageHeader = "This is Apollo";
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
		fileSystemService, albumService) {
	$rootScope.pageHeader = "Add Album";
	$rootScope.pageContentText = "Add a new album to the library";
	$rootScope.showPageControl = true;
	$rootScope.pageControlName = "Select Album";

	$scope.albumSelected = false;
	$scope.tagsParsed = false;

	$scope.selectAlbum = function() {
		$scope.albumSelected = false;
		apollo.plugins.FileSystemUtil.showModal(fileSystemService, function(
				aAlbumPath) {
			$scope.loadTracks(aAlbumPath);
		});
	};
	$rootScope.handlePageControl = $scope.selectAlbum;

	$scope.loadTracks = function(aAlbumPath) {
		albumService.fetchTracks(aAlbumPath).then(function(aResponse) {
			$scope.loadAlbumTracksDT(aResponse);
		}, apollo.plugins.AngularUtil.serverError);
	};

	$scope.loadAlbumTracksDT = function(aResponse) {
		var responseData = aResponse.dataMap;
		$scope.albumPath = responseData.albumPath;
		$scope.commonTag = responseData.commonTag;
		$scope.trackTags = responseData.trackTags;
		$scope.trackTagMap = {};

		if (!$.fn.DataTable.isDataTable("#albumTracksDT")) {
			$scope.albumTracksDT = apollo.datatables.albumTracksDT(
					$scope.trackTagMap, function(aRow) {
						$scope.showEditTrackTag(aRow.id());
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

	$scope.showParseTags = function(aReset) {
		if (aReset) {
			$scope.sampleFileName = $scope.trackTags[0].fileName;
			$scope.resetParseTagTokens();
		}

		$("#_parseTagExprModal").modal("show");
	};

	$scope.addParseTagToken = function(aToken) {
		var _token = null;
		if (aToken) {
			_token = "<" + aToken + ">";
		} else {
			_token = $("#_customParseToken").val();
			if (!_token) {
				return;
			}
		}

		if ($("#_tokenContainer span").length == 0) {
			$("#_tokenContainer").empty();
		}

		$("<span />").addClass("label label-primary").text(_token).appendTo(
				$("#_tokenContainer"));
		// var li = $("<li
		// />").addClass("active").appendTo($("#_tokenContainer"));
		// $("<a />").attr("href", "").text(_token).appendTo(li);

		$scope.parseTagTokens += _token;
		$("#_customParseToken").val("");
	}

	$scope.resetParseTagTokens = function() {
		$scope.parseTagTokens = "";
		$("#_tokenContainer").empty().html("<br /> <br />");
	}

	$scope.parseTags = function() {
		albumService.parseTags($scope.albumPath, $scope.parseTagTokens).then(
				function(aResponse) {
					var responseData = aResponse.dataMap;
					$scope.parsedCommonTag = responseData.commonTag;
					$scope.parsedTags = responseData.trackTags;
					$scope.tagsParsed = true;
					// apollo.plugins.AlertUtil.showPageAlert(
					// "Tags parsed from file names "
					// + "as per provided tokens", "info");
				});
	};

	$scope.discardParsedTags = function() {
		$scope.tagsParsed = false;
	};

	$scope.reparseTags = function() {
		$scope.showParseTags(false);
	};

	$scope.saveParsedTags = function() {
		$scope.tagsParsed = false;
	};

	/**
	 * Function to open modal for editing album tag
	 */
	$scope.showEditAlbumTag = function() {
		$scope.albumTag = angular.copy($scope.commonTag);
		$("#_editAlbumTagModal").modal("show");
	};

	$scope.chooseArtwork = function() {
		$("#_artworkFileForm")[0].reset();
		$("#_artworkFile")
				.click()
				.change(
						function() {
							if (!$scope.albumTag.artwork) {
								$scope.albumTag.artwork = {};
							}

							var readResult = apollo.plugins.FileSystemUtil
									.readImage(
											"_artworkFile",
											function(aImageData) {
												$scope.albumTag.artwork.base64Data = aImageData;
												$scope.$apply();
											},
											function(aErrorMsg) {
												apollo.plugins.AlertUtil
														.showModalAlert(
																"_editAlbumTagModal",
																aErrorMsg,
																"error");
											});
						});
	};

	$scope.deleteArtwork = function() {
		$scope.albumTag.artwork = null;
	};

	/**
	 * Function to open modal for editing track tag
	 */
	$scope.showEditTrackTag = function(aTrackPath) {
		$scope.trackTag = angular.copy($scope.trackTagMap[aTrackPath]);
		$scope.$apply();
		$("#_editTrackTagModal").modal("show");
	};

	/**
	 * Function to save album tag
	 */
	$scope.saveAlbumTag = function() {
		$scope.albumTag.dataMap = {
			"albumPath" : $scope.albumPath
		};

		albumService.saveAlbumTag($scope.albumTag).then(
				function(aResponse) {
					$scope.commonTag = aResponse.dataMap.savedTag;
					apollo.plugins.AlertUtil.showPageAlert(
							"Tags saved successfully for album tracks",
							"success");
				});
	};

	/**
	 * Function to save track tag
	 */
	$scope.saveTrackTag = function() {
		$scope.trackTag.dataMap = {
			"trackPath" : $scope.trackTag.filePath
		};

		albumService.saveTrackTag($scope.trackTag).then(
				function(aResponse) {
					var newTrackTag = aResponse.dataMap.savedTag;
					$scope.trackTagMap[newTrackTag.filePath] = newTrackTag;
					apollo.plugins.AlertUtil.showPageAlert(
							"Tags saved successfully", "success");
				});
	};

	$scope.renameToTitle = function() {
		alert("renaming tracks");
	};

	$scope.addToLibrary = function() {
		alert("adding to library");
	};

	/*
	 * Setup parse tags drag/drop functionality
	 */
	$("#_standardTokens li").draggable({
		appendTo : "#_parseTagExprModalBody",
		helper : "clone",
		cursor : "pointer",
		revert : "invalid"
	});

	$("#_droppableContainer").droppable({
		hoverClass : "bg-warning",
		accept : "#_standardTokens li",
		drop : function(event, ui) {
			$scope.addParseTagToken(ui.draggable.data("token"));
		}
	});

	$("#_scopeApplyBtn").click(function() {
		$scope.$apply();
	})

	/*
	 * Initial call
	 */
	setTimeout(
			function() {
				// $scope.selectAlbum();
				$scope
						.loadTracks("C:/Temp/M/Library/Hindi/TempGenre/TempArtist/TempAlbum");
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

	$('#example').DataTable();
};