import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import { Utils } from '../../classes/utils';
import {
  IAdminOptionsVariable,
  IAllConfiguration,
  IAuthenticatedUser,
  IBlockOutDate,
  IDropDownValue,
  IFormField,
  IFormFieldsByTable,
  IFormFieldVariable,
  IProjectMin,
  ITimeCode
} from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { GlobalsService } from '../../services/globals/globals.service';
import { LogsService } from '../../services/logs/logs.service';
import { ProjectsService } from '../../services/projects/projects.service';
import { UserSchedulesService } from '../../services/userSchedules/user-schedules.service';
import {CanComponentDeactivate} from "../../guards/unsaved-changes.guard";
import {UnsavedChangesDialogComponent} from "../../components/unsaved-changes-dialog/unsaved-changes-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import { Router } from '@angular/router';

@Component({
  selector: 'app-configuration',
  templateUrl: './configuration.component.html',
  styleUrls: ['./configuration.component.css']
})
export class ConfigurationComponent implements CanComponentDeactivate {
  @HostListener('window:beforeunload') onBeforeUnload(e: any) {

    if (this.changedFormFields.length > 0 || this.adminOptionsChanged) {
      e.preventDefault();
      e.returnValue = '';
    }

  }

  authenticatedUser!: IAuthenticatedUser;
  allConfiguration!: IAllConfiguration;
  commHubProjects: IProjectMin[] = [];
  selectedCommHubProject!: number;
  commHubFormFields: IFormFieldVariable[] = [];
  filteredCommHubFormFields: IFormFieldVariable[] = [];
  newCommHubField: IFormFieldVariable = {} as IFormFieldVariable;
  errorMessage!: string;
  formFieldsByTable: IFormFieldsByTable[] = [];
  timeCodes: ITimeCode[] = [];
  newBlockOutDate: IBlockOutDate = {} as IBlockOutDate;
  blockOutDates: IBlockOutDate[] = [];
  startEndTimeRequired: boolean = false;
  uniqueKeyTracker: number = 1;
  changedFormFields: IFormFieldVariable[] = [];
  invalidFormFields: IFormFieldVariable[] = [];
  adminOptionsChanged: boolean = false;
  formFieldsInvalid: boolean = false;
  showNewFieldPopup: boolean = false;
  showPopupMessage: boolean = false;
  showHoverMessage: boolean = false;
  showEditChoicesPopup: boolean = false;
  //codelist should be a field in the formfields table, but we don't have time to add a field as of this writing
  codeListLock: string[] = ['ProjectDisplayID', 'employmenttype', 'role', 'schedulinglevel', 'requestCodeID', 'decisionID'];
  commHubTableColumns: string[] = ['Sort', 'FieldName', 'FieldLabel', 'FieldType', 'Required', 'DisplayInTable', 'Hidden', 'RequireConfirmation', 'Actions'];
  newCommHubTableColumns: string[] = ['FieldName', 'FieldLabel', 'FieldType', 'Required', 'DisplayInTable', 'Hidden', 'RequireConfirmation'];
  dropDownTableColumns: string[] = ['Sort', 'CodeValues', 'DropDownItem', 'Hidden'];
  commHubNoResultsMessage: string = '';
  newCommHubChoices: IDropDownValue[] = [{ sortOrder: 1 } as IDropDownValue];
  selectedCommHubFormField: IFormFieldVariable = {} as IFormFieldVariable;
  choicesSaveDisabled: boolean = true;
  choicesSaveInvalid: boolean = false;
  public nextUrl: string | null = null;
  constructor(private configurationService: ConfigurationService,
              private globalsService: GlobalsService,
              private userSchedulesService: UserSchedulesService,
              private authenticationService: AuthenticationService,
              private projectsService: ProjectsService,
              private logsService: LogsService,private dialog: MatDialog,
              private router: Router) {

    this.selectedCommHubFormField.dropDownValues = [];

    //default all day to checked
    this.newBlockOutDate.allDay = true;

    //subscribe to timecodes
    this.userSchedulesService.timeCodes.subscribe(
      timeCodes => {
        timeCodes = timeCodes.filter(x => (x.timeCodeValue > 14 && x.timeCodeValue !== 49));
        this.timeCodes = timeCodes;
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
    this.globalsService.showPopupMessage.subscribe(
      showPopupMessage => {
        this.showPopupMessage = showPopupMessage;
      }
    );

    this.globalsService.showHoverMessage.subscribe(
      showHoverMessage => {
        this.showHoverMessage = showHoverMessage;
      }
    );

    //get block-out dates
    this.getBlockOutDates();

  }

  ngOnInit(): void {

    //set new comm hub field to blank default
    this.setNewCommHubToDefault();

    //get comm hub projects
    this.projectsService.allProjectsMin.subscribe(
      allProjectsMin => {
        this.commHubProjects = allProjectsMin.filter(x => x.projectDisplayId.split('|').includes('4') && x.active);
        //this.commHubProjects = allProjectsMin.filter(x => (x.ProjectDisplayId.split('|').includes('4') && x.ProjectType !== '4'));
      },
      error => this.errorMessage = <any>error
    );

    //set current page for navigation menu to track
    this.globalsService.selectedPage.next('configuration');

    this.setNoResultsCommHubMessage();

    this.getAllConfiguration();

    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;
      }
    );

  }

