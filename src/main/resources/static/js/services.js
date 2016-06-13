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
		"fetchTracks" : fetchTracks
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
};