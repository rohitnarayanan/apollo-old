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
	$rootScope.pageHeader = "Hello";
	$rootScope.pageContentText = "Okie Dokie";

	$scope.logout = function() {
		$http.post(global_scope.context_path + "/logout");
		window.location.href = $window.location.origin
				+ global_scope.context_path + "/login?logout";
	}

	// position links
	var allowedWidth = $("#apolloLinksDiv").width();
	$("div.apollo-submenu").each(function() {
		var _width = $(this).width();
		var _left = $(this).parent().position().left;
		if ((_left + _width) >= allowedWidth) {
			_left -= (_left + _width - allowedWidth);

		}

		$(this).css("left", _left);
	});
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