  formFieldChanged(formField: IFormFieldVariable, dropDownValue: IDropDownValue | null = null): void {
    formField.changed = true;
    formField.invalid = false;

    this.changedFormFields = this.allConfiguration.formFields.filter(x => x.changed);
    if (this.changedFormFields.length > 0) {
      this.globalsService.currentChanges.next(true);
    }

    if (dropDownValue) {
      if (this.invalidValue(dropDownValue.sortOrder)
        || this.invalidValue(dropDownValue.codeValues)
        || this.invalidValue(dropDownValue.dropDownItem)) {
        dropDownValue.invalid = true;
        formField.invalid = true;
      } else {
        dropDownValue.invalid = false;
        formField.invalid = false;
      }
    }

    //required fields
    if ((formField.formField?.columnName ? (formField.formField?.columnName.length < 1 ? true : false) : true)
      || (formField.formField.fieldLabel ? (formField.formField.fieldLabel.length < 1 ? true : false) : true)
      || (formField.formField.fieldType ? (formField.formField.fieldType.length < 1 ? true : false) : true)) {
      formField.invalid = true;
    }


    if (this.allConfiguration.formFields.filter(x => x.invalid).length > 0) {
      this.formFieldsInvalid = true;
    } else {
      this.formFieldsInvalid = false;
    }

    this.invalidFormFields = this.allConfiguration.formFields.filter(x => x.invalid);

  }

  invalidValue(valueToCheck: any): boolean {
    if (valueToCheck == null) {
      return true;
    }

    if (valueToCheck.toString().length < 1) {
      return true;
    }

    return false;
  }
  closeModalPopup(): void {
    this.globalsService.hidePopupMessage();
  }
  addCodeList(formField: IFormFieldVariable): void {
    let newCodeList: IDropDownValue = {} as IDropDownValue;
    newCodeList.tempKey = this.uniqueKeyTracker;
    this.uniqueKeyTracker = this.uniqueKeyTracker + 1;
    if (!formField.dropDownValues) {
      formField.dropDownValues = [];
    }
    formField.dropDownValues.push(newCodeList);
  }

  deleteCodeList(formField: IFormFieldVariable, dv: IDropDownValue): void {
    dv.confirmDelete = false;
    this.formFieldChanged(formField, dv);
    if (formField.dropDownValues) {
      if (dv.tempKey as number > 0) {
        formField.dropDownValues = formField.dropDownValues.filter(x => !(x.tempKey == dv.tempKey));
      } else {
        formField.dropDownValues = formField.dropDownValues.filter(x => !(x.dropDownItem == dv.dropDownItem && x.codeValues == dv.codeValues));
      }
    }
  }

  public saveConfiguration(): void {
    //save configuration variables
    this.configurationService.saveConfigurationVariables(this.allConfiguration.adminOptionsVariables).subscribe(
      response => {
        if (response.Status.toUpperCase() == "SUCCESS") {
          this.adminOptionsChanged = false;
          if (this.changedFormFields.length < 1) {
            this.globalsService.currentChanges.next(false);
          }
          if (response.Subject.errors.length < 1) {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', "Configuration saved successfully!", ['OK']));
          } else {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Errors encounted saving configuration - unaffected fields have been saved:<br />" + response.Subject.Errors.join('<br />'), ['OK']));
          }
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Unable to save configuration:<br />" + response.Subject.Errors.join('<br />'), ['OK']));
        }
      },
      error => {
        this.errorMessage = <any>error;
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', this.errorMessage, ['OK']));
      }
    );
  }

