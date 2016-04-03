'use strict';

/*
 * Module Declaration
 */
var apollo = angular.module('apollo', [ 'ngRoute' ]);

/*
 * Module Configuration
 */
apollo.config([ '$routeProvider', '$httpProvider',
		function($routeProvider, $httpProvider) {
			fn_ConfigureHttpProvider($httpProvider);

			$routeProvider.when('/error/:statusCode', {
				templateUrl : "/apollo/errorview",
				controller : 'errorController'
			}).when('/test', {
				templateUrl : "/apollo/test",
				controller : 'apolloController'
			}).otherwise({
				redirectTo : '/test'
			});
		} ]);

/*
 * Attach Controllers
 */
apollo.controller('apolloController', apolloController);
apollo.controller('errorController', errorController);

/**
 * Controller Instance
 * 
 * @param $scope
 * @param $rootScope
 * @param $location
 */
function apolloController($scope, $rootScope, $route, $location, $http, $window) {
	$rootScope.pageHeader = "Hello";
	$rootScope.pageContentText = "Okie Dokie";

	$scope.logout = function() {
		$http.post(global_scope.context_path + "/logout");
		window.location.href = $window.location.origin
				+ global_scope.context_path + "/login?logout";
	}
}

/**
 * Controller Instance
 * 
 * @param $scope
 * @param $$routeParams
 */
function errorController($scope, $routeParams) {
	var _statusCode = $routeParams.statusCode;

	if (_statusCode.match(/^(403|404)$/)) {
		$scope.statusCode = _statusCode;
	} else {
		$scope.statusCode = "ALL";
	}
}

/**
 * Function to handle view loaded event
 */
function fn_ConfigureHttpProvider(aHttpProvider) {
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
}