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
	$rootScope.pageTitle = "Application Error";
	$rootScope.pageDescription = "";
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
		$window, $q, $compile, $location, $route) {
	$rootScope.pageTitle = "Home";
	$rootScope.pageDescription = "This application provides a set of utilities to manage your music library";
	$rootScope.context = apollo.context;
	apollo.angularSvc = {};
	apollo.angularSvc.http = $http;
	apollo.angularSvc.window = $window;
	apollo.angularSvc.q = $q;
	apollo.angularSvc.compile = $compile;

	$scope.logout = function() {
		$("#_logoutForm").submit();
	}

	/*
	 * Always load the view on side menu link
	 */
	$('li.submenu-li a').click(function() {
		if ($location.$$path == "/" + this.hash.substring(1)) {
			$route.reload();
		}
	});

	/*
	 * Setup parse tags token add/remove functionality: start
	 */
	// add a standard token on click
	$("#_standardTokens a").click(function(aEvent) {
		var token = $(this).data("token");
		apollo.plugins.ParseTagsUtil.addToken(token);
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
}

/**
 * addAlbumController
 */
apollo.controllers.addAlbumController = function($rootScope, $scope,
		fileSystemService, albumService, tagService) {
	$rootScope.pageTitle = "Add Album";
	$rootScope.pageDescription = "Add a new album to the library";
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
		albumService.getTracks(aAlbumPath).then(function(aResponse) {
			$scope.loadAlbumTracksDT(aResponse);
		}, apollo.plugins.AngularUtil.serverError);
	};

	/**
	 * Reusable function to actually diplay the tags
	 */
	$scope.loadAlbumTracksDT = function(aResponse) {
		$scope.albumTag = aResponse.albumTag;
		$scope.trackTags = aResponse.trackTags;
		$scope.addedToLibrary = aResponse.addedToLibrary;
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
		$scope.albumTracksDT.rows.add(aResponse.trackTags);

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
		$scope.tagsParsed = false;
		apollo.plugins.ParseTagsUtil.showModal(aReset,
				$scope.trackTags[0].fileName);
	};

	/**
	 * Function to parse the tags and display the result
	 */
	$scope.parseTags = function() {
		var parseTokenList = apollo.plugins.ParseTagsUtil.getTokens();
		if (!parseTokenList) {
			return;
		}

		albumService.parseTags($scope.albumTag.filePath, parseTokenList).then(
				function(aResponse) {
					$scope.parsedCommonTag = aResponse.commonTag;
					$scope.parsedTags = aResponse.trackTags;
					$scope.tagsParsed = true;
				});
	};

	/**
	 * Function to accept the parsed tags and save them
	 */
	$scope.saveParsedTags = function() {
		albumService.saveParsedTags($scope.albumTag.filePath,
				$scope.parseTokenList).then(function(aResponse) {
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
		$("#_editAlbumTagModal div.form-group").removeClass("bg-warning");
		$("#_editAlbumTagModal").modal("show");
	};

	/**
	 * Function to capture changes is album tag
	 */
	$scope.captureAlbumTagUpdate = function(aFieldName) {
		var currentValue = $scope.tmpAlbumTag[aFieldName];
		$scope.updatedAlbumTag[aFieldName] = (currentValue) ? currentValue
				: "|~|";
		$("#_" + aFieldName).parent().parent().addClass("bg-warning");
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
		_updatedTag.filePath = $scope.albumTag.filePath;
		var _albumTag = $scope.albumTag;
		var _tmpAlbumTag = $scope.tmpAlbumTag;
		// albumService.saveAlbumTag(_updatedTag).then(
		// function(aResponse) {
		// $.each(_updatedTag, function(aKey, aValue) {
		// $scope.albumTag[aKey] = (aValue === "|~|") ? ""
		// : aValue;
		// });
		//
		// apollo.plugins.AlertUtil.showPageAlert(
		// "Tags saved successfully for album tracks",
		// "success");
		// });

		$("#_editAlbumTagModal").modal("hide");
	};

	/**
	 * Function to open modal for editing track tag
	 */
	$scope.showEditTrackTag = function(aRow) {
		$scope.selectedRows = $scope.albumTracksDT.rows({
			selected : true
		});

		$scope.selectedTags = $scope.albumTracksDT.rows({
			selected : true
		}).data();

		if ($scope.selectedTags.length == 1) {
			$scope.tmpTrackTag = $scope.selectedTags[0];
		} else {
			$scope.tmpTrackTag = {};
			$.each($scope.selectedTags, function(aIdx, aTag) {
				tagService.extractCommonTag($scope.tmpTrackTag,
						$scope.trackTagMap[aTag.filePath]);
			});
		}

		$scope.updatedTrackTag = {};
		$("#_editTrackTagModal div.form-group").removeClass("bg-warning");
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
		var _selectedTags = $scope.selectedTags;
		var _updatedTag = $scope.updatedTrackTag;

		if (_selectedTags.length == 1) {
			_updatedTag.filePath = $scope.tmpTrackTag.filePath;
			albumService.saveTrackTag(_updatedTag).then(
					function(aResponse) {
						var _tag = $scope.trackTagMap[_updatedTag.filePath];
						$.each(_updatedTag, function(aKey, aValue) {
							_tag[aKey] = (aValue === "|~|") ? "" : aValue;
						});

						_selectedRows.invalidate();
						apollo.plugins.AlertUtil.showPageAlert(
								"Tag saved successfully", "success");
					});
		} else {
			var _updatedList = [];
			$.each(_selectedTags, function(aIdx, aTag) {
				var tmpTag = angular.copy(_updatedTag);
				tmpTag.filePath = aTag.filePath;
				_updatedList.push(tmpTag);
			});

			albumService.saveTrackTags(_updatedList).then(
					function(aResponse) {
						$.each(_selectedTags, function(aIdx, aTag) {
							$.each(_updatedTag, function(aKey, aValue) {
								aTag[aKey] = (aValue === "|~|") ? "" : aValue;
							});
						});

						_selectedRows.invalidate();
						apollo.plugins.AlertUtil.showPageAlert(
								"Tag(s) saved successfully", "success");
					});
		}

		$("#_editTrackTagModal").modal("hide");
	};

	/**
	 * 
	 */
	$scope.addToLibrary = function() {
		albumService.addToLibrary($scope.albumTag).then(
				function(aResponse) {
					$scope.loadAlbumTracksDT(aResponse);

					if (aResponse.resultFlags) {
						apollo.plugins.AlertUtil.showPageAlert(
								"Album processed and added to Library",
								"success");
					} else {
						apollo.plugins.AlertUtil.showPageAlert(
								"Failed to processed and add album to Library - "
										+ aResponse.msgBuffer, "error");
					}
				});
	};

	/*
	 * Initial call
	 */
	setTimeout(function() {
		// $scope.selectAlbum();
		$scope.loadTracks("C:/Temp/Traffic-320Kbps-2016(Songspk.SITE)");
		// $scope
		// .loadTracks("/Users/rohitnarayanan/Music/Unorganized/Bollywood/Azhar-320Kbps-2016(Songspk.LINK)");
	}, 200);
};

/**
 * addPlaylistController
 */
apollo.controllers.addPlaylistController = function($rootScope, $scope) {
	$rootScope.pageTitle = "Add Playlist";
	$rootScope.pageDescription = "Add a new playlist to the library";
	$rootScope.showPageControl = false;
};

/**
 * addSongController
 */
apollo.controllers.addSongController = function($rootScope, $scope,
		fileSystemService, songService) {
	$rootScope.pageTitle = "Add Song";
	$rootScope.pageDescription = "Add a new song to the library";
	$rootScope.showPageControl = true;
	$rootScope.pageControlName = "Select Song";

	$scope.songSelected = false;
	$scope.tagsParsed = false;

	/**
	 * Function to open the file browser to select an album
	 */
	$scope.selectSong = function() {
		$scope.songSelected = false;
		apollo.plugins.FileSystemUtil.showModal(fileSystemService, function(
				aAlbumPath) {
			$scope.loadTag(aSongPath);
		});
	};
	$rootScope.handlePageControl = $scope.selectSong;

	/**
	 * Function to fetch and diplay tags for the selected album
	 */
	$scope.loadTag = function(aSongPath) {
		songService.getTag(aSongPath).then(function(aResponse) {
			$scope.songTag = aResponse.songTag;
			$scope.addedToLibrary = aResponse.addedToLibrary;
			$scope.tmpTag = angular.copy(aResponse.songTag);
			$scope.updatedTag = {};

			$scope.songSelected = true;
			apollo.plugins.AlertUtil.hidePageAlert();
		}, apollo.plugins.AngularUtil.serverError);
	};

	/**
	 * Function to show the parse tag modal
	 */
	$scope.showParseTags = function(aReset) {
		$scope.tagsParsed = false;
		apollo.plugins.ParseTagsUtil.showModal(aReset, $scope.songTag.fileName);
	};

	/**
	 * Function to parse the tags and display the result
	 */
	$scope.parseTags = function() {
		$scope.parseTokenList = apollo.plugins.ParseTagsUtil.getTokens();
		if (!$scope.parseTokenList) {
			return;
		}

		songService.parseTags($scope.songTag.filePath, $scope.parseTokenList)
				.then(function(aResponse) {
					$scope.parsedTag = aResponse.parsedTag;
					$scope.tagsParsed = true;
				});
	};

	/**
	 * Function to accept the parsed tags and save them
	 */
	$scope.saveParsedTags = function() {
		songService.saveParsedTags($scope.songTag.filePath,
				$scope.parseTokenList).then(function(aResponse) {
			$scope.songTag = aResponse.songTag;
			$scope.addedToLibrary = aResponse.addedToLibrary;
			$scope.tmpTag = angular.copy(aResponse.songTag);
			$scope.updatedTag = {};
		});

		$scope.tagsParsed = false;
	};

	/**
	 * Function to capture changes is album tag
	 */
	$scope.captureTagUpdate = function(aFieldName) {
		var currentValue = $scope.tmpTag[aFieldName];
		$scope.updatedTag[aFieldName] = (currentValue) ? currentValue : "|~|";
		$("#_" + aFieldName).parent().parent().addClass("bg-warning");
	};

	/**
	 * Function to choose an artwork
	 */
	$scope.chooseArtwork = function() {
		apollo.plugins.FileSystemUtil.selectArtwork(function(aImageData) {
			$scope.tmpTag.artwork = aImageData;
			$scope.captureTagUpdate("artwork");
			$scope.$apply();
		}, function(aErrorMsg) {
			apollo.plugins.AlertUtil.showPageAlert(aErrorMsg, "error");
		});
	};

	/**
	 * Function to delete artwork
	 */
	$scope.deleteArtwork = function() {
		$scope.tmpTag.artwork = "|~|";
		$scope.captureTagUpdate("artwork");
	};

	/**
	 * Function to save tag
	 */
	$scope.saveTag = function() {
		if (jQuery.isEmptyObject($scope.updatedTag)) {
			return;
		}

		var _updatedTag = $scope.updatedTag;
		_updatedTag.filePath = $scope.trackTag.filePath;
		tagService.saveTag(_updatedTag).then(
				function(aResponse) {
					$.each(_updatedTag, function(aKey, aValue) {
						$scope.trackTag[aKey] = (aValue === "|~|") ? ""
								: aValue;
						$scope.tmpTag = angular.copy($scope.trackTag);
					});

					apollo.plugins.AlertUtil.showPageAlert(
							"Tag saved successfully", "success");
				});
	};

	/**
	 * 
	 */
	$scope.addToLibrary = function() {
		tagService.addToLibrary($scope.trackTag).then(
				function(aResponse) {
					$scope.loadAlbumTracksDT(aResponse);

					if (aResponse.resultFlag) {
						apollo.plugins.AlertUtil.showPageAlert(
								"Track processed and added to Library",
								"success");
					} else {
						apollo.plugins.AlertUtil.showPageAlert(
								"Failed to processed and add album to Library - "
										+ aResponse.msgBuffer, "error");
					}
				});
	};
};

/**
 * browseAlbumsController
 */
apollo.controllers.browseAlbumsController = function($rootScope, $scope) {
	$rootScope.pageTitle = "Albums";
	$rootScope.pageDescription = "Browse all albums in the library";
	$rootScope.showPageControl = false;
};

/**
 * browsePlaylistsController
 */
apollo.controllers.browsePlaylistsController = function($rootScope, $scope) {
	$rootScope.pageTitle = "Playlists";
	$rootScope.pageDescription = "Browse all playlists in the library";
	$rootScope.showPageControl = false;
};

/**
 * browseArtistsController
 */
apollo.controllers.browseArtistsController = function($rootScope, $scope) {
	$rootScope.pageTitle = "Albums";
	$rootScope.pageDescription = "Browse all artists in the library";
	$rootScope.showPageControl = false;
};

/**
 * browseSongsController
 */
apollo.controllers.browseSongsController = function($rootScope, $scope) {
	$rootScope.pageTitle = "Songs";
	$rootScope.pageDescription = "Browse all songs in the library";
	$rootScope.showPageControl = false;
};