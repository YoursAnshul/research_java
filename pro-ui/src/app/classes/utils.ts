
import { IActionButton, IDropDownValue, IPopupMessage, ISchedule, IWeekStartAndEnd, IWeekStartAndEndStrings } from "../interfaces/interfaces";
import moment from 'moment';
import colors from "tailwindcss/colors";

export class Utils {

  //----------------------------------------------
  // Dates
  //----------------------------------------------

  //set the selected dates week start and end
  public static setSelectedWeekStartAndEnd(selectedDate: Date): IWeekStartAndEnd {

    let anchorDate: Date = new Date(selectedDate);
    let anchorDay: number = anchorDate.getDay();
    var firstDay = anchorDate.getDate() - (anchorDay == 0 ? 7 : anchorDay) + 1; // First day is the day of the month - the day of the week (+ 1 for Monday)
    var lastDay = firstDay + 6; // last day is the first day + 6

    let startDate = new Date(selectedDate);
    startDate.setDate(firstDay);
    let endDate = new Date(selectedDate);
    endDate.setDate(lastDay);

    var returnDates: IWeekStartAndEnd = {
      weekStart: startDate,
      weekEnd: endDate,
    };

    return returnDates;
  }

  //zero pad a number, of course
  public static zeroPad(n: number): string {
    if (n <= 9) {
      return '0' + n;
    }
    return n.toString()
  }

  //format a zero-padded date string using either a slash or a dash
  public static formatDate(dateString: string, dashFormat: boolean = false): string {
    var offset = new Date().getTimezoneOffset();
    var returnDate = new Date(dateString);
    var separator = '/';

    if (dashFormat) {
      separator = '-';
    }

    //returnDate.setMinutes(returnDate.getMinutes() - offset);
    return this.zeroPad((returnDate.getMonth() + 1)) + separator + this.zeroPad(returnDate.getDate()) + separator + returnDate.getFullYear();
  }

  public static formatDateOnly(dateToFormat: Date | null | undefined): Date | null {
    if (!dateToFormat) {
      return null;
    }

    dateToFormat = new Date(dateToFormat);

    return new Date(dateToFormat.getFullYear(), dateToFormat.getMonth(), dateToFormat.getDate());
  }

  public static formatMonthOnly(dateToFormat: Date | null | undefined): Date | null {
    if (!dateToFormat) {
      return null;
    }

    dateToFormat = new Date(dateToFormat);

    return new Date(dateToFormat.getFullYear(), dateToFormat.getMonth(), dateToFormat.getDate());
  }

  public static formatDateToTime(dateToFormat: Date): string {

    return this.zeroPad(dateToFormat.getHours()) + ':' + this.zeroPad(dateToFormat.getMinutes());
  }

  public static formatDateOnlyToDateOnlyString(dateToFormat: Date | null | undefined, dashFormat: boolean = false, zeroPad: boolean = true, internationalFormat: boolean = false): string | null {

    if (!dateToFormat) {
      return null;
    }

    dateToFormat = new Date(dateToFormat);
    const offset = dateToFormat.getTimezoneOffset() / 60;
    dateToFormat.setHours(dateToFormat.getHours() + offset);
    
    var separator = '/';

    if (dashFormat) {
      separator = '-';
    }

    let year: number = dateToFormat.getFullYear();
    let month: number = (dateToFormat.getMonth() + 1);
    let day: number = dateToFormat.getDate();

    let yearString: string = year.toString();
    let monthString: string = month.toString();
    let dayString: string = day.toString();

    if (zeroPad) {
      monthString = this.zeroPad(month);
      dayString = this.zeroPad(day);
    }

    if (internationalFormat) {
      return `${yearString}${separator}${monthString}${separator}${dayString}`;
    } else {
      return `${monthString}${separator}${dayString}${separator}${yearString}`;
    }

  }

  public static formatDateOnlyToStringUTC(dateToFormat: Date | null | undefined, dashFormat: boolean = false, zeroPad: boolean = true, internationalFormat: boolean = false): string | null {

    if (!dateToFormat) {
      return null;
    }
    var separator = '/';

    if (dashFormat) {
      separator = '-';
    }

    //make sure we are using a Date type and not Date?

    let year: number = dateToFormat.getFullYear();
    let month: number = (dateToFormat.getMonth() + 1);
    let day: number = dateToFormat.getDate();

    let yearString: string = year.toString();
    let monthString: string = month.toString();
    let dayString: string = day.toString();

    if (zeroPad) {
      monthString = this.zeroPad(month);
      dayString = this.zeroPad(day);
    }

    if (internationalFormat) {
      return moment.utc(dateToFormat).format(`YYYY${separator}MM${separator}DD`);
    } else {
      return moment.utc(dateToFormat).format(`MM${separator}DD${separator}YYYY`);
    }

  }

