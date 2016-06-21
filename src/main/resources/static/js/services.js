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
		"saveParsedTags" : saveParsedTags,
		"saveTrackTag" : saveTrackTag,
	});

	function fetchTracks(aAlbumPath) {
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
};

/**
 * 
 */
apollo.services.tagService = function($http, $q) {
	return ({
		"getCommonTag" : getCommonTag
	});

	function getCommonTag(aMP3TagList) {
		var commonTag = {};

		$.each(aMP3TagList, function(aIdx, aMP3Tag) {
			if (aIdx === 0) {
				commonTag.language = aMp3Tag.language;
				commonTag.genre = aMp3Tag.genre;
				commonTag.mood = aMp3Tag.mood;
				commonTag.album = aMp3Tag.album;
				commonTag.year = aMp3Tag.year;
				commonTag.albumArtist = aMp3Tag.albumArtist;
				commonTag.composer = aMp3Tag.composer;
				commonTag.artist = aMp3Tag.artist;
				commonTag.tags = aMp3Tag.tags;
				return;
			}

			$.each(commonTag, function(aKey, aValue) {
				if (commonTag[aKey] !== aMp3Tag[aKey]) {
					commonTag[aKey] = "";
				}
			});
		});

		return commonTag;
	}
};