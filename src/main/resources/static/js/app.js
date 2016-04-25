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
		var context_path = apollo.context.path;

		aRouteProvider.when('/error/:statusCode', {
			templateUrl : context_path + "/errorPage/view",
			controller : 'errorController'
		}).when('/main', {
			templateUrl : context_path + "/main",
			controller : 'mainController'
		}).when('/editTags', {
			templateUrl : context_path + "/util/editTags",
			controller : 'editTagsController'
		}).otherwise({
			redirectTo : '/main'
		});
	}
}
