"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Utils = void 0;
var Utils = /** @class */ (function () {
    function Utils() {
    }
    //----------------------------------------------
    // Dates
    //----------------------------------------------
    //set the selected dates week start and end
    Utils.setSelectedWeekStartAndEnd = function (selectedDate) {
        var anchorDate = new Date(selectedDate);
        var anchorDay = anchorDate.getDay();
        var firstDay = anchorDate.getDate() - (anchorDay == 0 ? 7 : anchorDay) + 1; // First day is the day of the month - the day of the week (+ 1 for Monday)
        var lastDay = firstDay + 6; // last day is the first day + 6
        var startDate = new Date(selectedDate);
        startDate.setDate(firstDay);
        var endDate = new Date(selectedDate);
        endDate.setDate(lastDay);
        var returnDates = {
            WeekStart: startDate,
            WeekEnd: endDate,
        };
        return returnDates;
    };
    //zero pad a number, of course
    Utils.zeroPad = function (n) {
        if (n <= 9) {
            return '0' + n;
        }
        return n.toString();
    };
    //format a zero-padded date string using either a slash or a dash
    Utils.formatDate = function (dateString, dashFormat) {
        if (dashFormat === void 0) { dashFormat = false; }
        var offset = new Date().getTimezoneOffset();
        var returnDate = new Date(dateString);
        var separator = '/';
        if (dashFormat) {
            separator = '-';
        }
        //returnDate.setMinutes(returnDate.getMinutes() - offset);
        return this.zeroPad((returnDate.getMonth() + 1)) + separator + this.zeroPad(returnDate.getDate()) + separator + returnDate.getFullYear();
    };
    Utils.formatDateOnly = function (dateToFormat) {
        if (!dateToFormat) {
            return null;
        }
        dateToFormat = new Date(dateToFormat);
        return new Date(dateToFormat.getFullYear(), dateToFormat.getMonth(), dateToFormat.getDate());
    };
    Utils.formatDateToTime = function (dateToFormat) {
        return this.zeroPad(dateToFormat.getHours()) + ':' + this.zeroPad(dateToFormat.getMinutes());
    };
    Utils.formatDateOnlyToString = function (dateToFormat, dashFormat, zeroPad) {
        if (dashFormat === void 0) { dashFormat = false; }
        if (zeroPad === void 0) { zeroPad = true; }
        if (!dateToFormat) {
            return null;
        }
        var separator = '/';
        if (dashFormat) {
            separator = '-';
        }
        //make sure we are using a Date type and not Date?
        dateToFormat = new Date(dateToFormat);
        if (zeroPad)
            return this.zeroPad((dateToFormat.getMonth() + 1)) + separator + this.zeroPad(dateToFormat.getDate()) + separator + dateToFormat.getFullYear();
        else
            return (dateToFormat.getMonth() + 1) + separator + dateToFormat.getDate() + separator + dateToFormat.getFullYear();
    };
    Utils.formatDateOnlyWithMonthNameToString = function (dateToFormat) {
        if (!dateToFormat) {
            return null;
        }
        //make sure we are using a Date type and not Date?
        dateToFormat = new Date(dateToFormat);
        return dateToFormat.toLocaleString('default', { month: 'long' }) + ' ' + dateToFormat.getDate() + ', ' + dateToFormat.getFullYear();
    };
    Utils.formatDateMonthNameToString = function (dateToFormat) {
        if (!dateToFormat) {
            return null;
        }
        //make sure we are using a Date type and not Date?
        dateToFormat = new Date(dateToFormat);
        return dateToFormat.toLocaleString('default', { month: 'long' });
    };
    Utils.formatDateToTimeString = function (dateToFormat, zeroPadHours, includeSeconds) {
        if (zeroPadHours === void 0) { zeroPadHours = false; }
        if (includeSeconds === void 0) { includeSeconds = false; }
        if (!dateToFormat) {
            return null;
        }
        //make sure we are using a Date type and not Date?
        dateToFormat = new Date(dateToFormat);
        var h = dateToFormat.getHours(), m = this.zeroPad(dateToFormat.getMinutes()), s = this.zeroPad(dateToFormat.getSeconds());
        var hString = h.toString();
        var amPM = (h >= 12 && h !== 0) ? 'PM' : 'AM';
        if (zeroPadHours) {
            hString = (h > 12) ? this.zeroPad(h - 12) : this.zeroPad(h);
        }
        else {
            hString = (h > 12) ? (h - 12).toString() : h.toString();
        }
        if (includeSeconds) {
            return (hString + ':' + m + ':' + s + ' ' + amPM);
        }
        else {
            return (hString + ':' + m + ' ' + amPM);
        }
    };
    Utils.isValidDate = function (inputDate) {
        return !isNaN(Date.parse(inputDate));
    };
    Utils.formatDateToMonthYear = function (dateToFormat, shortMonth, shortYear, commaSeperation) {
        if (shortMonth === void 0) { shortMonth = false; }
        if (shortYear === void 0) { shortYear = false; }
        if (commaSeperation === void 0) { commaSeperation = false; }
        //make sure we are using a Date type and not Date?
        dateToFormat = new Date(dateToFormat);
        return dateToFormat.toLocaleString('default', { month: (shortMonth ? 'short' : 'long') }) + (commaSeperation ? ', ' : '-') + (shortYear ? dateToFormat.getFullYear().toString().substr(-2) : dateToFormat.getFullYear());
    };
    Utils.buildDateTime = function (scheduleDate, timeValue) {
        var returnDate = new Date(Utils.formatDateOnlyToString(scheduleDate, true) + ' ' + timeValue);
        return returnDate;
    };
    //----------------------------------------------
    // Arrays
    //----------------------------------------------
    //return a string array from a pipe-delimited string
    Utils.pipeStringToArray = function (pipeString) {
        var returnArray = [];
        if (pipeString) {
            returnArray = pipeString.split('|');
        }
        return returnArray;
    };
    Utils.convertObjectArrayToDropDownValues = function (objectArray, valueProp, labelProp) {
        var returnDv = [];
        for (var i = 0; i < objectArray.length; i++) {
            var dropDownValue = {
                CodeValues: objectArray[i][valueProp],
                DropDownItem: objectArray[i][labelProp],
                SortOrder: i,
                DropDownValueId: 0,
                ColorCode: null,
                Abbr: null,
                EntryBy: null,
                EntryDt: null,
                ModBy: null,
                ModDt: null,
            };
            returnDv.push(dropDownValue);
        }
        return returnDv;
    };
    Utils.convertStringArrayToDropDownValues = function (stringArray) {
        var returnDv = [];
        for (var i = 0; i < stringArray.length; i++) {
            var dropDownValue = {
                CodeValues: stringArray[i],
                DropDownItem: stringArray[i],
                SortOrder: i,
                DropDownValueId: 0,
                ColorCode: null,
                Abbr: null,
                EntryBy: null,
                EntryDt: null,
                ModBy: null,
                ModDt: null,
            };
            returnDv.push(dropDownValue);
        }
        return returnDv;
    };
    //check if any values in one array are present in the second array
    Utils.arrayIncludesAny = function (arraySource, arrayCheck) {
        for (var i = 0; i < arraySource.length; i++) {
            if (arrayCheck.includes(arraySource[i])) {
                return true;
            }
        }
        return false;
    };
    //check if all values in one array are present in the second array
    Utils.arrayIncludesAll = function (arraySource, arrayCheck) {
        for (var i = 0; i < arraySource.length; i++) {
            if (!arrayCheck.includes(arraySource[i])) {
                return false;
            }
        }
        return true;
    };
    //----------------------------------------------
    // Sizing
    //----------------------------------------------
    //dynamically size the scheduler popup
    Utils.schedulerPopupDynamicSize = function (expandValidationMessages) {
        if (expandValidationMessages === void 0) { expandValidationMessages = false; }
        var height = "innerHeight" in window
            ? window.innerHeight
            : document.documentElement.offsetHeight;
        var width = "innerWidth" in window
            ? window.innerWidth
            : document.documentElement.offsetWidth;
        var top = '2%';
        //var validationMessages = document.getElementById('validation-messages');
        //if (validationMessages) {
        //  console.log(validationMessages.offsetHeight);
        //}
        //schedule modal popup
        var scheduleModal = document.getElementById('schedule-modal');
        scheduleModal.style.height = (height * 0.95) + 'px';
        scheduleModal.style.top = top;
        scheduleModal.style.width = (width * 0.9) + 'px';
        scheduleModal.style.left = '5%';
        //tab bodies
        var tabBodies = document.querySelectorAll('#schedule-modal .mat-tab-group');
        for (var i = 0; i < tabBodies.length; i++) {
            tabBodies[i].style.height = (((height * (expandValidationMessages ? 0.75 : 0.9)) - 100) * 0.90) + 'px';
        }
        //paginated schedules
        var paginatedSchedules = document.querySelectorAll('#schedule-modal .paginated-schedules');
        for (var i = 0; i < paginatedSchedules.length; i++) {
            paginatedSchedules[i].style.height = (((height * (expandValidationMessages ? 0.75 : 0.9)) - 245) * 0.85) + 'px';
        }
    };
    //dynamically size the modal popup
    Utils.modalPopupDynamicSize = function () {
        var height = "innerHeight" in window
            ? window.innerHeight
            : document.documentElement.offsetHeight;
        var width = "innerWidth" in window
            ? window.innerWidth
            : document.documentElement.offsetWidth;
        //modal popup
        var modal = document.getElementById('modal-popup');
        if (modal) {
            modal.style.height = (height * 0.89) + 'px';
            modal.style.top = '5%';
            modal.style.width = (width * 0.9) + 'px';
            modal.style.left = '5%';
        }
        else {
            //generic popup for messaging
            modal = document.getElementById('modal-popup-message');
            if (modal) {
                var top = (height - modal.offsetHeight) / 2;
                var left = (width - modal.offsetWidth) / 2;
                modal.style.top = top + 'px';
                modal.style.left = left + 'px';
            }
        }
    };
    Utils.generatePopupMessage = function (title, body, buttonLabels, closeCallback) {
        var buttons = [];
        if (buttonLabels) {
            for (var i = 0; i < buttonLabels.length; i++) {
                buttons.push({ Label: buttonLabels[i] });
            }
        }
        var popupMessage = {
            Title: title,
            Body: body,
            Buttons: ((buttons === null || buttons === void 0 ? void 0 : buttons.length) > 0 ? buttons : null),
            CloseCallback: (typeof closeCallback === 'function' ? closeCallback : null),
        };
        return popupMessage;
    };
    Utils.generatePopupMessageWithCallbacks = function (title, body, buttons, noBlackFilter, closeCallback) {
        var popupMessage = {
            Title: title,
            Body: body,
            Buttons: (buttons ? buttons : null),
            NoBlackFilter: noBlackFilter,
            CloseCallback: (typeof closeCallback === 'function' ? closeCallback : null),
        };
        return popupMessage;
    };
    //----------------------------------------------
    // Element Handling
    //----------------------------------------------
    Utils.hideShow = function (elementId, hide, subClass) {
        if (hide === void 0) { hide = true; }
        if (subClass === void 0) { subClass = null; }
        try {
            var element = document.getElementById(elementId);
            if (subClass) {
                var subClasses = element.getElementsByClassName(subClass);
                if (subClasses) {
                    element = subClasses[0];
                }
            }
            if (element) {
                if (hide) {
                    element.style.display = 'none';
                }
                else {
                    element.style.display = 'initial';
                }
            }
        }
        catch (ex) {
        }
    };
    Utils.hideShowClasses = function (className, hide) {
        if (hide === void 0) { hide = true; }
        var elements = document.getElementsByClassName(className);
        for (var i = 0; i < elements.length; i++) {
            var element = elements[i];
            if (hide) {
                element.style.display = 'none';
            }
            else {
                element.style.display = null;
            }
        }
    };
    Utils.validateStartEndDateTime = function (schedule) {
        //update start and end datetime values
        if (schedule.StartTime && schedule.StartTime !== '0') {
            schedule.Startdatetime = Utils.buildDateTime(schedule.Startdatetime, schedule.StartTime);
        }
        if (schedule.EndTime && schedule.EndTime !== '0') {
            schedule.Enddatetime = Utils.buildDateTime(schedule.Startdatetime, schedule.EndTime);
        }
        else {
            schedule.Enddatetime = null;
        }
    };
    return Utils;
}());
exports.Utils = Utils;
//# sourceMappingURL=utils.js.map