  static convertToDate(dateToConvert: string): Date {
    if (dateToConvert === null) {
        return new Date();
    }
    const milliseconds = Date.parse(dateToConvert);
    if (isNaN(milliseconds)) {
        return new Date(); // Invalid date string
    }
    return new Date(milliseconds);
  }

  public static formatDateAsStringUTC(dateToFormat: Date | null | undefined, dashFormat: boolean = false, zeroPad: boolean = true, internationalFormat: boolean = false): string | null {
    if (!dateToFormat) {
      return null;
    }
    return moment.utc(dateToFormat).format('MM/DD/YYYY');
  }


  public static formatDateOnlyWithMonthNameToString(dateToFormat: Date | null | undefined): string | null {
    if (!dateToFormat) {
      return null;
    }

    return moment.utc(dateToFormat).format('MM-DD , YYYY');
  }

  public static formatDateMonthNameToString(dateToFormat: Date | null | undefined): string | null {
    if (!dateToFormat) {
      return null;
    }

    //make sure we are using a Date type and not Date?
    dateToFormat = new Date(dateToFormat);

    return dateToFormat.toLocaleString('default', { month: 'long' });
  }

  public static formatDateToTimeString(dateToFormat: Date | null | undefined, zeroPadHours: boolean = false, includeSeconds: boolean = false): string | null {
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
    } else {
      hString = (h > 12) ? (h - 12).toString() : h.toString();
    }

