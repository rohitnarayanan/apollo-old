'use strict';

/*
 * Module Declaration
 */
apollo.module = angular.module('apollo', [ 'ngRoute' ]);

/*
 * Module Configuration
 */
apollo.module.config([ '$routeProvider', '$httpProvider',
		function($routeProvider, $httpProvider) {
			apollo.config.configureHttpProvider($httpProvider);
			apollo.config.configureRouteProvider($routeProvider);
		} ]);

/*
 * Register Controllers
 */
$.each(apollo.controllers, function(aKey, aFunction) {
	apollo.module.controller(aKey, aFunction);
});

/*
 * Register Services
 */
$.each(apollo.services, function(aKey, aFunction) {
	apollo.module.service(aKey, aFunction);
});

/*
 * Apollo Configuration
 */
apollo.config = {
	"configureHttpProvider" : function(aHttpProvider) {
		// initialize get if not there
		if (!aHttpProvider.defaults.headers.get) {
			aHttpProvider.defaults.headers.get = {};
		}

		aHttpProvider.interceptors.push(function($q, $location, $rootScope) {
			return {
				"request" : function(config) {
					config.headers["AJAX-REQUEST"] = "true";
					return config;
				},
				"requestError" : function(rejection) {
					return rejection;
				},
				"response" : function(aResponse) {
					return aResponse;
				},
				"responseError" : function(aResponse) {
					if (aResponse.status == 401 || aResponse.status == -1) {
						$rootScope.logout();
					} else {
						$location.path("/error/" + aResponse.status);
					}

					return $q.reject(aResponse);
				}
			}
		});
	},

	"configureRouteProvider" : function(aRouteProvider) {
		var context_path = apollo.context.path;

		aRouteProvider.when('/error/:statusCode', {
			templateUrl : context_path + "/errorPage/view",
			controller : 'errorController'
		}).when('/main', {
			templateUrl : context_path + "/view/main",
			controller : 'mainController'
		}).when('/addAlbum', {
			templateUrl : context_path + "/view/manageAlbum",
			controller : 'addAlbumController'
		}).when('/addPlaylist', {
			templateUrl : context_path + "/view/managePlaylist",
			controller : 'addPlaylistController'
		}).when('/addSong', {
			templateUrl : context_path + "/view/manageSong",
			controller : 'addSongController'
		}).when('/browseAlbums', {
			templateUrl : context_path + "/view/browseAlbums",
			controller : 'browseAlbumsController'
		}).when('/browsePlaylists', {
			templateUrl : context_path + "/view/browsePlaylists",
			controller : 'browsePlaylistsController'
		}).when('/browseArtists', {
			templateUrl : context_path + "/view/browseArtists",
			controller : 'browseArtistsController'
		}).when('/browseSongs', {
			templateUrl : context_path + "/view/browseSongs",
			controller : 'browseSongsController'
		}).otherwise({
			redirectTo : '/main'
		});
	}
}
