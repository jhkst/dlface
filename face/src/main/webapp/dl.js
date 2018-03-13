var rootUrl = "dl/v1/";
var messageTTL = 5 * 1000;
var updateInterval = 1000;
var updateFinishedDelay = 1000;
var dropFiles = [];
var windowFocus = true;
var faviconNormal = "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";
var faviconAlert = "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";

function getFinishedDownloads() {
    $.ajax({
        type: 'GET',
        url: rootUrl + "downloads/finished",
        dataType: 'json',
        success: function(data) {
            DL.alert.hide("connection-error");

            var retainIds = [];
            for(var i = 0; i < data.length; i++) {
                DL.finishedDownloadsTable.add(data[i].dlId.id, data[i]);
                retainIds.push(data[i].dlId.id);
            }
            DL.finishedDownloadsTable.retain(retainIds);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            DL.alert.showDanger("connection-error", Util.errorMessage(jqXHR, errorThrown));
        }
    });
}

function getDownloads() {
    $.ajax({
        type: 'GET',
        url: rootUrl + "downloads",
        dataType: 'json',
        success: function(data) {
            DL.alert.hide("connection-error");

            var total = 0;
            var down = 0;
            var retainIds = [];
            for(var i = 0; i < data.length; i++) {
                DL.downloadsTable.add(data[i].dlId.id, data[i]);
                retainIds.push(data[i].dlId.id);
                total += data[i].totalSize;
                down += data[i].downloadedSize;
            }
            var removedCnt = DL.downloadsTable.retain(retainIds);
            if(removedCnt > 0) {
                setTimeout(getFinishedDownloads, updateFinishedDelay);
            }
            //TODO: total see: https://datatables.net/examples/advanced_init/footer_callback.html
       },
        error: function(jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            DL.alert.showDanger("connection-error", Util.errorMessage(jqXHR, errorThrown));
        }
    });
}

function addDownloads() {
    var data = {'downloadList': $("#addLinks").val()};
    var jsondata = JSON.stringify(data);
    $.ajax({
        type: 'POST',
        url: rootUrl + "downloads/add",
        contentType: 'application/json',
        dataType: 'json',
        data: jsondata,
        success: function() {
            DL.alert.hide("connection-error");
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            DL.alert.showDanger("connection-error", Util.errorMessage(jqXHR, errorThrown));
        }
    });

    $('#addDialog').modal('hide');
    $('#addLinks').val('');
}

function addFiles() {
    var formData = new FormData();
    var fileInput = $('#js-upload-files');
    var files = fileInput.get(0);
    if(files.length === 0) {
        return;
    }

    var formFiles = $('#js-upload-files')[0].files;

    $.each(formFiles, function(i, file) {
        formData.append('files[]', file);
    });
    $.each(dropFiles, function(i, file){
        formData.append('files[]', file);
    });

    if(formFiles.length + dropFiles.length > 0) {
        $.ajax({
            type: 'POST',
            url: rootUrl + 'downloads/add/files',
            data: formData,
            cache: false,
            contentType: false,
            processData: false,
            success: function(data) {
                DL.alert.hide("connection-error");
                cleanUploads();
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.debug(jqXHR);
                DL.alert.showDanger("connection-error", Util.errorMessage(jqXHR, errorThrown));
            }
        });
    }
}

