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
		"listFolders" : listFolders
	});

	function listFolders(aDirPath, aDirName) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/fileSystem/folders",
			params : {
				"dirPath" : aDirPath,
				"dirName" : aDirName
			}
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
		"fetchTracks" : fetchTracks,
		"parseTags" : parseTags,
		"saveAlbumTag" : saveAlbumTag,
		"saveTrackTag" : saveTrackTag,
	});

	function fetchTracks(aAlbumPath) {
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
};