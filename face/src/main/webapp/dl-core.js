var DL = {
    alert: {
        showSuccess: function(id, message, dismissible, time) {
            this.show(id, "alert-success", message, time);
        },
        showInfo: function(id, message, dismissible, time) {
            this.show(id, "alert-info", message, time);
        },
        showWarning: function(id, message, dismissible, time) {
            this.show(id, "alert-warning", message, time);
        },
        showDanger: function(id, message, dismissible, time) {
            this.show(id, "alert-danger", message, time);
        },
        show: function(id, type, message, dismissible, time)  {
            var sel = $("#alerts #" + id);
            if(!sel.length) { //not exists
                var cls = "alert " + type + (dismissible ? " alert-dismissible" : "");
                var htm = '<div id="' + id + '" class="' + cls + '" role="alert">';
                if(dismissible) {
                    htm += '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>';
                }
                htm += '<span class="alertmsg">';
                htm += message;
                htm += '</span>';
                htm += '</div>';
                $("#alerts").append(htm);

                if(time > 0) {
                    var timeout = setTimeout(function() {
                        DL.alert.hide(id);
                    }, time);
                }

            } else {
                sel.find(".alertmsg").html(message);
            }
        },
        hide: function(id) {
            var sel = $("#alerts #" + id);
            if(sel.length) { //exists
                sel.remove();
            }
        }
    },
    dialog: {
        addDownload: {
            show: function() {}
        },
        configuration: {
            show: function() {}
        }
    },
    downloadsTable: {
        add: function(dlId, data) {
            var dt = $("#downloadsTable").DataTable();

            var tData = {
                "dlId": data.dlId.id,
                "name": data.name,
                "progress": data.progress,
                "estTime": data.estTime < 0 ? "N/A" : Util.secToTime(data.estTime),
                "size": Util.bytesToH(data.downloadedSize, 1, false, 'B', '&nbsp;') + " / " + Util.bytesToH(data.totalSize, 1, false, 'B', '&nbsp;'),
                "speed": data.speed < 0 ? "N/A" : Util.bytesToH(data.speed, 1, false, 'B/s', '&nbsp;'),
                "_data": data
            };

            var row = dt.row("#" + dlId);
            if(row.length) { //=exists
                row.data(tData).draw('full-hold');
            } else {
                dt.row.add(tData).draw('full-hold');
            }

        },
        retain: function(dlIds) {
            var dt = $("#downloadsTable").DataTable();

            var removedCount = 0;
            dt.rows().every(function (rowIdx, tableLoop, rowLoop) {
                if(dlIds.indexOf(this.data().dlId) < 0) {
                    this.remove();
                    removedCount++;
                }
            });
            dt.draw('full-hold');
            return removedCount;
        }
    },
    finishedDownloadsTable: {
        add: function(dlId, data) {
            var dt = $("#finishedDownloadsTable").DataTable();

            var tData = {
                "dlId": data.dlId.id,
                "name": data.name,
                "startTime": data.startTime ? Util.toLocalTimezone(data.startTime) : "N/A",
                "endTime": data.endTime ? Util.toLocalTimezone(data.endTime) : "N/A",
                "size": Util.bytesToH(data.downloadedSize, 1, false, 'B', '&nbsp;'),
                "_data": data
            };

            var row = dt.row("#" + dlId);
            if(row.length) { //=exists
                row.data(tData).draw('full-hold');
            } else {
                dt.row.add(tData).draw('full-hold');
            }

        },
        retain: function(dlIds) {
            var dt = $("#finishedDownloadsTable").DataTable();

            dt.rows().every(function (rowIdx, tableLoop, rowLoop) {
                if(dlIds.indexOf(this.data().dlId) < 0) {
                    this.remove();
                }
            });
            dt.draw('full-hold');
        }
    },
    systemInfo: function(data) {
        $("#appVersion").html(data.appVersion);
        $("#upTime").html(Util.secToTime(data.upTime/1000));
        $("#freeSpace").html(Util.bytesToH(data.freeSpace, 1, false, "B", '&nbsp;'));
        $("#freeSpace").css("color", data.lowSpace ? "red" : "");
        $("#maxMem").html(Util.bytesToH(data.maxMem, 1, false, "B", '&nbsp;'));
        $("#totalMem").html(Util.bytesToH(data.totalMem, 1, false, "B", '&nbsp;'));
        $("#usedMem").html(Util.bytesToH(data.totalMem - data.freeMem, 1, false, "B", '&nbsp;'));
        $("#threadCnt").html(data.activeThreadCount);
        $("#lockedThreadCnt").html(data.lockedThreadCount);
        $("#systemLoad").html(data.systemLoad);
    }

};

var Util = {
    secToTime: function(sec) {
        var minus = sec < 0;
        var leftover = Math.abs(sec);
        var y = Math.floor(leftover / 31556926);
        leftover -= y * 31556926;
        var mo = Math.floor(leftover / 2629743.83);
        leftover -= mo * 2629743.83;
        var d = Math.floor(leftover / 86400);
        leftover -= d * 86400;
        var h = Math.floor(leftover / 3600);
        leftover -= h * 3600;
        var m = Math.floor(leftover / 60);
        leftover -= m * 60;
        var s = Math.round(leftover);

        var out = (minus ? "-" : "");

        if(y > 0) {
            out += y + "y ";
        }
        if(y > 0 || mo > 0) {
            out += mo + "mo ";
        }
        if(y > 0 || mo > 0 || d > 0) {
            out += d + "d ";
        }

        out += (h < 10 ? "0" : "") + h + ":";
        out += (m < 10 ? "0" : "") + m + ":";
        out += (s < 10 ? "0" : "") + s;
        return out;
    },
    bytesToH: function(bytes, digits, si, suff, space) {
        var thresh = si ? 1000 : 1024;
        var units = si ? ['', 'k', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'] : ['', 'ki', 'Mi', 'Gi', 'Ti', 'Pi', 'Ei', 'Zi', 'Yi'];
        var u;
        for(u = 0; u < units.length - 1 && Math.abs(bytes) > thresh; u++) {
            bytes /= thresh;
        }
        return bytes.toFixed(digits) + space + units[u] + suff;
    },
    toLocalTimezone: function(dateISO8601) {
        return moment(dateISO8601).format("YYYY-MM-DDTHH:mm:ss.SSSZZ");
    },
    errorMessage: function(jqXHR, exception) {
        switch(jqXHR.status) {
            case 0: return 'Not connected. Verify Network.';
            case 404: return 'Requested page not found. [404]';
            case 500: return 'Internal Server Error [500].';
        }

        switch(exception) {
            case 'parseerror': return 'Requested JSON parse failed.';
            case 'timeout': return 'Time out error.';
            case 'abort': return 'Ajax request aborted.';
        }

        return 'Uncaught Error. ' + jqXHR.responseText;
    },
    getIconBase64: function(iconBase64) {
        return 'data:image/png;base64,' + iconBase64;
    },
    setFaviconBase64: function(iconBase64) {
        var link = document.querySelector("link[rel*='icon']") || document.createElement('link');
        link.type = 'image/png';
        link.rel = 'shortcut icon';
        link.href = this.getIconBase64(iconBase64);
        document.getElementsByTagName('head')[0].appendChild(link);
    },
    notification: function(text, iconBase64) {
        if(window.Notification && Notification.permission === "granted") {
            var n = new Notification('dlFace', {
                body: text,
                icon: this.getIconBase64(iconBase64)
            });
        }
    },
    notificationPermissionRequest: function() {
        if(window.Notification && Notification.permission !== "denied") {
            Notification.requestPermission();
        }
    }
};