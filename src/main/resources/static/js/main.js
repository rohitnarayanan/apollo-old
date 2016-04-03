/**
 * Function to remove class by wildcard
 */
$.fn.removeClassRegex = function(aRegExp) {
	return $(this).removeClass(function(index, classes) {
		return classes.split(/\s+/).filter(function(c) {
			return aRegExp.test(c);
		}).join(" ");
	});
};

function fn_ShowPageAlert(aMessage, aMessageClass) {
	$('#_alertMessage').empty().text(aMessage).removeClassRegex(/^alert-/)
			.addClass(aMessageClass).show().get(0).scrollIntoView();
}

function fn_HidePageAlert() {
	$('#_alertMessage').hide();
}

function fn_ShowModalAlert(aMessage, aMessageClass) {
	$('#_modalAlert').empty().text(aMessage).removeClassRegex(/^alert-/)
			.addClass(aMessageClass).show().get(0).scrollIntoView();
}

function fn_HideModalAlert() {
	$('#_modalAlert').hide();
}

function fn_HTMLEscapeText(aText) {
	return aText.replace('\'', '&apos;').replace('\"', '&quot;');
}

function fn_IsEmpty(aVal) {
	if (!aVal || aVal.trim().length == 0) {
		return true;
	}

	return false;
}
