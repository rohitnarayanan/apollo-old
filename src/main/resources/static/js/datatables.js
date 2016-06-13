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
		"dom" : "<'top clearfix'f>rt<'bottom clearfix'ip>",
		"rowId" : "id",
		"autoFill" : {
			"alwaysAsk" : true,
			"columns" : [ 1, 2, 3, 4, 5 ]
		},
		"createdRow" : function(aRow, aData, aDataIndex) {
			aRowDataMap[aData.id] = aData;
		},
		"responsive" : true,
		"columns" : [ {
			"title" : "File Name",
			"data" : "fileName",
			"defaultContent" : ""
		// "mData" : "fileName",
		// "mRender" : function(data, type, full, meta) {
		// return "<span class='glyphicon glyphicon-music' data-toggle='tooltip'
		// data-placement='top' title='"
		// + data + "'/>";
		// }
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
	$("#albumTracksDT tbody").on("click", "tr", function() {
		aRowClickCallback(albumTracksDT.row(this));
	});

	return albumTracksDT;
};