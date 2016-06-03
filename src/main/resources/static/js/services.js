'use strict';

/*
 * Services
 */
apollo.services = {};

apollo.services.playlistService = function playlistService($http, $q) {
	return ({
		"find" : findPlaylist,
		"create" : createPlaylist,
		"update" : updatePlaylist,
		"delete" : deletePlaylist,
		"export" : exportPlaylist
	});

	function findPlaylist() {

	}

	function createPlaylist() {

	}

	function updatePlaylist() {

	}

	function deletePlaylist() {

	}

	function exportPlaylist() {

	}
};

/**
 * 
 */
apollo.services.utilService = function utilService($http, $q) {
	return ({
		"listFolders" : listFolders,
		"listTracks" : listTracks
	});

	function listFolders(aDirPath, aDirName) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/util/listFolders",
			params : {
				"dirPath" : aDirPath,
				"dirName" : aDirName
			}
		});

		return (request.then(apollo.plugins.angularUtils.httpSuccess,
				_handleError));
	}

	function listTracks(aDirPath, aDirName) {
		var request = $http({
			method : "get",
			url : apollo.context.path + "/util/listTracks",
			params : {
				"dirPath" : aDirPath,
				"dirName" : aDirName
			}
		});

		return (request.then(apollo.plugins.angularUtils.httpSuccess,
				_handleError));
	}

	/**
	 * internal error handler
	 */
	function _handleError(aResponse) {
		return apollo.plugins.angularUtils.httpError(aResponse, $q);
	}
};