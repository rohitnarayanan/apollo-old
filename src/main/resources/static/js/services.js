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
		"getFileTree" : getFileTree
	});

	function getFileTree(aDirPath, aDirName, aFoldersOnly) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/fileSystem/fileTree",
			params : {
				"dirPath" : aDirPath,
				"dirName" : aDirName,
				"foldersOnly" : aFoldersOnly
			}
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
		"extractCommonTag" : extractCommonTag
	});

	function extractCommonTag(aCommonTag, aMp3Tag) {
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
			url : apollo.context.path + "/song/getTag",
			params : {
				"songPath" : aSongPath
			}
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function parseTags(aAlbumPath, aParseTagTokens) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/song/parseTags",
			params : {
				"songPath" : aAlbumPath,
				"parseTagTokens" : aParseTagTokens
			}
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function saveParsedTags(aAlbumPath, aParseTagTokens) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/song/saveParsedTags",
			params : {
				"songPath" : aAlbumPath,
				"parseTagTokens" : aParseTagTokens
			}
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function saveTag(aTrackTag) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/song/saveTag",
			data : aTrackTag,
			datatype : "json"
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function addToLibrary(aAlbumTag) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/song/addToLibrary",
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
apollo.services.albumService = function($http, $q) {
	return ({
		"getTracks" : getTracks,
		"parseTags" : parseTags,
		"saveParsedTags" : saveParsedTags,
		"saveAlbumTag" : saveAlbumTag,
		"saveTrackTag" : saveTrackTag,
		"saveTrackTags" : saveTrackTags,
		"addToLibrary" : addToLibrary
	});

	function getTracks(aAlbumPath) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/album/listTracks",
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
			url : apollo.context.path + "/album/saveAlbumTag",
			data : aAlbumTag,
			datatype : "json"
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function saveTrackTag(aTrackTag) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/album/saveTrackTag",
			data : aTrackTag,
			datatype : "json"
		});

		return (request.then(apollo.plugins.AngularUtil.httpSuccess,
				apollo.plugins.AngularUtil.httpError));
	}

	function saveTrackTags(aTrackTagList) {
		var request = $http({
			method : "post",
			url : apollo.context.path + "/album/saveTrackTags",
			data : aTrackTagList,
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
