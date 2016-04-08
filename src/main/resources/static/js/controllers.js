'use strict';

/*
 * Controllers
 */
apollo.controllers = {};

/**
 * mainController
 */
apollo.controllers.mainController = function($scope, $rootScope, $route,
		$location, $http, $window) {
	$rootScope.pageHeader = "This is Apollo";
	$rootScope.pageContentText = "This application provides a set of utilities to manage your music library";

	$scope.logout = function() {
		$http.post(global_scope.context_path + "/logout");
		window.location.href = $window.location.origin
				+ global_scope.context_path + "/login?logout";
	}
}

/**
 * errorController
 * 
 * @param $scope
 * @param $$routeParams
 */
apollo.controllers.errorController = function($scope, $routeParams) {
	var _statusCode = $routeParams.statusCode;

	if (_statusCode.match(/^(403|404)$/)) {
		$scope.statusCode = _statusCode;
	} else {
		$scope.statusCode = "ALL";
	}
}
