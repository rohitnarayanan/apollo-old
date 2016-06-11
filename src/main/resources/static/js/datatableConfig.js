/*
 * Configuration for datatables
 */

apollo.dataTableConfig = {
	"albumTracksDT" : {
		"order" : [ [ 6, "asc" ] ],
		"pageLength" : 15,
		"dom" : "<'top'f>rt<'bottom'ip>",
		"rowId" : "id",
		autoFill : true,
		responsive : true,
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