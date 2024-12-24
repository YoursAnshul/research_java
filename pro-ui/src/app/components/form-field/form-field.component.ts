import { Component, Input, OnInit, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { Utils } from '../../classes/utils';
import { IDropDownValue, IFormFieldInstance, IProject, IProjectMin, IKeyValue } from '../../interfaces/interfaces';
import { User } from '../../models/data/user';
@Component({
  selector: 'app-form-field',
  templateUrl: './form-field.component.html',
  styleUrls: ['./form-field.component.css']
})
export class FormFieldComponent implements OnInit {
  @Input() formField!: IFormFieldInstance;
  @Input() contextUser!: User;
  @Input() contextProject!: IProject;
  @Input() allProjectsMin!: IProjectMin[];
  @Input() allUsersMin!: any[];
  @Input() readOnly: boolean = false;
  @Output() formFieldChanged = new EventEmitter<any>();

  customSelectValues: IDropDownValue[] = [];
  missingValuesMessage: string = '';

  fieldTypes: IDropDownValue[] = [];
  rgUsers: IDropDownValue[] = [];
  trainedOnUsers: IDropDownValue[] = [];
  trainedOnProjects: IDropDownValue[] = [];
  allProjects: IDropDownValue[] = [];
  activeProjects: IDropDownValue[] = [];
  inactiveProjects: IDropDownValue[] = [];

  //dob specific variables
  month!: string;
  day!: string;

  //date/time specific variables
  dtDate!: Date;
  dtHours: string = '';
  dtMinutes: string = '';
  dtAmPm: string = '';

  //require confirmation specific
  confirmValue: string = '';

  constructor(private cdref: ChangeDetectorRef) { }

  ngAfterContentChecked() {
    this.cdref.detectChanges();
  }

  ngOnInit(): void {
    //hide hidden dropdown values
    this.formField.formFieldVariable.dropDownValues = (this.formField.formFieldVariable.dropDownValues || []).filter(x => !x.hidden);

    //field types field type
    let fieldTypeKeyValues: IKeyValue[] = [
      { key: 'checkbox', value: 'Checkbox' },
      { key: 'combobox', value: 'Multi Select' },
      { key: 'dateonly', value: 'Date' },
      { key: 'datetime', value: 'Date/Time' },
      { key: 'longtextbox', value: 'Comment' },
      { key: 'number', value: 'Number' },
      { key: 'selectbox', value: 'Single Select' },
      { key: 'textbox', value: 'Text' },
      { key: 'email', value: 'Email' },
      { key: 'phone', value: 'Phone' },
      { key: 'trainedonusers', value: 'Interviewer' },
    ];
    this.fieldTypes = Utils.convertObjectArrayToDropDownValues(fieldTypeKeyValues, 'Key', 'Value');

    //dateonly handling - date only fields should not be treated as UTC
    if (this.formField.formFieldVariable.formField.fieldType == 'dateonly'
      && this.formField.value !== null
    ) {
      let dateString = this.formField.value as string;
      let [year, month, day] = dateString.split("-").map(Number);
      this.formField.value = new Date(year, month - 1, day); // months are 0-indexed in JavaScript
    }

    //dob handling
    this.parseDob();

    //datetime handling
    this.parseDateTime();

    //set if the field was configured as required
    if (this.formField.formFieldVariable.formField.wasRequired == null) {
      if (this.formField.formFieldVariable.formField.required) {
        this.formField.formFieldVariable.formField.wasRequired = true;
      } else {
        this.formField.formFieldVariable.formField.wasRequired = false;
      }
    }

    //set the specific field type from the onset
    if (!this.formField.formFieldVariable.formField.fieldTypeSpecific) {
      this.formField.formFieldVariable.formField.fieldTypeSpecific = this.formField.formFieldVariable.formField.fieldType;
    }

    //filter out projects into different arrays
    if (this.allProjectsMin) {
      //set trained on projects
      if (this.contextUser) {
        if (this.contextUser.trainedon) {
          if (this.formField.formFieldVariable.formField.fieldTypeSpecific == 'trainedonprojects'
            || this.formField.formFieldVariable.formField.fieldTypeSpecific == 'trainedonprojectscombo') {
            this.trainedOnProjects = Utils.convertObjectArrayToDropDownValues(this.allProjectsMin.filter(x => this.contextUser.trainedon.split('|').includes(x.projectID.toString())), 'projectID', 'projectName');
          }
        }
      }

      this.allProjects = Utils.convertObjectArrayToDropDownValues(this.allProjectsMin, 'ProjectId', 'ProjectName');
      this.activeProjects = Utils.convertObjectArrayToDropDownValues(this.allProjectsMin.filter(x => x.active), 'ProjectId', 'ProjectName');
      this.activeProjects = Utils.convertObjectArrayToDropDownValues(this.allProjectsMin.filter(x => !x.active), 'ProjectId', 'ProjectName');
    }

    //set trained on users
    if (this.contextProject) {
      if (this.contextProject.trainedOnUsersMin) {
        if (this.trainedOnUsers.length < 1
          && this.formField.formFieldVariable.formField.fieldTypeSpecific == 'trainedonusers') {
          this.trainedOnUsers = Utils.convertObjectArrayToDropDownValues(this.contextProject.trainedOnUsersMin, 'dempoid', 'displayName');
        }
      }
    }

    //set rgusers (resource group + admin users)
    if (this.allUsersMin) {
      let rgRoles = [1, 2];
      this.rgUsers = Utils.convertObjectArrayToDropDownValues(this.allUsersMin.filter(x => rgRoles.includes(x.role)), 'dempoid', 'displayName');
    }

    //custom select/combo values
    switch (this.formField.formFieldVariable.formField.fieldTypeSpecific) {
      case 'fieldtype':
        this.setCustomSelectBox(this.fieldTypes);
        this.missingValuesMessage = 'No field types found...';
        break;
      case 'rgusers':
        this.setCustomSelectBox(this.rgUsers);
        this.missingValuesMessage = 'No users found in the project team...';
        break;
      case 'trainedonusers':
        this.setCustomSelectBox(this.trainedOnUsers);
        this.missingValuesMessage = 'No users trained on this project...';
        break;
      case 'trainedonprojects':
        this.setCustomSelectBox(this.trainedOnProjects);
        this.missingValuesMessage = 'User is not trained on any projects...';
        break;
      case 'trainedonprojectscombo':
        this.setCustomComboBox(this.trainedOnProjects);
        break;
      case 'activeprojects':
        this.setCustomSelectBox(this.activeProjects);
        break;
      case 'activeprojectscombo':
        this.setCustomComboBox(this.activeProjects);
        break;
      case 'inactiveprojects':
        this.setCustomSelectBox(this.inactiveProjects);
        break;
      case 'inactiveprojectscombo':
        this.setCustomComboBox(this.inactiveProjects);
        break;
      case 'combobox':
        //if combobox type, split out the values delimited by |
        this.formField.value = String(this.formField.value).split('|');
        break;
      case 'comboframe':
        //if combobox type, split out the values delimited by |
        this.formField.value = String(this.formField.value).split('|');
        break;
      default:
    }

  }

  //parse out date of birth into month and day variables
  parseDob(): void {
    if (this.formField.formFieldVariable.formField.fieldType == 'dob' && this.formField.value) {
      if (this.formField.value) {
        let dob: Date = new Date(this.formField.value);
        if (dob) {
          ////set date back to UTC using offset so we don't misinterpret the date
          //dob.setHours(dob.getDate() + (dob.getTimezoneOffset() / 60));
          this.month = Utils.zeroPad(dob.getMonth() + 1);
          this.day = Utils.zeroPad(dob.getDate());
        }
      }
    }

  }

  //parse out date/time into variables
  parseDateTime(): void {
    if (this.formField.formFieldVariable.formField.fieldType == 'datetime' && this.formField.value) {
      if (this.formField.value) {
        let dateTime: Date = new Date(this.formField.value);
        if (dateTime) {
          let hoursH24: number = dateTime.getHours();
          let hoursH12: number = hoursH24;
          if (hoursH24 > 12)
            hoursH12 = hoursH24 - 12;

          this.dtDate = dateTime;
          this.dtHours = Utils.zeroPad(hoursH12);
          this.dtMinutes = Utils.zeroPad(dateTime.getMinutes());
          this.dtAmPm = (hoursH24 >= 12 && hoursH24 !== 0) ? 'PM' : 'AM';
        }
      }
    }

  }

  setCustomSelectBox(customSelectValues: IDropDownValue[]): void {
    this.formField.formFieldVariable.formField.fieldType = 'customselectbox';
    this.customSelectValues = customSelectValues;

    //do not require custom select dropdowns that have no available answers
    if (this.customSelectValues.length < 1) {
      this.formField.formFieldVariable.formField.required = false;
    } else if (this.formField.formFieldVariable.formField.wasRequired) {
      this.formField.formFieldVariable.formField.required = true;
    }
  }

  setCustomComboBox(customSelectValues: IDropDownValue[]): void {
    this.formField.formFieldVariable.formField.fieldType = 'customcombobox';
    this.customSelectValues = customSelectValues;

    //if combobox type, split out the values delimited by |
    this.formField.value = String(this.formField.value).split('|');
  }

  setChanged(event: any): void {
    //create a local string variable that can hold a blank and default to that blank if value is null
    let nvlFieldValue: string = '';
    if (this.formField.value) {
      nvlFieldValue = this.formField.value.toString() as string;
    }

    //dob handling
    if (this.formField.formFieldVariable.formField.fieldType == 'dob') {
      //basic min/max handling
      let dayNumber: number = 1;
      let monthNumber: number = 1;
      //day
      if (this.day || this.day == '0' || this.day == '00') {
        dayNumber = parseInt(this.day);
        if (dayNumber < 1) {
          dayNumber = 1;
        }
        if (dayNumber > 31) {
          dayNumber = 31;
        }

        //convert back to 0 padded string
        this.day = Utils.zeroPad(dayNumber);
      }

      //month
      if (this.month) {
        monthNumber = parseInt(this.month);
        if (monthNumber < 1) {
          monthNumber = 1;
        }
        if (monthNumber > 12) {
          monthNumber = 12;
        }

        //convert back to 0 padded string
        this.month = Utils.zeroPad(monthNumber);
      }

      //validate max day and build date value if both month and day are set
      if (this.month && this.day) {
        let maxDay: number = new Date(new Date().getFullYear(), monthNumber, 0).getDate();
        if (dayNumber > maxDay) {
          dayNumber = maxDay;

          //convert back to 0 padded string
          this.day = Utils.zeroPad(dayNumber);
        }

        this.formField.value = new Date(new Date().getFullYear(), monthNumber - 1, dayNumber);
      }
      //return and don't emit change if we don't have a value for both month and day
      else {
        return;
      }
    }

    //date/time handling
    if (this.formField.formFieldVariable.formField.fieldType == 'datetime') {
      //basic min/max handling
      let hoursNumber: number = 1;
      let minutesNumber: number = 0;
      //hours
      if (this.dtHours || this.dtHours == '0' || this.dtHours == '00') {
        hoursNumber = parseInt(this.dtHours);
        if (hoursNumber < 1) {
          hoursNumber = 1;
        }
        if (hoursNumber > 12) {
          hoursNumber = 12;
        }

        //convert back to 0 padded string
        this.dtHours = Utils.zeroPad(hoursNumber);
      }

      //minutes
      if (this.dtMinutes) {
        minutesNumber = parseInt(this.dtMinutes);
        if (minutesNumber < 0) {
          minutesNumber = 0;
        }
        if (minutesNumber > 60) {
          minutesNumber = 60;
        }

        //convert back to 0 padded string
        this.dtMinutes = Utils.zeroPad(minutesNumber);
      }

      //build out the final date/time variable for the field value
      if (this.dtDate && this.dtHours && (Number(this.dtMinutes) >= 0) && this.dtAmPm) {
   /*     if (this.dtAmPm == 'PM')
          hoursNumber = hoursNumber + 12;*/
       const  timestamp = new Date(this.dtDate.getFullYear(), this.dtDate.getMonth(), this.dtDate.getDate(), hoursNumber, minutesNumber);
        this.formField.value = timestamp.toLocaleString().includes('AM') ? timestamp.toLocaleString().replace(/AM/g, this.dtAmPm) : timestamp.toLocaleString().replace(/PM/g, this.dtAmPm);
        this.formField.validationError = false;
      }
      //return and don't emit change if we don't have a value for everything
      else {
        if (this.dtDate || this.dtHours || (Number(this.dtMinutes) < 0) || this.dtAmPm) {
          this.formField.validationError = true;
          this.formField.invalid = true;
        }
        this.formField.value = null;
      }
    }

    //phone handling
    if (this.formField.formFieldVariable.formField.fieldType == 'phone') {
      let keyboardEvent: KeyboardEvent = event as KeyboardEvent;
      let isBackspace: boolean = false;

      //check for backspace so we can skip the formatPhone function
      if (keyboardEvent) {
        if (keyboardEvent.key == 'Backspace') {
          isBackspace = true;
        }
      }

      let fieldValueString: string = (nvlFieldValue ? (nvlFieldValue) : '');
      if (!isBackspace)
        this.formField.value = this.formatPhone(fieldValueString);

      let confirmField: string = (this.confirmValue ? (this.confirmValue.toString() as string) : '');
      if (!isBackspace)
        this.confirmValue = this.formatPhone(confirmField);
    }

    //require confirmation handling - run before email and phone handing so their error messages take precedence
    if (this.formField.formFieldVariable.formField.requireConfirmation) {
      let currentValueAsString: string = (this.formField.value ? this.formField.value as string : '');

      if (currentValueAsString.length > 0) {
        if (currentValueAsString !== this.confirmValue) {
          this.formField.invalid = true;
          this.formField.validationError = true;
          this.formField.validationMessage = 'Both values must match';
        } else {
          this.formField.invalid = false;
          this.formField.validationError = false;
          this.formField.validationMessage = '';
        }
      }
      else {
        this.formField.validationError = false;
        this.formField.validationMessage = '';
      }
    }

    //email handling
    if (this.formField.formFieldVariable.formField.fieldType == 'email') {
      if (nvlFieldValue.length > 0) {
        if (!/.+\@.+\..+/.test(nvlFieldValue)) {
          this.formField.invalid = true;
          this.formField.validationError = true;
          this.formField.validationMessage = 'Email address must be in a valid format (example@example.xyz)';
        } else {
          if (this.formField.validationMessage !== 'Both values must match') {
            this.formField.invalid = false;
            this.formField.validationError = false;
            this.formField.validationMessage = '';
          }
        }
      }
      else {
        this.formField.validationError = false;
        this.formField.validationMessage = '';
      }
    }

    //emit changed field
    this.formFieldChanged.emit(this.formField);
  }

  formatPhone(phoneNumber: string): string {
    if (phoneNumber.length < 1)
      return '';

    let digitValue: string = phoneNumber.replace(/\D/g, '');

    //limit to 10 digits - formatting excluded
    if (digitValue.length > 10)
      digitValue = digitValue.substring(0, 10);

    if (digitValue.length < 4 && digitValue.length > 0)
      phoneNumber = '(' + digitValue.substring(0, digitValue.length) + ') ';

    if (digitValue.length > 3 && digitValue.length < 7)
      phoneNumber = '(' + digitValue.substring(0, 3) + ') ' + digitValue.substring(3, digitValue.length) + '-';

    if (digitValue.length > 6) {
      phoneNumber = '(' + digitValue.substring(0, 3) + ') ' + digitValue.substring(3, 6) + '-' + digitValue.substring(6, digitValue.length);
    }

    return phoneNumber
  }

  validateKey(event: any) {
    //zipcode handling
    if (this.formField.formFieldVariable.formField?.columnName == 'zipcode') {
      this.numberRestrict(event);
      if (this.formField.value) {
        if (this.formField.value.length >= 5) {
          event.preventDefault();
        }
      }
    }

    //phone handling
    if (this.formField.formFieldVariable.formField.fieldType == 'phone') {
      this.numberRestrict(event);
    }

  }

  numberRestrict(event: any) {
    var charCode = (event.which) ? event.which : event.keyCode;
    // Only Numbers 0-9
    if ((charCode < 48 || charCode > 57)) {
      event.preventDefault();
      return false;
    } else {
      return true;
    }
  }

  compareSelectValues(v1: any, v2: any): boolean {
    return (v1 || '').toString() === (v2 || '').toString();
  }

}