  //save form fields
  public saveFieldNames(): void {
    //form field mod/entry by/date
    for (var i = 0; i < this.changedFormFields.length; i++) {
      //set formfield mod by/date
      this.changedFormFields[i].formField.modBy = this.authenticatedUser.netID;
      this.changedFormFields[i].formField.modDt = new Date();

      if (this.changedFormFields[i].formField.formFieldId < 1) {
        //set formfield entry by/date
        this.changedFormFields[i].formField.entryBy = this.authenticatedUser.netID;
        this.changedFormFields[i].formField.entryDt = new Date();
      }

      //dropdown value mod/entry by/date
      for (var j = 0; j < (this.changedFormFields[i].dropDownValues || []).length; j++) {
        //set dropdown value mod by/date
        (this.changedFormFields[i].dropDownValues || [])[j].modBy = this.authenticatedUser.netID;
        (this.changedFormFields[i].dropDownValues || [])[j].modDt = new Date();

        if ((this.changedFormFields[i].dropDownValues || [])[j].dropDownValueId || 0 < 1) {
          //set dropdown value entry by/date
          (this.changedFormFields[i].dropDownValues || [])[j].entryBy = this.authenticatedUser.netID;
          (this.changedFormFields[i].dropDownValues || [])[j].entryDt = new Date();
        }

      }

    }

    // console.log(this.changedFormFields);

    //save form fields, including associated dropdown values
    this.configurationService.saveFieldNames(this.changedFormFields).subscribe(
      response => {
        if (response.Status.toUpperCase() == "SUCCESS") {
          if (response.Subject.errors.length < 1) {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', "Field Names saved successfully!", ['OK']));
          } else {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Errors encounted saving field names - unaffected fields have been saved:<br />" + response.Subject.Errors.join('<br />'), ['OK']));
          }

          let languageField: IFormFieldVariable | undefined = this.changedFormFields.find(x => x.formField?.columnName == 'language');

          if (languageField) {
            this.configurationService.languages.next(languageField);
          }

          this.changedFormFields = [];

          if (!this.adminOptionsChanged) {
            this.globalsService.currentChanges.next(false);
          }

        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Unable to save field names:<br />" + response.Subject.Errors.join('< br />'), ['OK']));
        }
      },
      error => {
        this.errorMessage = <any>error
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', this.errorMessage, ['OK']));
      }
    );
  }

  //save dropdown values
  public saveCodeLists(): void {
    //mod/entry by/date
    for (var i = 0; i < this.allConfiguration.formFields.length; i++) {
      //dropdown value mod/entry by/date
      for (var j = 0; j < (this.allConfiguration.formFields[i].dropDownValues || []).length; j++) {
        //set dropdown value mod by/date
        (this.allConfiguration.formFields[i].dropDownValues || [])[j].modBy = this.authenticatedUser.netID;
        (this.allConfiguration.formFields[i].dropDownValues || [])[j].modDt = new Date();

        if ((this.allConfiguration.formFields[i].dropDownValues || [])[j].dropDownValueId || 0 < 1) {
          //set dropdown value entry by/date
          (this.allConfiguration.formFields[i].dropDownValues || [])[j].entryBy = this.authenticatedUser.netID;
          (this.allConfiguration.formFields[i].dropDownValues || [])[j].entryDt = new Date();
        }

      }

    }

    this.configurationService.saveCodeLists(this.allConfiguration.formFields).subscribe(
      response => {
        if (response.Status.toUpperCase() == "SUCCESS") {
          if (response.Subject.Errors.length < 1) {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', "Code lists saved successfully!", ['OK']));
          } else {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Errors encounted saving code lists - unaffected fields have been saved:<br />" + response.Subject.Errors.join('<br />'), ['OK']));
          }
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Unable to save code lists:<br />" + response.Subject.Errors.join('<br />'), ['OK']));
        }
      },
      error => {
        this.errorMessage = <any>error
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', this.errorMessage, ['OK']));
      }
    );
  }

