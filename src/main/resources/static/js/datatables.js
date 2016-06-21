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

apollo.datatables.albumTracksDT = function(aRowDataMap, aRowClickCallback) {
	var dtConfig = {
		"order" : [ [ 1, "asc" ] ],
		"pageLength" : 6,
		"dom" : "<'top clearfix'f><'table-responsive't><'bottom clearfix'ip>",
		"rowId" : "filePath",
		"createdRow" : function(aRow, aData, aDataIndex) {
			aRowDataMap[aData.filePath] = aData;
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
			"data" : "trackNbr",
			"defaultContent" : ""
		}, {
			"title" : "Tags",
			"data" : "tags",
			"defaultContent" : ""
		} ]
	}

	var albumTracksDT = $("#albumTracksDT").DataTable(dtConfig);
	$("#albumTracksDT tbody").on("click", "tr", function(aEvent) {
		aRowClickCallback(albumTracksDT.row(this));
	});

	return albumTracksDT;
};