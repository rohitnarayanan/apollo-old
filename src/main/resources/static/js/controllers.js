'use strict';

/*
 * Controllers
 */
apollo.controllers = {};

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

/**
 * mainController
 */
apollo.controllers.mainController = function($scope, $rootScope, $route,
		$location, $http, $window) {
	$rootScope.pageHeader = "This is Apollo";
	$rootScope.pageContentText = "This application provides a set of utilities to manage your music library";

	$scope.logout = function() {
		$("#_logoutForm").submit();
		// $http.post(global_scope.context_path + "/logout");
		// window.location.href = $window.location.origin
		// + global_scope.context_path + "/login?logout";
	}
}

/**
 * editTagsController
 */
apollo.controllers.editTagsController = function($rootScope, $scope,
		utilService) {
	$rootScope.pageHeader = "Edit Tags";
	$rootScope.pageContentText = "Choose the directory in which the files are to be edited";
	$scope.firstCall = true;

	$scope.getFolders = function() {
		var selectedFolder = $("input[name='folderSelect']:checked");
		var dirPath = $scope.firstCall ? "" : $scope.dirPath;
		var dirName = $scope.firstCall ? "" : selectedFolder.val();

		var childFolders = selectedFolder.data("childFolders");
		if (!$scope.firstCall && !childFolders) {
			alert("No child folders available !");
			return;
		}

		$scope.firstCall = false;
		utilService
				.listFolders(dirPath, dirName)
				.then(
						function(aResponse) {
							// if (aResponse.returnCode != 0) {
							// apollo.plugins.angularUtils
							// .serverError(aResponse);
							// return;
							// }

							$scope.dirPath = aResponse.dirPath;

							if (!$.fn.DataTable.isDataTable('#folderListDT')) {
								var dataTableOptions = {
									"order" : [ [ 0, "asc" ] ],
									"pageLength" : 15,
									dom : '<"top"f>rt<"bottom"ip>',
									rowId : 'id',
									columns : [
											{
												"title" : "Select",
												"orderable" : false,
												"mRender" : function(data,
														type, full, meta) {
													return "<input type='radio' name='folderSelect' value='"
															+ full.name
															+ "' data-child-folders='"
															+ full.childFolders
															+ "'/>";
												}
											}, {
												"title" : "Name",
												"data" : "name",
												"defaultContent" : ""
											} ]
								};

								$scope.dataTable = $('#folderListDT')
										.DataTable(dataTableOptions);
							}

							$scope.dataTable.clear();
							$scope.dataTable.rows.add(aResponse.folders);
							$scope.dataTable.columns.adjust().draw();

						}, apollo.plugins.angularUtils.serverError);
	};

	$scope.getTracks = function() {
		var selectedFolder = $("input[name='folderSelect']:checked");
		var dirPath = $scope.firstCall ? "" : $scope.dirPath;
		var dirName = $scope.firstCall ? "" : selectedFolder.val();

		var childFolders = selectedFolder.data("childFolders");
		if (!$scope.firstCall && !childFolders) {
			alert("No child folders available !");
			return;
		}

		$scope.firstCall = false;
		utilService
				.listFolders(dirPath, dirName)
				.then(
						function(aResponse) {
							// if (aResponse.returnCode != 0) {
							// apollo.plugins.angularUtils
							// .serverError(aResponse);
							// return;
							// }

							$scope.dirPath = aResponse.dirPath;

							if (!$.fn.DataTable.isDataTable('#folderListDT')) {
								var dataTableOptions = {
									"order" : [ [ 0, "asc" ] ],
									"pageLength" : 15,
									dom : '<"top"f>rt<"bottom"ip>',
									rowId : 'id',
									columns : [
											{
												"title" : "Select",
												"orderable" : false,
												"mRender" : function(data,
														type, full, meta) {
													return "<input type='radio' value='"
															+ full.name
															+ "' data-child-folders='"
															+ full.childFolders
															+ "'/>";
												}
											}, {
												"title" : "Name",
												"data" : "name",
												"defaultContent" : ""
											} ]
								};

								$scope.dataTable = $('#trackListDT').DataTable(
										dataTableOptions);
							}

							$scope.dataTable.clear();
							$scope.dataTable.rows.add(aResponse.folders);
							$scope.dataTable.columns.adjust().draw();

							$("#_foldersDiv").hide();
							$("#_tagsDiv").show();
						}, apollo.plugins.angularUtils.serverError);
	};

	$scope.getFolders();
}
