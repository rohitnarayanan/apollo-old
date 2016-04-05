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
 * Attach Controllers
 */
apollo.module.controller('mainController', apollo.controllers.mainController);
apollo.module.controller('errorController', apollo.controllers.errorController);

/*
 * Apollo Configuration
 */
apollo.config = {
	"configureHttpProvider" : function(aHttpProvider) {
		// initialize get if not there
		if (!aHttpProvider.defaults.headers.get) {
			aHttpProvider.defaults.headers.get = {};
		}

		aHttpProvider.interceptors.push(function($q, $location, $window) {
			return {
				"request" : function(config) {
					config.headers["ajaxRequest"] = "true";
					return config;
				},
				"requestError" : function(rejection) {
					return rejection;
				},
				"response" : function(response) {
					return response;
				},
				"responseError" : function(rejection) {
					if (rejection.status == 401) {
						var landingUrl = $window.location.origin
								+ global_scope.context_path
								+ "/login?sessionExpired";
						window.location.href = landingUrl;
					} else {
						$location.path("/error/" + rejection.status);
					}
					return $q.reject(rejection);
				}
			}
		});
	},

	"configureRouteProvider" : function(aRouteProvider) {
		aRouteProvider.when('/error/:statusCode', {
			templateUrl : "/apollo/errorview",
			controller : 'errorController'
		}).when('/test', {
			templateUrl : "/apollo/test",
			controller : 'mainController'
		}).otherwise({
			redirectTo : '/test'
		});
	}
}