    if (includeSeconds) {
      return (hString + ':' + m + ':' + s + ' ' + amPM);
    } else {
      return (hString + ':' + m + ' ' + amPM);}

  }

  public static isValidDate(inputDate: any): boolean {
    return !isNaN(Date.parse(inputDate));
  }

  public static formatDateToShortMonthYearUTC(dateToFormat: Date) {
    return moment.utc(dateToFormat).format('MMM-YY');
  }

  public static formatDateToMonthYear(dateToFormat: Date, shortMonth: boolean = false, shortYear: boolean = false, commaSeperation: boolean = false) {
    //make sure we are using a Date type and not Date?
    dateToFormat = new Date(dateToFormat);

    return dateToFormat.toLocaleString('default', { month: (shortMonth ? 'short' : 'long') }) + (commaSeperation ? ', ' : '-') + (shortYear ? dateToFormat.getFullYear().toString().substr(-2) : dateToFormat.getFullYear());
  }

  public static buildDateTime(scheduleDate: Date | null | undefined, timeValue: string): Date | null {
    if (!scheduleDate) {
      return null;
    }
    let returnDate: Date = new Date(Utils.formatDateOnlyToStringUTC(scheduleDate, true) + ' ' + timeValue);
    return returnDate;
  }

  //----------------------------------------------
  // Arrays
  //----------------------------------------------

  //return a string array from a pipe-delimited string
  public static pipeStringToArray(pipeString: string): Array<string> {

    let returnArray: string[] = [];
    if (pipeString) {
      returnArray = pipeString.split('|');
    }

    return returnArray;
  }

  public static convertObjectArrayToDropDownValues(objectArray: any[], valueProp: string, labelProp: string): IDropDownValue[] {
    let returnDv: IDropDownValue[] = [];

    for (var i = 0; i < objectArray.length; i++) {
      let dropDownValue: IDropDownValue = {
        codeValues: objectArray[i][valueProp],
        dropDownItem: objectArray[i][labelProp],
        sortOrder: i,
        dropDownValueId: 0,
        colorCode: null,
        abbr: null,
        entryBy: null,
        entryDt: null,
        modBy: null,
        modDt: null,
      }

      returnDv.push(dropDownValue);
    }

    return returnDv;
  }

  public static convertStringArrayToDropDownValues(stringArray: any[]): IDropDownValue[] {
    let returnDv: IDropDownValue[] = [];

    for (var i = 0; i < stringArray.length; i++) {
      let dropDownValue: IDropDownValue = {
        codeValues: stringArray[i],
        dropDownItem: stringArray[i],
        sortOrder: i,
        dropDownValueId: 0,
        colorCode: null,
        abbr: null,
        entryBy: null,
        entryDt: null,
        modBy: null,
        modDt: null,
      }

      returnDv.push(dropDownValue);
    }

    return returnDv;
  }

  //check if any values in one array are present in the second array
  public static arrayIncludesAny(arraySource: Array<any>, arrayCheck: Array<any>): boolean {
    for (var i = 0; i < arraySource.length; i++) {
      if (arrayCheck.includes(arraySource[i])) {
        return true;
      }
    }

    return false;
  }

  //check if all values in one array are present in the second array
  public static arrayIncludesAll(arraySource: Array<any>, arrayCheck: Array<any>): boolean {
    for (var i = 0; i < arraySource.length; i++) {
      if (!arrayCheck.includes(arraySource[i])) {
        return false;
      }
    }

    return true;
  }

  //----------------------------------------------
  // Sizing
  //----------------------------------------------

  //dynamically size the scheduler popup
  public static schedulerPopupDynamicSize(expandValidationMessages: boolean = false): void {
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
    let scheduleModal: HTMLElement | null = document.getElementById('schedule-modal');
    if (scheduleModal !== null) {
      scheduleModal.style.height = (height * 0.95) + 'px';
      scheduleModal.style.top = top;
      scheduleModal.style.width = (width * 0.9) + 'px';
      scheduleModal.style.left = '5%';
      scheduleModal.style.backgroundColor = colors.white
    }

    //tab bodies
    let tabBodies: NodeListOf<Element> = document.querySelectorAll('#schedule-modal .mat-tab-group');
    for (var i = 0; i < tabBodies.length; i++) {
      (<HTMLElement>tabBodies[i]).style.height = (((height * (expandValidationMessages ? 0.75 : 0.9)) - 100) * 0.90) + 'px';
    }

    //paginated schedules
    var paginatedSchedules = document.querySelectorAll('#schedule-modal .paginated-schedules');
    for (var i = 0; i < paginatedSchedules.length; i++) {
      (<HTMLElement>paginatedSchedules[i]).style.height = (((height * (expandValidationMessages ? 0.75 : 0.9)) - 245) * 0.85) + 'px';
    }

  }

  //dynamically size the modal popup
  public static modalPopupDynamicSize(): void {
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
    } else {
      //generic popup for messaging
      modal = document.getElementById('modal-popup-message');
      if (modal) {
        var top = (height - modal.offsetHeight) / 2;
        var left = (width - modal.offsetWidth) / 2;
        modal.style.top = top + 'px';
        modal.style.left = left + 'px';
      }
    }
  }

  public static generatePopupMessage(title: string, body: string, buttonLabels?: string[], closeCallback?: Function): IPopupMessage {
    let buttons: IActionButton[] = [];
    if (buttonLabels) {
      for (var i = 0; i < buttonLabels.length; i++) {
        buttons.push({ label: buttonLabels[i] } as IActionButton)
      }
    }

    let popupMessage: IPopupMessage = {
      title: title,
      body: body,
      buttons: (buttons?.length > 0 ? buttons : null),
      closeCallback: (typeof closeCallback === 'function' ? closeCallback : null),
    }

    return popupMessage;
  }

  public static generatePopupMessageWithCallbacks(title: string, body: string, buttons?: IActionButton[], noBlackFilter?: boolean, closeCallback?: Function): IPopupMessage {
    let popupMessage: IPopupMessage = {
      title: title,
      body: body,
      buttons: (buttons ? buttons : null),
      noBlackFilter: noBlackFilter,
      closeCallback: (typeof closeCallback === 'function' ? closeCallback : null),
    }

    return popupMessage;
  }

  //----------------------------------------------
  // Element Handling
  //----------------------------------------------

  public static hideShow(elementId: string, hide: boolean = true, subClass: string | null = null): void {
    try {
      let element: HTMLElement | null = document.getElementById(elementId);

      if (subClass && element) {
        var subClasses = element.getElementsByClassName(subClass);
        if (subClasses) {
          element = <HTMLElement>subClasses[0];
        }
      }

      if (element) {
        if (hide) {
          element.style.display = 'none';
        } else {
          element.style.display = 'initial';
        }
      }
    } catch (ex) {

    }
  }

  public static hideShowClasses(className: string, hide: boolean = true): void {
    let elements = document.getElementsByClassName(className);

    for (var i = 0; i < elements.length; i++) {
      let element: HTMLElement = <HTMLElement>elements[i];
      if (hide) {
        element.style.display = 'none';
      } else {
        element.style.display = '';
      }
    }

  }

  public static validateStartEndDateTime(schedule: ISchedule): void {

    //update start and end datetime values
    if (schedule.startTime && schedule.startTime !== '0') {
      schedule.startdatetime = Utils.buildDateTime(schedule.startdatetime, schedule.startTime);
    }
    if (schedule.endTime && schedule.endTime !== '0') {
      schedule.enddatetime = Utils.buildDateTime(schedule.startdatetime, schedule.endTime);
    } else {
      schedule.enddatetime = null;
    }

  }

  //----------------------------------------------
  // Numbers
  //----------------------------------------------
  
  //check if a value is a number and return it as a number if it is
  public static ifNumber(value: any): any {
    if (!isNaN(value) && value !== null && value !== '') {
      return Number(value);
    }

    return value;
  }

  //generate a random number
  public static randomInt(min: number, max: number): number {
    return Math.floor(Math.random() * (max - min + 1)) + min;
  }

  //----------------------------------------------
  // Strings
  //----------------------------------------------
  
  //generate a random GUID
  public static generateGUID(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      var r = Utils.randomInt(0, 15);
      var v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

}
