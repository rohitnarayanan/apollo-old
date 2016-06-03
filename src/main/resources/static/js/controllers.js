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
apollo.controllers.mainController = function($rootScope, $scope, $http, $q,
		$window) {
	$rootScope.pageHeader = "This is Apollo";
	$rootScope.pageContentText = "This application provides a set of utilities to manage your music library";
	$rootScope.context = apollo.context;
	apollo.angularSvc = {};
	apollo.angularSvc.http = $http;
	apollo.angularSvc.q = $q;
	apollo.angularSvc.window = $window;

	$scope.logout = function() {
		$("#_logoutForm").submit();
	}
}

/**
 * addTracksController
 */
apollo.controllers.addTracksController = function($rootScope, $scope,
		utilService) {
	$rootScope.pageHeader = "Add Tracks";
	$rootScope.pageContentText = "Choose the directory from which you want to add tracks";
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
							$scope.dirPath = aResponse.model.dirPath;

							if (!$.fn.DataTable.isDataTable('#folderListDT')) {
								var dataTableOptions = {
									"order" : [ [ 1, "asc" ] ],
									"pageLength" : 15,
									dom : '<"top"f>rt<"bottom"ip>',
									rowId : 'id',
									buttons : [ {
										text : 'Reload',
										action : function(e, dt, node, config) {
											dt.ajax.reload();
										}
									} ],
									columns : [
											{
												"title" : "Select",
												"orderable" : false,
												"mRender" : function(data,
														type, full, meta) {
													return "<input type='radio' name='folderSelect' value='"
															+ full.model.name
															+ "' data-child-folders='"
															+ full.model.childFolders
															+ "'/>";
												}
											}, {
												"title" : "Name",
												"data" : "model.name",
												"defaultContent" : ""
											} ]
								};

								$scope.folderListDT = $('#folderListDT')
										.DataTable(dataTableOptions);
							}

							$scope.folderListDT.clear();
							$scope.folderListDT.rows
									.add(aResponse.model.folders);
							$scope.folderListDT.columns.adjust().draw();

							$scope.folderMode = true;
							$("#folderListDT").css("width", "100%");
						}, apollo.plugins.angularUtils.serverError);
	};

	$scope.getTracks = function() {
		var selectedFolder = $("input[name='folderSelect']:checked");
		var dirPath = $scope.firstCall ? "" : $scope.dirPath;
		var dirName = $scope.firstCall ? "" : selectedFolder.val();

		$scope.firstCall = false;
		utilService
				.listTracks(dirPath, dirName)
				.then(
						function(aResponse) {
							$scope.dirPath = aResponse.dirPath;

							if (!$.fn.DataTable.isDataTable('#trackListDT')) {
								var dataTableOptions = {
									"order" : [ [ 6, "asc" ] ],
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
															+ full.sourceFile
															+ "'/>";
												}
											}, {
												"title" : "Language",
												"data" : "language",
												"defaultContent" : ""
											}, {
												"title" : "Genre",
												"data" : "genre",
												"defaultContent" : ""
											}, {
												"title" : "Album",
												"data" : "album",
												"defaultContent" : ""
											}, {
												"title" : "Year",
												"data" : "year",
												"defaultContent" : ""
											}, {
												"title" : "Artist",
												"data" : "artist",
												"defaultContent" : ""
											}, {
												"title" : "Title",
												"data" : "title",
												"defaultContent" : ""
											}, {
												"title" : "Track Nbr",
												"data" : "trackNbr",
												"defaultContent" : ""
											} ]
								};

								$scope.trackListDT = $('#trackListDT')
										.DataTable(dataTableOptions);
							}

							$scope.trackListDT.clear();
							$scope.trackListDT.rows.add(aResponse.model.tracks);
							$scope.trackListDT.columns.adjust().draw();

							$scope.folderMode = false;
							$("#trackListDT").css("width", "100%");
						}, apollo.plugins.angularUtils.serverError);
	};

	$scope.getFolders();
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
							$scope.dirPath = aResponse.model.dirPath;

							if (!$.fn.DataTable.isDataTable('#folderListDT')) {
								var dataTableOptions = {
									"order" : [ [ 1, "asc" ] ],
									"pageLength" : 15,
									dom : '<"top"Bf>rt<"bottom"ip>',
									rowId : 'id',
									buttons : [ {
										text : 'Reload',
										action : function(e, dt, node, config) {
											alert("Hi");
										}
									} ],
									columns : [
											{
												"title" : "Select",
												"orderable" : false,
												"mRender" : function(data,
														type, full, meta) {
													return "<input type='radio' name='folderSelect' value='"
															+ full.model.name
															+ "' data-child-folders='"
															+ full.model.childFolders
															+ "'/>";
												}
											}, {
												"title" : "Name",
												"data" : "model.name",
												"defaultContent" : ""
											} ]
								};

								$scope.folderListDT = $('#folderListDT')
										.DataTable(dataTableOptions);
							}

							$scope.folderListDT.clear();
							$scope.folderListDT.rows
									.add(aResponse.model.folders);
							$scope.folderListDT.columns.adjust().draw();

							$scope.folderMode = true;
							$("#folderListDT").css("width", "100%");
						}, apollo.plugins.angularUtils.serverError);
	};

	$scope.getTracks = function() {
		var selectedFolder = $("input[name='folderSelect']:checked");
		var dirPath = $scope.firstCall ? "" : $scope.dirPath;
		var dirName = $scope.firstCall ? "" : selectedFolder.val();

		$scope.firstCall = false;
		utilService
				.listTracks(dirPath, dirName)
				.then(
						function(aResponse) {
							$scope.dirPath = aResponse.dirPath;

							if (!$.fn.DataTable.isDataTable('#trackListDT')) {
								var dataTableOptions = {
									"order" : [ [ 6, "asc" ] ],
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
															+ full.sourceFile
															+ "'/>";
												}
											}, {
												"title" : "Language",
												"data" : "language",
												"defaultContent" : ""
											}, {
												"title" : "Genre",
												"data" : "genre",
												"defaultContent" : ""
											}, {
												"title" : "Album",
												"data" : "album",
												"defaultContent" : ""
											}, {
												"title" : "Year",
												"data" : "year",
												"defaultContent" : ""
											}, {
												"title" : "Artist",
												"data" : "artist",
												"defaultContent" : ""
											}, {
												"title" : "Title",
												"data" : "title",
												"defaultContent" : ""
											}, {
												"title" : "Track Nbr",
												"data" : "trackNbr",
												"defaultContent" : ""
											} ]
								};

								$scope.trackListDT = $('#trackListDT')
										.DataTable(dataTableOptions);
							}

							$scope.trackListDT.clear();
							$scope.trackListDT.rows.add(aResponse.model.tracks);
							$scope.trackListDT.columns.adjust().draw();

							$scope.folderMode = false;
							$("#trackListDT").css("width", "100%");
						}, apollo.plugins.angularUtils.serverError);
	};

	$scope.getFolders();
}
