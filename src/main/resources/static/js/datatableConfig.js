/*
 * Configuration for datatables
 */

apollo.dataTableConfig = {
	"albumTracksDT" : {
		"order" : [ [ 6, "asc" ] ],
		"pageLength" : 6,
		"dom" : "<'top clearfix'Bf>rt<'bottom clearfix'ip>",
		"rowId" : "id",
		"autoFill" : {
			"alwaysAsk" : true,
			"columns" : [ 3, 4, 5, 7, 9, 10, 11, 12, 13 ]
		},
		"buttons" : [ {
			extend : 'collection',
			className : 'btn btn-warning btn-responsive',
			text : 'Table control',
			buttons : [ {
				className : 'btn btn-warning btn-responsive',
				text : 'Toggle start date',
				action : function(e, dt, node, config) {
					dt.column(-2).visible(!dt.column(-2).visible());
				}
			}, {
				className : 'btn btn-warning btn-responsive',
				text : 'Toggle salary',
				action : function(e, dt, node, config) {
					dt.column(-1).visible(!dt.column(-1).visible());
				}
			} ]
		} ],
		"select" : true,
		"keys" : true,
		"responsive" : true,
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
			"title" : "Lyrics",
			"data" : "album",
			"defaultContent" : ""
		}, {
			"title" : "Album",
			"data" : "album",
			"defaultContent" : ""
		}, {
			"title" : "Track Nbr",
			"data" : "trackNbr",
			"defaultContent" : ""
		}, {
			"title" : "Year",
			"data" : "year",
			"defaultContent" : ""
		}, {
			"title" : "Album Artist",
			"data" : "albumArtist",
			"defaultContent" : ""
		}, {
			"title" : "Genre",
			"data" : "genre",
			"defaultContent" : ""
		}, {
			"title" : "Mood",
			"data" : "mood",
			"defaultContent" : ""
		}, {
			"title" : "Language",
			"data" : "language",
			"defaultContent" : ""
		}, {
			"title" : "Tags",
			"data" : "tags",
			"defaultContent" : ""
		} ]
	}
}