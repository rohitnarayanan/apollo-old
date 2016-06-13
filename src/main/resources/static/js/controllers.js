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

	$scope.albumSelected = false;
	$scope.tagTokens = [ "language", "genre", "album", "year", "composer",
			"albumArtist", "artist", "trackNbr", "title" ]

	$scope.selectAlbum = function() {
		$scope.albumSelected = false;
		apollo.plugins.FileSystemUtil.showModal(fileSystemService, function(
				aAlbumPath) {
			$scope.loadTracks(aAlbumPath);
		});
	}

	$scope.loadTracks = function(aAlbumPath) {
		albumService.fetchTracks(aAlbumPath).then(
				function(aResponse) {
					var responseData = aResponse.dataMap;

					$scope.response = aResponse;
					$scope.albumPath = responseData.albumPath;
					$scope.albumTag = responseData.commonTag;
					$scope.trackTagMap = {};

					if (!$.fn.DataTable.isDataTable("#albumTracksDT")) {
						$scope.albumTracksDT = apollo.datatables.albumTracksDT(
								$scope.trackTagMap, function(aRow) {
									$scope.editTrackTag(aRow.id());
								});
					}

					$scope.albumTracksDT.clear();
					$scope.albumTracksDT.rows.add(responseData.tags);

					$scope.albumSelected = true;

					$scope.albumTracksDT.columns.adjust().draw();
					$scope.albumTracksDT.responsive.rebuild();
					$scope.albumTracksDT.responsive.recalc();
				}, apollo.plugins.AngularUtil.serverError);
	};

	$scope.showParseTags = function() {
		$("#_parseTagExprModal").modal("show");
	};

	$scope.getTagTokens = function(aQuery) {
		return $.map($scope.tagTokens, function(aToken, aIdx) {
			return (aToken.startsWith(aQuery)) ? aToken : null;
		});
	};

	$scope.parseTags = function() {
		var tagExprTokens = "";
		$.each($scope.tagExpressions, function(aIdx, aExpr) {
			tagExprTokens += aExpr.text + "|";
		});

		alert("parseTags:" + tagExprTokens);
	};

	$scope.editTrackTag = function(aTrackId) {
		$scope.trackTag = $scope.trackTagMap[aTrackId];
		$scope.$apply();
		$("#_editTrackTagModal").modal("show");
	};

	$scope.editAlbumTag = function() {
		$("#_editAlbumTagModal").modal("show");
	};

	$scope.saveTags = function() {
		apollo.utils.alert("saveTags", $scope.trackTag);
	};

	$scope.saveAlbumTags = function() {
		apollo.utils.alert("saveAlbumTags", $scope.albumTag);
	};

	setTimeout(
			function() {
				// $scope.selectAlbum();
				$scope
						.loadTracks("C:/Temp/M/Library/Hindi/TempGenre/TempArtist/TempAlbum");
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