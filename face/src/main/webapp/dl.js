var rootUrl = "dl/"

function periodic() {
    getDownloads();
    getActionRequest();
    setTimeout(periodic, 1000);
}

function secToTime(sec) {
    var minus = sec < 0;
    sec = Math.abs(sec);
    var h = Math.floor(sec / 3600);
    var m = Math.floor((sec - h * 3600) / 60);
    var s = sec - h * 3600 - m * 60;
    return (minus ? "-" : "") + (h < 10 ? "0" : "") + h + ":" + (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
}

function bytesToH(bytes, si, suff) {
    var thresh = si ? 1000 : 1024;
    if(Math.abs(bytes) < thresh) {
        return bytes + ' ' + suff;
    }
    var units = si ? ['k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'] : ['ki', 'Mi', 'Gi', 'Ti', 'Pi', 'Ei', 'Zi', 'Yi'];
    var u = -1;
    do {
        bytes /= thresh;
        u++;
    } while(Math.abs(bytes) > thresh && u < units.length - 1);
    return bytes.toFixed(1) + ' ' + units[u] + suff;
}

function col(data, tooltip) { //todo: data, tooltip escape
    if(tooltip === undefined) {
        return "<td>" + data + "</td>";
    } else {
        return '<td data-toggle="tooltip" title="' + tooltip + '">' + data + '</td>';
    }
}

function colProgress(data) {
  var val = parseInt(data);
  if(val < 0) {
    return '<td><div class="progress" style="margin-bottom: 0 !important;"><div class="progress-bar progress-bar-stripped active" role="progressbar" aria-valuenow="' + val + '" aria-valuemin="0" aria-valuemax="100" style="width: 100%;"></div></div></td>';
  }

  if(val > 100) val = 100;
  if(val < 0) val = 0;
  return '<td><div class="progress" style="margin-bottom: 0 !important;"><div class="progress-bar" role="progressbar" aria-valuenow="' + val + '" aria-valuemin="0" aria-valuemax="100" style="width: ' + val + '%;">' + val + '%</div></div></td>';
}

function getDownloads() {
    $.ajax({
        type: 'GET',
        url: rootUrl + "downloads/",
        dataType: 'json',
        success: function(data) {
            for(var i = 0; i < data.length; i++) {
                replaceRow(data[i].dlId.id, data[i]);
            }
        },
        error: function(jqXHR) {
            console.log(jqXHR);
        }
    });
}

function replaceRow(id, data) {
    var tableData = "";
    tableData += "<tr id='" + data.dlId.id + "'>";
    tableData += col(data.name);
    tableData += colProgress(data.progress);
    tableData += col(data.estTime < 0 ? "N/A" : secToTime(data.estTime));
    tableData += col(bytesToH(data.downloadedSize, false, 'B') + " / " + bytesToH(data.totalSize, false, 'B'),
                     data.downloadedSize + " B / " + data.totalSize + " B");
    if(data.speed < 0) {
        tableData += col("N/A");
    } else {
        tableData += col(bytesToH(data.speed, false, 'B/s'), data.speed + " B/s");
    }
    tableData += "</tr>";

    var orig = $("tr#" + id);
    if(orig.length) { //=exists
        orig.replaceWith(tableData);
    } else {
        $("#downloadsTable tbody").append(tableData);
    }
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
        error: function(jqXHR) {
            console.log(jqXHR);
        }
    });

    $('#addDialog').modal('hide');
    $('#addLinks').val('');
}

//------------------------------------------------

function getActionRequest() {
    $.ajax({
        type: 'GET',
        url: rootUrl + "actionRequests/",
        dataType: 'json',
        success: function(data) {
            console.log(data);
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

            for(var i = 0; i < ids.length; i++) {
                $("#action-requests").append('<div class="sidebar-module" id="' + data[i].id + '">' + data[i].html + '</div>');
                console.log("adding " + data[i].id);
            }
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
        error: function(jqXHR) {
            console.log(jqXHR);
        }
    });
}

//------------------------------------------------

function addLinksFocus() {
    if($("#addLinks").is(":visible")) {
        $("#addLinks").focus();
    } else {
        window.setTimeout(addLinksFocus, 100);
    }
}

$(document).ready(function() {
    $("#addStart").click(addDownloads);

    $("#addDialog").on('show.bs.modal', function() {
        var timer = window.setTimeout(addLinksFocus, 100);
    });

    setTimeout(periodic, 1000);
});