  blockOutDateValidation(changedInput: string): void {
    //conflict validation
    let dateConflicts: IBlockOutDate[] = [];
    if (this.blockOutDates) {
      dateConflicts = this.blockOutDates.filter(x => Utils.formatDateOnlyToString(x.blockOutDay) == Utils.formatDateOnlyToString(this.newBlockOutDate.blockOutDay));
      if (dateConflicts.length > 0) {
        this.newBlockOutDate.blockOutDay = null;
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'This date has already been added', ['OK']));
      }
    }

    //previous day validation
    if ((Utils.formatDateOnly(this.newBlockOutDate.blockOutDay) || new Date()) < (Utils.formatDateOnly(new Date) || new Date())) {
      this.newBlockOutDate.blockOutDay = null;
      this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Only current and future dates allowed', ['OK']));
    }

    //uncheck all day if start or end time is selected
    let startEndInputs: string[] = ['start', 'end'];
    if (startEndInputs.includes(changedInput)) {
      this.newBlockOutDate.allDay = false;
      this.startEndTimeRequired = true;
    }

    //either remove or require start/end times based on the status of checking all day
    if (changedInput == 'checkbox') {
      if (this.newBlockOutDate.allDay) {
        this.startEndTimeRequired = false;
        this.newBlockOutDate.startTime = null || '';
        this.newBlockOutDate.endTime = null || '';
      } else {
        this.startEndTimeRequired = true;
      }
    }
  }

  compareSelectValues(v1: any, v2: any): boolean {
    return (v1 || '').toString() === (v2 || '').toString();
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

  formatDateOnlyWithMonthNameToString(dateToFormat: Date | undefined): string {
    if (!dateToFormat) {
      return '';
    }

    return Utils.formatDateOnlyWithMonthNameToString(dateToFormat) || '';
  }

  //get block-out dates (current day and future)
  getBlockOutDates(): void {
    this.configurationService.getBlockOutDates().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.blockOutDates = <IBlockOutDate[]>response.Subject;
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  //add a new block-out date
  addBlockOutDate(): void {
    this.configurationService.saveBlockOutDateWithAudit(this.newBlockOutDate,this.authenticatedUser.netID).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.newBlockOutDate = {} as IBlockOutDate;
          //default all day to checked
          this.newBlockOutDate.allDay = true;
          this.startEndTimeRequired = false;

          this.getBlockOutDates();
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Save successful', ['OK']));
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to save block-out date', ['OK']));
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to save schedule:<br />' + this.errorMessage, ['OK']));
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }

  //remove a block-out date
  deleteBlockOutDate(blockOutDate: IBlockOutDate): void {
    this.configurationService.deleteBlockOutDateWithAudit(blockOutDate,this.authenticatedUser.netID).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Successfully removed block-out date', ['OK']));
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to remove block-out date', ['OK']));
        }

        this.getBlockOutDates();
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to save schedule:<br />' + this.errorMessage, ['OK']));
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }


    getFilteredConfiguration(): IAdminOptionsVariable [] {
    // 9 is Scheduled Lock . All we need is Scheduled Lock .
      return this.allConfiguration?.adminOptionsVariables.filter(x=>x.adminOptionsId===9);
    }

  adminOptionsChange(): void {
    this.adminOptionsChanged = true;
    this.globalsService.currentChanges.next(true);
  }

  lockDate(lockDateValue: number): string {
    let lastDayOfMonth: number = new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).getDate();

    if (lockDateValue > lastDayOfMonth)
      lockDateValue = lastDayOfMonth;

    return (new Date().getMonth() + 1) + '/' + lockDateValue;
  }

  unlockDate(beforeLock: boolean): string {
    let monthModifier: number = 2;
    if (beforeLock)
      monthModifier = 1;

    return (new Date().getMonth() + 1 + monthModifier) + '/1';
  }

  sortCommHubField(sortIncrement: number, formField: IFormFieldVariable): void {

    let startingFormOrder: number = formField.formField.formOrder;

    //get replaced field
    let replacedField: IFormFieldVariable = this.filteredCommHubFormFields.find(x => x.formField.formOrder == (formField.formField.formOrder + sortIncrement)) as IFormFieldVariable;

    //don't decrement if already the first choice
    if (sortIncrement == -1 && formField.formField.formOrder == 1)
      return;

    //increment/decrement sort order
    formField.formField.formOrder = formField.formField.formOrder + sortIncrement;

    //move down
    if (sortIncrement == 1) {
      if (replacedField)
        replacedField.formField.formOrder -= 1;
      else
        //if we don't have a field to replace with and we're moving down, that means we're already at the bottom - set the sort order back
        formField.formField.formOrder = startingFormOrder;
    }

    //move up
    if (sortIncrement == -1) {
      if (replacedField)
        replacedField.formField.formOrder += 1;
    }

    //order array by form order
    this.filteredCommHubFormFields.sort(function (x, y) { return x.formField.formOrder - y.formField.formOrder });
    this.filteredCommHubFormFields = [...this.filteredCommHubFormFields];

    formField.changed = true;
    this.formFieldChanged(formField);
  }

  sortCommHubDropDown(sortIncrement: number, dropDown: IDropDownValue, newFormFieldChoices: boolean = false): void {

    let startingSortOrder: number = dropDown.sortOrder || 0;

    //get replaced choice
    let replacedChoice: IDropDownValue | null = null;
    if (newFormFieldChoices) {
      replacedChoice = this.newCommHubChoices.find(x => x.sortOrder == ((dropDown.sortOrder || 0) + sortIncrement)) as IDropDownValue;
    } else {
      replacedChoice = (this.selectedCommHubFormField.dropDownValues || []).find(x => x.sortOrder == ((dropDown.sortOrder || 0) + sortIncrement)) as IDropDownValue;
    }

    //don't decrement if already the first choice
    if (sortIncrement == -1 && dropDown.sortOrder == 1)
      return;

    //increment/decrement sort order
    dropDown.sortOrder = (dropDown.sortOrder || 0) + sortIncrement;

    //move down
    if (sortIncrement == 1) {
      if (replacedChoice) {
        if (replacedChoice.sortOrder) {
          replacedChoice.sortOrder -= 1;
        }
      }
      else
        //if we don't have a choice to replace with and we're moving down, that means we're already at the bottom - set the sort order back
        dropDown.sortOrder = startingSortOrder;
    }

    //move up
    if (sortIncrement == -1) {
      if (replacedChoice) {
        if (replacedChoice.sortOrder) {
          replacedChoice.sortOrder += 1;
        }
      }
    }
    //order array by sort order
    if (newFormFieldChoices) {
      this.newCommHubChoices.sort(function (x, y) { return (x.sortOrder || 0) - (y.sortOrder || 0) });
      this.newCommHubChoices = [...this.newCommHubChoices];
    } else {
      if (this.selectedCommHubFormField.dropDownValues) {
        this.selectedCommHubFormField.dropDownValues.sort(function (x, y) { return (x.sortOrder || 0) - (y.sortOrder || 0) });
        this.selectedCommHubFormField.dropDownValues = [...this.selectedCommHubFormField.dropDownValues];
      }
    }

    dropDown.changed = true;

    let choicesToCheck: IDropDownValue[] = this.newCommHubChoices;
    if (this.selectedCommHubFormField.formField)
      choicesToCheck = this.selectedCommHubFormField.dropDownValues || [];

    //disabled check
    if (choicesToCheck.filter(x => x.changed).length < 1 || choicesToCheck.filter(x => x.invalid).length > 0) {
      this.choicesSaveDisabled = true;
    } else {
      this.choicesSaveDisabled = false;
    }

    //invalid check
    if (choicesToCheck.filter(x => x.invalid).length > 0) {
      this.choicesSaveInvalid = true;
    } else {
      this.choicesSaveInvalid = false;
    }

  }

  addNewBlankChoice(newChoices: boolean = false): void {
    let newChoice: IDropDownValue = {} as IDropDownValue;
    let fieldName: string = 'new';

    if (newChoices) {
      let sortOrder: number = 1;
      if (this.newCommHubChoices.length > 0) {
        let maxSortOrder: number[] = this.newCommHubChoices.map(x => x.sortOrder).filter(val => !isNaN(val as number)) as number[];
        sortOrder = Math.max(...maxSortOrder) + 1;
      }

      newChoice = { sortOrder: sortOrder } as IDropDownValue;

      this.newCommHubChoices = [...this.newCommHubChoices, newChoice];
    } else {
      let sortOrder: number = 1;
      if ((this.selectedCommHubFormField.dropDownValues || []).length > 0) {

        let maxSortOrder: number[] = [0];

        if (this.selectedCommHubFormField.dropDownValues) {
          this.selectedCommHubFormField.dropDownValues.map(x => x.sortOrder).filter(val => !isNaN(val as number));
        }

        sortOrder = Math.max(...maxSortOrder) + 1;

      }

      newChoice = { formFieldId: this.selectedCommHubFormField.formField.formFieldId, sortOrder: sortOrder } as IDropDownValue;
      fieldName = this.selectedCommHubFormField.formField?.columnName;

      this.selectedCommHubFormField.dropDownValues = [...(this.selectedCommHubFormField.dropDownValues || []), newChoice];
    }

    this.setChangedChoice(newChoice, fieldName);

  }

  setChanged(formField: IFormFieldVariable, name: string): void {
    //adjust the size of the popup if we change the field type
    if (name == 'commhub-fieldtype')
      setTimeout(function () { Utils.modalPopupDynamicSize() }, 100);



    formField.changed = true;
    this.formFieldChanged(formField);
  }

  setChangedChoice(dropdown: IDropDownValue, name: string): void {
    dropdown.changed = true;
    dropdown.invalid = false;
    this.choicesSaveDisabled = false;
    this.choicesSaveInvalid = false;

    let choicesToCheck: IDropDownValue[] = this.newCommHubChoices;
    if (this.selectedCommHubFormField.formField)
      choicesToCheck = this.selectedCommHubFormField.dropDownValues || [];

    //check for duplicate code values
    let duplicateCodeValues: IDropDownValue[] = choicesToCheck.filter(x => x.codeValues == dropdown.codeValues);

    if (duplicateCodeValues.length > 1) {
      dropdown.invalid = true;
    }

    //check for missing values
    if (!dropdown.codeValues || (dropdown.dropDownItem ? (dropdown.dropDownItem.length < 1 ? true : false) : true)) {
      dropdown.invalid = true;
    }

    //disabled check
    if (choicesToCheck.filter(x => x.changed).length < 1 || choicesToCheck.filter(x => x.invalid).length > 0) {
      this.choicesSaveDisabled = true;
    } else {
      this.choicesSaveDisabled = false;
    }

    //invalid check
    if (choicesToCheck.filter(x => x.invalid).length > 0) {
      this.choicesSaveInvalid = true;
    } else {
      this.choicesSaveInvalid = false;
    }

  }

  showEditChoices(FormField: IFormFieldVariable): void {
    this.showEditChoicesPopup = true;
    this.selectedCommHubFormField = FormField;
  }

  closeNewFieldPopup(): void {
    if (this.newCommHubField.changed) {
      if (confirm('You have unsaved changes. Are you sure you want to close this window?')) {
        this.globalsService.hideBlackFilter();
        this.showNewFieldPopup = false;
        this.newCommHubChoices = [{ sortOrder: 1 } as IDropDownValue];
      } else {
        return;
      }
    } else {
      this.globalsService.hideBlackFilter();
      this.showNewFieldPopup = false;
      this.newCommHubChoices = [{ sortOrder: 1 } as IDropDownValue];
    }

    this.setNewCommHubToDefault();
  }

  closeEditChoicesPopup(): void {
    if ((this.selectedCommHubFormField.dropDownValues || []).filter(x => x.changed).length > 0) {
      if (confirm('You have unsaved changes. Are you sure you want to close this window?')) {
        this.globalsService.hideBlackFilter();
        this.showEditChoicesPopup = false;
        this.selectedCommHubFormField = {} as IFormFieldVariable;
        this.selectedCommHubFormField.dropDownValues = [];
      } else {
        return;
      }
    } else {
      this.globalsService.hideBlackFilter();
      this.showEditChoicesPopup = false;
      this.selectedCommHubFormField = {} as IFormFieldVariable;
      this.selectedCommHubFormField.dropDownValues = [];
    }

    this.selectedCommHubFormField = {} as IFormFieldVariable;
    this.selectedCommHubFormField.dropDownValues = [];
  }

  setNoResultsCommHubMessage(customMessage: string | null = null): void {
    if (customMessage) {
      this.commHubNoResultsMessage = customMessage;
      return;
    }

    if (this.commHubFormFields.length < 1) {
      this.commHubNoResultsMessage = 'No communications hub fields found...';
    } else {
      this.commHubNoResultsMessage = 'Loading communications hub fields...';
    }
  }

  saveNewCommHubField(): void {
    //setup new comm hub form field object's dropdown values
    if (this.newCommHubChoices.length > 0) {
      if (this.newCommHubChoices.length == 1 && !this.newCommHubChoices[0].dropDownItem)
        this.newCommHubField.dropDownValues = [];
      else
        this.newCommHubField.dropDownValues = this.newCommHubChoices;
    } else {
      this.newCommHubField.dropDownValues = [];
    }

    this.newCommHubField.formField.defaultLabel = this.newCommHubField.formField.fieldLabel;
    this.newCommHubField.formField.formOrder = Math.max(...this.filteredCommHubFormFields.map(x => (x.formField.formOrder ? x.formField.formOrder : 0))) + 1;

    //set formfield mod by/date
    this.newCommHubField.formField.modBy = this.authenticatedUser.netID;
    this.newCommHubField.formField.modDt = new Date();

    if (this.newCommHubField.formField.formFieldId < 1) {
      //set formfield entry by/date
      this.newCommHubField.formField.entryBy = this.authenticatedUser.netID;
      this.newCommHubField.formField.entryDt = new Date();
    }

    //dropdown value mod/entry by/date
    for (var j = 0; j < this.newCommHubField.dropDownValues.length; j++) {
      //set dropdown value mod by/date
      this.newCommHubField.dropDownValues[j].modBy = this.authenticatedUser.netID;
      this.newCommHubField.dropDownValues[j].modDt = new Date();

      if (this.newCommHubField.dropDownValues[j].dropDownValueId || 0 < 1) {
        //set dropdown value entry by/date
        this.newCommHubField.dropDownValues[j].entryBy = this.authenticatedUser.netID;
        this.newCommHubField.dropDownValues[j].entryDt = new Date();
      }

    }

    //save form field
    this.configurationService.saveFieldNames([this.newCommHubField]).subscribe(
      response => {
        if (response.Status.toUpperCase() == "SUCCESS") {

          if (response.Subject.errors?.length < 1) {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', "New field saved successfully!", ['OK']));
          } else {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Errors encounted saving field:<br />" + response.Subject.Errors.join('<br />'), ['OK']));
          }

        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Unable to save field:<br />" + response.Subject.Errors.join('< br />'), ['OK']));
        }

        this.getAllConfiguration(true);
        this.setNewCommHubToDefault();
        this.globalsService.hideBlackFilter();
        this.showNewFieldPopup = false;

      },
      error => {
        this.errorMessage = <any>error
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', this.errorMessage, ['OK']));
      }
    );

  }

  saveCommHubFields(): void {

    //form field mod/entry by/date
    for (var i = 0; i < this.commHubFormFields.length; i++) {
      //set formfield mod by/date
      this.commHubFormFields[i].formField.modBy = this.authenticatedUser.netID;
      this.commHubFormFields[i].formField.modDt = new Date();

      if (this.commHubFormFields[i].formField.formFieldId < 1) {
        //set formfield entry by/date
        this.commHubFormFields[i].formField.entryBy = this.authenticatedUser.netID;
        this.commHubFormFields[i].formField.entryDt = new Date();
      }

      //dropdown value mod/entry by/date
      for (var j = 0; j < (this.commHubFormFields[i].dropDownValues || []).length; j++) {
        //set dropdown value mod by/date
        (this.commHubFormFields[i].dropDownValues || [])[j].modBy = this.authenticatedUser.netID;
        (this.commHubFormFields[i].dropDownValues || [])[j].modDt = new Date();

        if ((this.commHubFormFields[i].dropDownValues || [])[j].dropDownValueId || 0 < 1) {
          //set dropdown value entry by/date
          (this.commHubFormFields[i].dropDownValues || [])[j].entryBy = this.authenticatedUser.netID;
          (this.commHubFormFields[i].dropDownValues || [])[j].entryDt = new Date();
        }

      }

    }

    //save form field
    this.configurationService.saveFieldNames(this.commHubFormFields).subscribe(
      response => {

        if (response.Status.toUpperCase() == "SUCCESS") {

          if (response.Subject.Errors.length < 1) {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', "Comm Hub field(s) saved successfully!", ['OK']));
          } else {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Errors encounted saving Comm Hub field(s):<br />" + response.Subject.Errors.join('<br />'), ['OK']));
          }

        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Unable to save Comm Hub field(s):<br />" + response.Subject.Errors.join('< br />'), ['OK']));
        }

        this.getAllConfiguration(true);
        this.setNewCommHubToDefault();
        this.globalsService.hideBlackFilter();
        this.showNewFieldPopup = false;

      },
      error => {
        this.errorMessage = <any>error
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', this.errorMessage, ['OK']));
      }
    );

  }

  saveCommHubChoices(choices: IDropDownValue[]): void {
    let formField: IFormFieldVariable = {
      formField: {} as IFormField,
      dropDownValues: choices
    } as IFormFieldVariable;

    //dropdown value mod/entry by/date
    if (!formField.dropDownValues) {
      formField.dropDownValues = [];
    }

    for (var j = 0; j < formField.dropDownValues.length; j++) {
      //set dropdown value mod by/date
      formField.dropDownValues[j].modBy = this.authenticatedUser.netID;
      formField.dropDownValues[j].modDt = new Date();

      if (formField.dropDownValues[j].dropDownValueId || 0 < 1) {
        //set dropdown value entry by/date
        formField.dropDownValues[j].entryBy = this.authenticatedUser.netID;
        formField.dropDownValues[j].entryDt = new Date();
      }

    }

    //save choices
    this.configurationService.saveCodeLists([formField]).subscribe(
      response => {
        if (response.Status.toUpperCase() == "SUCCESS") {
          if (response.Subject.errors.length < 1) {

            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', "Code lists saved successfully!", ['OK']));

            //set changed to false for all choices
            if (!formField.dropDownValues) {
              formField.dropDownValues = [];
            }

            for (var i = 0; i < formField.dropDownValues.length; i++) {
              formField.dropDownValues[i].changed = false;
            }

            this.globalsService.hideBlackFilter();
            this.showEditChoicesPopup = false;

          } else {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Errors encounted saving code lists:<br />" + response.Subject.Errors.join('<br />'), ['OK']));
          }
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', "Unable to save code lists:<br />" + response.Subject.Errors.join('<br />'), ['OK']));
        }
      },
      error => {
        this.errorMessage = <any>error
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', this.errorMessage, ['OK']));
      }
    );

  }

  commHubProjectSelect(): void {
    this.newCommHubField.formField.projectId = this.selectedCommHubProject;
    this.getAllConfiguration(true);
  }

  setNewCommHubToDefault(): void {
    this.newCommHubField = {
      formField: {
        formFieldId: 0,
        fieldLabel: '',
        defaultLabel: '',
        columnName: '',
        tableName: 'Communications Hub',
        formOrder: 0,
        tab: null,
        fieldType: '',
        configurable: 'TRUE',
        required: false,
        displayInTable: false,
        hidden: false,
        projectId: 0,
        requireConfirmation: false,
      } as IFormField,
      dropDownValues: []
    } as IFormFieldVariable;

    if (this.selectedCommHubProject) {
      this.newCommHubField.formField.projectId = this.selectedCommHubProject;
    }

    this.newCommHubChoices = [{ sortOrder: 1 } as IDropDownValue];
  }

  getAllConfiguration(getOnlyCommHub: boolean = false): void {

    this.configurationService.getAllConfiguration().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let allConfiguration: IAllConfiguration = <IAllConfiguration>response.Subject;

          this.allConfiguration = allConfiguration;

          //this.allConfiguration.AdminOptionsVariable.forEach(function (config) {
          //});

          //create arrays of form fields by table
          if (!getOnlyCommHub) {
            this.formFieldsByTable = [];
          }
          this.commHubFormFields = [];
          const uniqueValue = (value: any, index: any, self: any) => {
            return self.indexOf(value) === index
          }

          let outerScope = this;
          var uniqueTables = ['Communications Hub', 'Users', 'Projects', 'Requests'];
          this.allConfiguration.formFields.sort(function (x, y) {
            if (outerScope.toNumber(x.formField.tab) < outerScope.toNumber(y.formField.tab)) {
              return -1;
            } else if (outerScope.toNumber(x.formField.tab) > outerScope.toNumber(y.formField.tab)) {
              return 1;
            } else {
              return x.formField.formOrder - y.formField.formOrder;
            }
          });
          // let commHubFields: IFormFieldVariable[] = this.allConfiguration.formFields.filter(x => x.formField.tableName == 'Communications Hub');
          // let userFields: IFormFieldVariable[] = this.allConfiguration.formFields.filter(x => x.formField.tableName == 'Users');
          // let projectFields: IFormFieldVariable[] = this.allConfiguration.formFields.filter(x => x.formField.tableName == 'Projects');
          // let requestFields: IFormFieldVariable[] = this.allConfiguration.formFields.filter(x => x.formField.tableName == 'Requests');
          // var uniqueTables = this.allConfiguration.formFields.map(x => x.formField.tableName).filter(uniqueValue);

          for (var i = 0; i < uniqueTables.length; i++) {
            let formFields: IFormFieldsByTable = {
              tableName: uniqueTables[i],
              formFields: this.allConfiguration.formFields.filter(x => x.formField.tableName == uniqueTables[i]),
            };

            if (formFields.tableName.toUpperCase() == 'COMMUNICATIONS HUB') {
              this.commHubFormFields = formFields.formFields;
            } else {
              if (!getOnlyCommHub) {
                this.formFieldsByTable.push(formFields);
              }
            }
          }

          //filter the comm hub form fields by selected project
          this.filteredCommHubFormFields = this.commHubFormFields.filter(x => x.formField.projectId == this.selectedCommHubProject);
          //sort the fields by form order
          this.filteredCommHubFormFields.sort(function (x, y) { return x.formField.formOrder - y.formField.formOrder });
          this.filteredCommHubFormFields = [...this.filteredCommHubFormFields];

        }

        this.setNoResultsCommHubMessage();

      },
      error => {
        this.errorMessage = <any>error
        this.setNoResultsCommHubMessage('Error loading communications hub fields');
      }
    );

  }

  convertFieldType(fieldType: string): string {
    switch (fieldType.toUpperCase()) {
      case 'CHECKBOX':
        return 'Checkbox';
        break;
      case 'COLORBOX':
        return 'Color';
        break;
      case 'COMBOBOX':
        return 'Multi Select';
        break;
      case 'DATEONLY':
        return 'Date Only';
        break;
      case 'TIMEONLY':
        return 'Time Only';
        break;
      case 'DATETIME':
        return 'Date/Time';
        break;
      case 'DOB':
        return 'DOB';
        break;
      case 'LONGTEXTBOX':
        return 'Comment';
        break;
      case 'NUMBER':
        return 'Number';
        break;
      case 'EMAIL':
        return 'Email';
        break;
      case 'PHONE':
        return 'Phone';
        break;
      case 'TRAINEDONUSERS':
        return 'Interviewer';
        break;
      case 'RGUSERS':
        return 'Resource Group Users';
        break;
      case 'SELECTBOX':
        return 'Single Select';
        break;
      case 'TEXTBOX':
        return 'Text';
        break;
      case 'TRAINEDONPROJECTS':
        return 'Trained On Projects';
        break;
      default:
        return '';
        break;

    }
  }

  canDeactivate(nextUrl: string | null): boolean {

    if (this.adminOptionsChanged || (typeof this.newBlockOutDate !== 'undefined' && this.newBlockOutDate !== null && this.newBlockOutDate.blockOutDay !=null)) {
      this.nextUrl = nextUrl;
      this.openDialog({
        dialogType: 'error',
        isUserProfile: true
      })
      return false;
    }
    return true;

  }

  openDialog(data: any,): void {
    const dialogRef = this.dialog.open(UnsavedChangesDialogComponent, {
      width: '300px',
      data: {
        ...data
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == 'discardChanges') {
        this.getAllConfiguration();
        this.newBlockOutDate = {} as IBlockOutDate;
        this.adminOptionsChanged = false;
        if(this.nextUrl) {
          this.router.navigateByUrl(this.nextUrl);
        }
      }
    });
  }

  public toNumber(value: any): number {
    if (value == null || value == undefined || value == '') {
      return 0;
    }

    return Number(value);
  }

  protected readonly Utils = Utils;
}
