/*
 * Configuration for datatables
 */
apollo.datatables = {};

apollo.datatables.utils = {
	"createText" : function(aName, aValue) {
		return "<input id='" + aName
				+ "' type='text' class='form-control' value='" + aValue
				+ "' />";
	}
};

apollo.datatables.albumTracksDT = function() {
	var rowDataMap = {};

	var dtConfig = {
		"order" : [ [ 4, "asc" ] ],
		"pageLength" : 6,
		"dom" : "<'top clearfix'f><'table-responsive't><'bottom clearfix'ip>",
		"select" : true,
		"rowId" : "filePath",
		"createdRow" : function(aRow, aData, aDataIndex) {
			rowDataMap[aData.filePath] = aData;
		},
		"columns" : [ {
			"title" : "File Name",
			"data" : "fileName",
			"defaultContent" : ""
		}, {
			"title" : "Title",
			"data" : "title",
			"defaultContent" : ""
		}, {
			"title" : "Artist",
			"data" : "artist",
			"defaultContent" : ""
		}, {
			"title" : "Composer",
			"data" : "composer",
			"defaultContent" : ""
		}, {
			"title" : "Track Nbr",
			"className" : "text-center",
			"data" : "trackNbr",
			"defaultContent" : ""
		}, {
			"title" : "Tags",
			"data" : "tags",
			"defaultContent" : ""
		} ]
	}

	var albumTracksDT = $("#albumTracksDT").DataTable(dtConfig);
	albumTracksDT.rowDataMap = rowDataMap;
	return albumTracksDT;
};

apollo.datatables.missingFilesDT = function(aTableId) {
	var dtConfig = {
		"order" : [ [ 0, "asc" ] ],
		"pageLength" : 10,
		"dom" : "<'top clearfix'f><'table-responsive't><'bottom clearfix'ip>",
		"select" : true,
		"rowId" : "filePath",
		"columns" : [ {
			"title" : "Name",
			"data" : "name",
			"defaultContent" : ""
		}, {
			"title" : "Type",
			"data" : "type",
			"defaultContent" : ""
		}, {
			"title" : "Size",
			"data" : "size",
			"defaultContent" : ""
		}, {
			"title" : "Last Modified",
			"data" : "lastModified",
			"defaultContent" : ""
		} ]
	}

	var missingFilesDT = $("#" + aTableId).DataTable(dtConfig);
	return missingFilesDT;
};

apollo.datatables.conflictingFilesDT = function(aTableId) {
	var dtConfig = {
		"order" : [ [ 0, "asc" ] ],
		"pageLength" : 10,
		"dom" : "<'top clearfix'f><'table-responsive't><'bottom clearfix'ip>",
		"select" : true,
		"rowId" : "filePath",
		"columns" : [ {
			"title" : "Path",
			"data" : "key",
			"defaultContent" : ""
		}, {
			"title" : "Type",
			"data" : "type",
			"defaultContent" : ""
		}, {
			"title" : "Source Size",
			"data" : "source.size",
			"defaultContent" : ""
		}, {
			"title" : "Source Last Modified",
			"data" : "source.lastModified",
			"defaultContent" : ""
		}, {
			"title" : "Target Size",
			"data" : "target.size",
			"defaultContent" : ""
		}, {
			"title" : "Target Last Modified",
			"data" : "target.lastModified",
			"defaultContent" : ""
		} ]
	}

	var conflictingFilesDT = $("#" + aTableId).DataTable(dtConfig);
	return conflictingFilesDT;
};