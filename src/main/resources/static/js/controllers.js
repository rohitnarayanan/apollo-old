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

	$rootScope.logout = function() {
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
	$scope.selectAlbum = function(aCustomPath) {
		$scope.albumSelected = false;
		if (aCustomPath) {
			$("#_fileTreeRoot").val(aCustomPath);
		}
		apollo.plugins.FileSystemUtil.showModal(fileSystemService, "none",
				"all", function(aAlbumPath) {
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
			$scope.albumTracksDT = apollo.datatables
					.albumTracksDT($scope.trackTagMap);

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
	$rootScope.parseTags = function() {
		$scope.parseTokenList = apollo.plugins.ParseTagsUtil.getTokens();
		if (!$scope.parseTokenList) {
			return;
		}

		albumService.parseTags($scope.albumTag.filePath, $scope.parseTokenList)
				.then(function(aResponse) {
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
		apollo.plugins.FileSystemUtil.selectArtwork(function(aImageData) {
			$scope.tmpAlbumTag.artwork = aImageData;
			$scope.captureAlbumTagUpdate("artwork");
			$scope.$apply();
		}, function(aErrorMsg) {
			apollo.plugins.AlertUtil.showModalAlert("_editAlbumTagModal",
					aErrorMsg, "error");
		});
	};

	/**
	 * Function to delete artwork
	 */
	$scope.deleteArtwork = function() {
		$scope.tmpAlbumTag.artwork = null;
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
		albumService.saveAlbumTag(_updatedTag).then(
				function(aResponse) {
					$.each(_updatedTag, function(aKey, aValue) {
						$scope.albumTag[aKey] = (aValue === "|~|") ? ""
								: aValue;
					});

					$scope.addedToLibrary = tagService
							.checkAlbumLocation($scope.albumTag);

					apollo.plugins.AlertUtil.showPageAlert(
							"Tags saved successfully for album tracks",
							"success");
				});
	};

	/**
	 * Function to open modal for editing track tag
	 */
	$scope.showEditTrackTag = function(aRow) {
		var _selectedRows = $scope.albumTracksDT.rows({
			selected : true
		});
		var rowCount = _selectedRows.ids().length;
		if (rowCount == 0) {
			return;
		}

		$scope.selectedRows = _selectedRows;
		$scope.selectedTags = $scope.albumTracksDT.rows({
			selected : true
		}).data();

		if (rowCount == 1) {
			$scope.tmpTrackTag = $scope.selectedTags[0];
		} else {
			$scope.tmpTrackTag = {};
			$.each($scope.selectedTags, function(aIdx, aTag) {
				tagService.getCommonTag($scope.tmpTrackTag,
						$scope.albumTracksDT.rowDataMap[aTag.filePath]);
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
	$scope.saveTracksTag = function() {
		if (jQuery.isEmptyObject($scope.updatedTrackTag)) {
			return;
		}

		var _selectedRows = $scope.selectedRows;
		var _selectedTags = $scope.selectedTags;
		var _updatedTag = $scope.updatedTrackTag;

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
	};

	/**
	 * Function to rename tracks within the album to their title
	 */
	$scope.renameTracks = function() {
		/*
		 * basic copy of album tag to avoid sending artwork data when not
		 * required
		 */
		var tmpAlbumTag = {};
		tmpAlbumTag.language = $scope.albumTag.language;
		tmpAlbumTag.genre = $scope.albumTag.genre;
		tmpAlbumTag.album = $scope.albumTag.album;
		tmpAlbumTag.albumArtist = $scope.albumTag.albumArtist;
		tmpAlbumTag.filePath = $scope.albumTag.filePath;

		albumService.renameTracks(tmpAlbumTag).then(
				function(aResponse) {
					$scope.loadAlbumTracksDT(aResponse);

					if (aResponse.resultFlags) {
						apollo.plugins.AlertUtil.showPageAlert(
								"Tracks renamed to title", "success");
					} else {
						apollo.plugins.AlertUtil.showPageAlert(
								"Failed to rename tracks - "
										+ aResponse.msgBuffer, "error");
					}
				});
	}

	/**
	 * Function to add album to library
	 */
	$scope.addToLibrary = function() {
		/*
		 * basic copy of album tag to avoid sending artwork data when not
		 * required
		 */
		var tmpAlbumTag = {};
		tmpAlbumTag.language = $scope.albumTag.language;
		tmpAlbumTag.genre = $scope.albumTag.genre;
		tmpAlbumTag.album = $scope.albumTag.album;
		tmpAlbumTag.albumArtist = $scope.albumTag.albumArtist;
		tmpAlbumTag.filePath = $scope.albumTag.filePath;

		albumService.addToLibrary(tmpAlbumTag).then(
				function(aResponse) {
					$scope.loadAlbumTracksDT(aResponse);

					if (aResponse.resultFlags) {
						apollo.plugins.AlertUtil.showPageAlert(
								"Album added to Library path", "success");
					} else {
						apollo.plugins.AlertUtil.showPageAlert(
								"Failed to add album to Library path - "
										+ aResponse.msgBuffer, "error");
					}
				});
	};

	/*
	 * Initial call
	 */
	setTimeout(
			function() {
				$scope
						.selectAlbum("/Users/rohitnarayanan/Music/Unorganized/Bollywood");
			}, 200);
};

/**
 * addSongController
 */
apollo.controllers.addSongController = function($rootScope, $scope,
		fileSystemService, songService, tagService) {
	$rootScope.pageTitle = "Add Song";
	$rootScope.pageDescription = "Add a new song to the library";
	$rootScope.showPageControl = true;
	$rootScope.pageControlName = "Select Song";

	$scope.songSelected = false;
	$scope.tagsParsed = false;

	/**
	 * Function to open the file browser to select an album
	 */
	$scope.selectSong = function(aCustomPath) {
		$scope.songSelected = false;
		if (aCustomPath) {
			$("#_fileTreeRoot").val(aCustomPath);
		}
		apollo.plugins.FileSystemUtil.showModal(fileSystemService,
				apollo.context.configProps.fileExtn, "anyFile", function(
						aSongPath) {
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
	$rootScope.parseTags = function() {
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
		$scope.tmpTag.artwork = null;
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
		_updatedTag.filePath = $scope.songTag.filePath;
		songService.saveTag(_updatedTag).then(
				function(aResponse) {
					$.each(_updatedTag,
							function(aKey, aValue) {
								$scope.songTag[aKey] = (aValue === "|~|") ? ""
										: aValue;
							});

					$scope.addedToLibrary = tagService
							.checkSongLocation($scope.songTag);

					$scope.tmpTag = angular.copy($scope.songTag);
					apollo.plugins.AlertUtil.showPageAlert(
							"Tag saved successfully", "success");
				});
	};

	/**
	 * Function to add album to library
	 */
	$scope.addToLibrary = function() {
		songService.addToLibrary($scope.songTag.filePath).then(
				function(aResponse) {
					$scope.songTag = aResponse.songTag;
					$scope.addedToLibrary = aResponse.addedToLibrary;
					$scope.tmpTag = angular.copy(aResponse.songTag);
					$scope.updatedTag = {};

					$scope.songSelected = true;

					if (aResponse.resultFlag) {
						apollo.plugins.AlertUtil.showPageAlert(
								"Song added to Library", "success");
					} else {
						apollo.plugins.AlertUtil.showPageAlert(
								"Failed to process and add song to Library - "
										+ aResponse.msgBuffer, "error");
					}
				});
	};

	/*
	 * Initial call
	 */
	setTimeout(function() {
		$scope.selectSong("/Users/rohitnarayanan/Music/Unorganized");
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