function getMessages() {
    $.ajax({
        type: 'GET',
        url: rootUrl + "alerts",
        contentType: 'application/json',
        dataType: 'json',
        success: function(data) {
            DL.alert.hide("connection-error");

            for(var i = 0; i < data.length; i++) {
                var dmid = "msg-" + data[i].id;
                DL.alert.show(dmid, "alert-" + data[i].type.toLowerCase(), data[i].message, true, messageTTL);
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            DL.alert.showDanger("connection-error", Util.errorMessage(jqXHR, errorThrown));
        }
    });
}

function getSystemInfo() {
    $.ajax({
        type: 'GET',
        url: rootUrl + "systemInfo",
        contentType: 'application/json',
        dataType: 'json',
        success: function(data) {
            DL.alert.hide("connection-error");

            DL.systemInfo(data);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            DL.alert.showDanger("connection-error", Util.errorMessage(jqXHR, errorThrown));
        }
    });
}

function getActionRequest() {
    $.ajax({
        type: 'GET',
        url: rootUrl + "actionRequests/",
        dataType: 'json',
        success: function(data) {
            DL.alert.hide("connection-error");

            var ids = [];
            for(var i = 0; i < data.length; i++) {
                ids.push(data[i].id);
            }

            $("#action-requests .sidebar-module").each(function(index) {
                var idPos = $.inArray(this.id, ids);
                if(idPos >= 0) {
                    ids.splice(idPos, 1);
                    data.splice(idPos, 1);
                } else {
                    this.remove();
                }
            });

            for(var j = 0; j < ids.length; j++) {
                var actionDialogHtml = null; //TODO:
                var envelopeHtml = '<div class="sidebar-module" id="' + data[j].id + '"></div>';
                var envelope = $(envelopeHtml).appendTo("#action-requests");
                DLActions[data[j].type](envelope, data[j]);
            }

            if(ids.length > 0) {
                Util.notification("New user action request" + (ids.length > 1 ? "s" : ""), faviconNormal);
            }

            if($("#action-requests .sidebar-module").length > 0) {
                Util.setFaviconBase64(faviconAlert);
            } else {
                Util.setFaviconBase64(faviconNormal);
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
            DL.alert.showDanger("connection-error", Util.errorMessage(jqXHR, errorThrown));
        }
    });
}

function sendActionResponseData(data) {
    var jsondata = JSON.stringify(data);
    $.ajax({
        type: 'POST',
        url: rootUrl + "actionRequests/response",
        contentType: 'application/json',
        dataType: 'json',
        data: jsondata,
        success: function() {
            DL.alert.hide("connection-error");
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
            DL.alert.showDanger("connection-error", Util.errorMessage(jqXHR, errorThrown));
        }
    });
}

function updateDownloadButtons() {
    var dt = $("#downloadsTable").DataTable();
    var selectedRowsCnt = dt.rows({selected: true}).count();
    dt.button(0).enable(selectedRowsCnt > 0);
    dt.button(1).enable(selectedRowsCnt === 1);
}

function updateFinishedButtons() {
    var dt = $("#finishedDownloadsTable").DataTable();
    var selectedRowsCnt = dt.rows({selected: true}).count();
    dt.button(0).enable(selectedRowsCnt === 1);
}

//------------------------------------------------

function addLinksFocus() {
    if($("#addLinks").is(":visible")) {
        $("#addLinks").focus();
    } else {
        window.setTimeout(addLinksFocus, 100);
    }
}

function updateUploadInfo() {
    var dropped = dropFiles.length;
    var uploadForm = $('#js-upload-files').prop('files').length;
    var total = dropped + uploadForm;
    if(total <= 0) {
        $('#upload-info-box').hide();
    } else {
        writeUploadInfo(total + " file" + (total === 1 ? "" : "s") + " prepared to upload");
        $('#upload-info-box').show();
    }
}

function writeUploadInfo(text) {
    $('#upload-info').text(text);
}

function cleanUploads() {
    $('#js-upload-files').val("");
    dropFiles = [];
    updateUploadInfo();
}

$(document).ready(function() {
    $("#addStart").click(function() {
        addDownloads();
        addFiles();
    });

    var dt = $('#downloadsTable').DataTable({
        responsive: true,
        stateSave: true,
        rowId: "dlId",
        columns: [
            {"data": "name"},
            {
                "data": "progress",
                "render": function(data, type, row) {
                    var val = parseFloat(data);
                    if(val <= 0) {
                        return '<div class="progress" style="margin-bottom: 0 !important;"><div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: 100%;"><span class="sr-only"></span></div></div>';
                    }
                    if(val > 100) {
                        val = 100;
                    }
                    var showVal = parseFloat(Math.round(val * 10)/10).toFixed(1);
                    return '<div class="progress" style="margin-bottom: 0 !important;"><div class="progress-bar" role="progressbar" aria-valuenow="' + val + '" aria-valuemin="0" aria-valuemax="100" style="width: ' + showVal + '%;">' + showVal + '&nbsp;%</div></div>';
                }
            },
            {"data": "estTime"},
            {"data": "size"},
            {"data": "speed"}
        ],
        select: true,
        dom: 'Bfrtip',
        buttons: [{
                text: '<em class="glyphicon glyphicon-remove"></em> Cancel',
                enabled: false,
                action: function() {
                    var data = dt.rows({selected: true}).data();
                    bootbox.confirm("Really cancel " + data.length + " download(s)?", function(result) {
                        if(result === true) {
                            for(var i = 0; i < data.length; i++) {
                                $.ajax({
                                    type: 'POST',
                                    url: rootUrl + "downloads/cancel/" + data[i].dlId,
                                    contentType: 'application/json',
                                    dataType: 'json'
                                });
                            }
                        }
                    });
                }
            }, {
                text: '<em class="glyphicon glyphicon-eye-open"></em> Details',
                enabled: false,
                action: function() {
                    var data = dt.rows({selected: true}).data()[0];
                    var dd = $("#downloadDetailTable").DataTable();
                    dd.clear();
                    $.each(data._data, function(k, v) {
                        dd.row.add({"property": k, "value": JSON.stringify(v)});
                    });
                    dd.draw();
                    $("#detailDialog").modal('show');
                }
            }
        ],
        oLanguage: {
            "sEmptyTable": "No active downloads available"
        }
    });

    var ft = $('#finishedDownloadsTable').DataTable({
        responsive: true,
        stateSave: true,
        rowId: "dlId",
        columns: [
            {"data": "name"},
            {"data": "size"},
            {"data": "startTime"},
            {"data": "endTime"}
        ],
        select: 'single',
        dom: 'Bfrtip',
        buttons: [{
                text: '<em class="glyphicon glyphicon-eye-open"></em> Details',
                enabled: false,
                action: function() {
                    var data = ft.rows({selected: true}).data()[0];
                    var dd = $("#downloadDetailTable").DataTable();
                    dd.clear();
                    $.each(data._data, function(k, v) {
                        dd.row.add({"property": k, "value": JSON.stringify(v)});
                    });
                    dd.draw();
                    $("#detailDialog").modal('show');
                }
            }
        ],
        oLanguage: {
            "sEmptyTable": "No finished downloads yet"
        }
    });

    var dd = $("#downloadDetailTable").DataTable({
        columns: [
            {"data": "property"},
            {"data": "value"}
        ],
        paging: false,
        select: false,
        searching: false,
        ordering: true,
        info: false,
    });

    dt.on('select', function() {
        updateDownloadButtons();
    });
    dt.on('deselect', function() {
        updateDownloadButtons();
    });
    dt.on('draw', function() {
        updateDownloadButtons();
    });

    ft.on('select', function() {
        updateFinishedButtons();
    });
    ft.on('deselect', function() {
        updateFinishedButtons();
    });
    ft.on('draw', function() {
        updateFinishedButtons();
    });


    $("#addDialog").on('show.bs.modal', function() {
        var timer = window.setTimeout(addLinksFocus, 100);
    });

    $("#detailDialog").on('show.bs.modal', function() {
        $(this).show();
        setModalMaxHeight(this);
    });

    $(window).resize(function() {
      if ($('.modal.in').length !== 0) {
        setModalMaxHeight($('.modal.in'));
      }
    });

    $('#drop-zone').on('dragover', false).on('dragleave', false).on('drop', function(e) {
        console.log("drop");
        e.preventDefault();
        $.each(e.originalEvent.dataTransfer.files, function(i, file) {
           dropFiles.push(file);
        });
        updateUploadInfo();
    });

    $('#js-upload-files').on('change', function() {
        updateUploadInfo();
    });

    $('#upload-info-box').hide();

    $('#remove-uploads').on('click', function() {
        cleanUploads();
    });

    $(window).bind('focus', function() {
        windowFocus = true;
    });
    $(window).bind('blur', function() {
        windowFocus = false;
    });

    Util.notificationPermissionRequest();

    setTimeout(getFinishedDownloads, updateFinishedDelay);
    setTimeout(periodic, updateInterval);
});

function setModalMaxHeight(element) {
  this.$element     = $(element);
  this.$content     = this.$element.find('.modal-content');
  var borderWidth   = this.$content.outerHeight() - this.$content.innerHeight();
  var dialogMargin  = $(window).width() < 768 ? 20 : 60;
  var contentHeight = $(window).height() - (dialogMargin + borderWidth);
  var headerHeight  = this.$element.find('.modal-header').outerHeight() || 0;
  var footerHeight  = this.$element.find('.modal-footer').outerHeight() || 0;
  var maxHeight     = contentHeight - (headerHeight + footerHeight);

  this.$content.css({
      'overflow': 'hidden'
  });

  this.$element
    .find('.modal-body').css({
      'max-height': maxHeight,
      'overflow-y': 'auto'
  });
}

var noFocusCount = 0;
function periodic() {
    if(windowFocus) {
        getDownloads();
        getActionRequest();
        getMessages();
        getSystemInfo();
        noFocusCount = 0;
    } else {
        if(noFocusCount > 10) {
            getActionRequest();
            noFocusCount = 0;
        } else {
            noFocusCount++;
        }
    }
    setTimeout(periodic, updateInterval);
}
