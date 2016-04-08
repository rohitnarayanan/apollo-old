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