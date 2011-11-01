function showFacility(element) {
    if(!$('#facilities')) {
        return;
    }
    if(element.find('option:selected').attr('parent') != 'select') {
        $('#facilitiesList').parent().show();
         $('#facilitiesList').html($('#facilitiesList :first-child'));
        var filteredFacilities = $('#facilities').find('option').filter(function() {
            return ($(this).attr('parent') == element.find('option:selected').text());
        });

        $('#facilitiesList').append(filteredFacilities);
    }
}

function hideFacility() {
    if($('#facilities')) {
        $('#facilities').parent().hide();
    }
}

var Field = function(elementId) {
    this.id = elementId;
    this.node = $('#' + this.id);
    this.alertNode = $('#' + this.id + 'Error');
    this.options = this.node.find('option');
}

Field.prototype.hideAlert = function() {
    this.alertNode.hide();
}

Field.prototype.hasADependent = function(dependent) {
    this.dependent = dependent;
    var field = this;
    this.node.change(function() {
        field.hideAlert();
        field.populateDependentWithOriginalValues();
        field.showOrHideDependsBasedOnSelection();
    });
    return this;
}

Field.prototype.populateDependentWithOriginalValues = function() {
    this.dependent.node.html(this.dependent.options);
}

Field.prototype.showOrHideDependsBasedOnSelection = function() {
    var field = this;

    function getDependentOptionsForSelectedValue() {
        return field.dependent.node.find('option').filter(function() {
            return $(this).attr('parent') == field.node.find('option:selected').text() && $(this).text() != '';
        });
    }

    var dependentOptions = getDependentOptionsForSelectedValue();
    hideFacility();
    if (dependentOptions.length > 0) {
        this.showDependent(dependentOptions);
    } else {
        this.hideDependents();
        showFacility(field.node);
    }
}

Field.prototype.hasDependent = function() {
    return this.dependent != undefined;
}

Field.prototype.hideDependents = function() {
    var field = this;
    while (field.hasDependent()) {
        var field = field.dependent;
        field.node.parent().hide();
    }
}

Field.prototype.getDefaultValue = function() {
    return this.node.find('option[parent=select]');
}

Field.prototype.showDependent = function(dependentOptions) {
    if (this.hasDependent()) {
        this.dependent.hideAlert();
        this.dependent.hideDependents();
        var dependentDefaultOption = this.dependent.getDefaultValue();
        this.dependent.node.html(dependentOptions);
        this.dependent.node.prepend(dependentDefaultOption);
        this.dependent.node.find('option:first').attr('selected', 'selected');
        this.dependent.node.parent().show();
    }
}