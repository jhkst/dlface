// todo: use mustache, or other
// see http://stackoverflow.com/questions/18673860/defining-a-html-template-to-append-using-jquery

var DLActions = {
    'captcha': function(parent, data) {
        var id = data.id;
        var captchaImage = data.captchaImage;

        var template =
        '<div class="thumbnail">' +
             '<img src="' + captchaImage + '" style="max-width: 100%; height: auto;" border="0" id="cimg-' + id + '"/>' +
             '<div class="caption">' +
                 '<div class="input-group">' +
                     '<input type="text" class="form-control" id="val-' + id + '" placeholder="Enter value"/>' +
                     '<span class="input-group-btn">' +
                         '<button class="btn btn-default" id="sub-' + id + '" type="button">Submit</button>' +
                     '</span>' +
                 '</div>' +
             '</div>' +
         '</div>';

        $(template).appendTo(parent);

        $("#cimg-" + id).popover({
            html: true,
            trigger: 'hover',
            placement: 'auto left',
            content: function() {
                return '<div><img src="' + $(this).attr('src') + '" border="5"/></div>';
            }
        }).on("show.bs.popover", function() {
            $(this).data("bs.popover").tip().css("max-width", "600px");
        });

        $("#sub-" + id).click(function() {
           this.disabled = true;
           sendActionResponseData({
               "id": id,
               "value": $("#val-" + id).val()
           });
        });
    },
    'account': function(parent, data) {
        var id = data.id;
        var text = data.text;

        var template =
        '<div class="thumbnail">' +
             '<div class="caption">' +
                 '<div>' + text + '</div>' +
                 '<input type="text" class="form-control" id="user-' + id + '" placeholder="Username"/>' +
                 '<input type="text" class="form-control" id="pass-' + id + '" placeholder="Password"/>' +
                 '<span class="input-group-btn">' +
                     '<button class="btn btn-default" id="sub-' + id + '" type="button">Submit</button>' +
                 '</span>' +
             '</div>' +
         '</div>';

        $(template).appendTo(parent);

        $("#sub-" + id).click(function() {
           this.disabled = true;
           sendActionResponseData({
               "id": id,
               "user": $("#user-" + id).val(),
               "password": $("#pass-" + id).val()
           });
        });
    },
    'ok': function(parent, data) {
        var id = data.id;
        var text = data.text;

        var template =
        '<div class="thumbnail">' +
             '<div class="caption">' +
                 '<div class="input-group">' +
                     '<div>' + text + '</div>' +
                     '<button class="btn btn-default" id="sub-' + id + '" type="button">OK</button>' +
                 '</div>' +
             '</div>' +
        '</div>';

        $(template).appendTo(parent);

        $("#sub-" + id).click(function() {
            this.disabled = true;
            sendActionResponseData({
                "id": id
            });
        });
    },
    'okcancel': function(parent, data) {
        var id = data.id;
        var text = data.text;

        var template =
        '<div class="thumbnail">' +
             '<div class="caption">' +
                 '<div class="input-group">' +
                     '<div>' + text + '</div>' +
                     '<div class="btn-group" role="group">' +
                         '<button class="btn btn-default" id="ok-' + id + '" type="button">OK</button>' +
                         '<button class="btn btn-default" id="cancel-' + id + '" type="button">Cancel</button>' +
                     '</div>' +
                 '</div>' +
             '</div>' +
        '</div>';

        $(template).appendTo(parent);

        $("#ok-" + id).click(function() {
            this.disabled = true;
            sendActionResponseData({
                "id": id,
                "confirmed": true
            });
        });
        $("#cancel-" + id).click(function() {
            this.disabled = true;
            sendActionResponseData({
                "id": id,
                "confirmed": false
            });
        });
    },
    'textbox': function(parent, data) {
        var id = data.id;
        var name = data.name;

        var template =
        '<div class="thumbnail">' +
             '<div class="caption">' +
                 '<div>' + name + '</div>' +
                 '<div class="input-group">' +
                     '<input type="text" class="form-control" id="val-' + id + '" placeholder="Enter value"/>' +
                     '<span class="input-group-btn">' +
                         '<button class="btn btn-default" id="sub-' + id + '" type="button">Submit</button>' +
                     '</span>' +
                 '</div>' +
             '</div>' +
        '</div>';

        $(template).appendTo(parent);

        $("#sub-" + id).click(function() {
            this.disabled = true;
            sendActionResponseData({
                "id": id,
                "value": $("#val-" + id).val()
            });
        });
    }
};