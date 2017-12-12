'use strict';

/*
 * Services
 */
apollo.services = {};

/**
 * 
 */
apollo.services.fileSystemService = function($http, $q) {
	return ({
		"getFileTree" : getFileTree,
		"compareFolders" : compareFolders,
		"copyFiles" : copyFiles
	});

	function getFileTree(aDirPath, aDirName, aFileType) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/fileSystem/fileTree",
			params : {
				"dirPath" : aDirPath,
				"dirName" : aDirName,
				"fileType" : aFileType
			}
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function compareFolders(aSyncFoldersInput) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/fileSystem/compareFolders",
			params : aSyncFoldersInput
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function copyFiles(aFileCopyParams) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/fileSystem/copyFile",
			data : aFileCopyParams,
			datatype : "json"
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}
};

/**
 * 
 */
apollo.services.tagService = function($http, $q) {
	return ({
		"getCommonTag" : getCommonTag,
		"checkAlbumLocation" : checkAlbumLocation,
		"checkSongLocation" : checkSongLocation
	});

	function getCommonTag(aCommonTag, aMp3Tag) {
		if (aCommonTag.initialized !== "true") {
			aCommonTag.composer = aMp3Tag.composer;
			aCommonTag.artist = aMp3Tag.artist;
			aCommonTag.tags = aMp3Tag.tags;
			aCommonTag.initialized = "true";
			return;
		}

		$.each(aCommonTag, function(aKey, aValue) {
			if (aKey !== "initialized" && aCommonTag[aKey] !== aMp3Tag[aKey]) {
				delete aCommonTag[aKey];
			}
		});
	}

	function checkAlbumLocation(aAlbumTag) {
		var albumPath = apollo.context.configProps.libraryRoot + "/"
				+ aAlbumTag.language + "/" + aAlbumTag.genre + "/"
				+ aAlbumTag.albumArtist + "/" + aAlbumTag.album;

		return (albumPath === aAlbumTag.filePath);
	}

	function checkSongLocation(aSongTag) {
		var songPath = apollo.context.configProps.libraryRoot + "/"
				+ aSongTag.language + "/" + aSongTag.genre + "/"
				+ aSongTag.albumArtist + "/" + aSongTag.album + "/"
				+ aSongTag.title + "." + apollo.context.configProps.fileExtn;

		return (songPath === aSongTag.filePath);
	}
};

/**
 * 
 */
apollo.services.albumService = function($http, $q) {
	return ({
		"getTracks" : getTracks,
		"parseTags" : parseTags,
		"saveParsedTags" : saveParsedTags,
		"saveAlbumTag" : saveAlbumTag,
		"saveTrackTag" : saveTrackTag,
		"saveTrackTags" : saveTrackTags,
		"renameTracks" : renameTracks,
		"addToLibrary" : addToLibrary
	});

	function getTracks(aAlbumPath) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/album/tracks",
			params : {
				"albumPath" : aAlbumPath
			}
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function parseTags(aAlbumPath, aParseTagTokens) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/album/parseTags",
			params : {
				"albumPath" : aAlbumPath,
				"parseTagTokens" : aParseTagTokens
			}
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function saveParsedTags(aAlbumPath, aParseTagTokens) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/album/saveParsedTags",
			params : {
				"albumPath" : aAlbumPath,
				"parseTagTokens" : aParseTagTokens
			}
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function saveAlbumTag(aAlbumTag) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/album/albumTag",
			data : aAlbumTag,
			datatype : "json"
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function saveTrackTag(aTrackTag) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/album/trackTag",
			data : aTrackTag,
			datatype : "json"
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function saveTrackTags(aTrackTagList) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/album/trackTags",
			data : aTrackTagList,
			datatype : "json"
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function renameTracks(aAlbumTag) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/album/renameTracks",
			data : aAlbumTag,
			datatype : "json"
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function addToLibrary(aAlbumTag) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/album/addToLibrary",
			data : aAlbumTag,
			datatype : "json"
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}
};

/**
 * 
 */
apollo.services.songService = function($http, $q) {
	return ({
		"getTag" : getTag,
		"parseTags" : parseTags,
		"saveParsedTags" : saveParsedTags,
		"saveTag" : saveTag,
		"addToLibrary" : addToLibrary
	});

	function getTag(aSongPath) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/song/tag",
			params : {
				"songPath" : aSongPath
			}
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function parseTags(aSongPath, aParseTagTokens) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/song/parseTags",
			params : {
				"songPath" : aSongPath,
				"parseTagTokens" : aParseTagTokens
			}
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function saveParsedTags(aSongPath, aParseTagTokens) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/song/saveParsedTags",
			params : {
				"songPath" : aSongPath,
				"parseTagTokens" : aParseTagTokens
			}
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function saveTag(aTrackTag) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/song/tag",
			data : aTrackTag,
			datatype : "json"
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function addToLibrary(aSongPath) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/song/addToLibrary",
			params : {
				"songPath" : aSongPath
			}
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}
};
