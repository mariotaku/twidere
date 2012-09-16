function parseForm(event) {
	var form = this;
	// make sure form points to the surrounding form object if a custom button
	// was used
	if (this.tagName.toLowerCase() != 'form')
		form = this.form;
	if (!form.method)
		form.method = 'get';
	var inputs = document.forms[0].getElementsByTagName('input');
	var values = new Array();
	var j = 0;
	for ( var i = 0; i < inputs.length; i++) {
		var field = inputs[i];
		if (field.type != 'submit' && field.type != 'reset' && field.type != 'button') {
			values[j] = field.name + '=' + field.value;
			j++;
		}
	}
	window.form_inject.processFormData(form.method, form.action, values);
}

for ( var form_idx = 0; form_idx < document.forms.length; ++form_idx)
	document.forms[form_idx].addEventListener('submit', parseForm, false);
var inputs = document.getElementsByTagName('input');
for ( var i = 0; i < inputs.length; i++) {
	if (inputs[i].getAttribute('type') == 'button')
		inputs[i].addEventListener('click', parseForm, false);
}
