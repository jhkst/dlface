var rootUrl = "dl/v1/";
var messageTTL = 5 * 1000;
var updateInterval = 1000;
var updateFinishedDelay = 1000;
var dropFiles = [];

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
                    console.log("not modifying " + this.id);
                } else {
                    this.remove();
                    console.log("removing " + this.id);
                }
            });

            for(var j = 0; j < ids.length; j++) {
                var actionDialogHtml = null; //TODO:
                var envelopeHtml = '<div class="sidebar-module" id="' + data[j].id + '"></div>';
                var envelope = $(envelopeHtml).appendTo("#action-requests");
                DLActions[data[j].type](envelope, data[j]);
                console.log("adding " + data[j].id);
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

function periodic() {
    getDownloads();
    getActionRequest();
    getMessages();
    getSystemInfo();
    setTimeout(periodic, updateInterval);
}
