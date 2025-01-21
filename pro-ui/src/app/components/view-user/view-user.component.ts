import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { GlobalsService } from "../../services/globals/globals.service";
import { LogsService } from "../../services/logs/logs.service";
import { RequestsService } from "../../services/requests/requests.service";
import { ProjectsService } from "../../services/projects/projects.service";
import { ConfigurationService } from "../../services/configuration/configuration.service";
import { UsersService } from "../../services/users/users.service";
import { AuthenticationService } from "../../services/authentication/authentication.service";
import {
  IAuthenticatedUser,
  ICoreHours, IDropDownValue,
  IFormFieldInstance, IFormFieldVariable,
  IProjectMin,
  IRequest,
} from "../../interfaces/interfaces";
import { Utils } from "../../classes/utils";
import { User } from '../../models/data/user';
import { UnsavedChangesDialogComponent } from '../unsaved-changes-dialog/unsaved-changes-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import {
  MatSnackBar,
  MatSnackBarHorizontalPosition,
  MatSnackBarVerticalPosition,
} from '@angular/material/snack-bar';
import * as moment from 'moment';

@Component({
  selector: 'app-view-user',
  templateUrl: './view-user.component.html',
  styleUrls: ['./view-user.component.css'],
})
export class ViewUserComponent implements OnInit {
  @HostListener('window:beforeunload') onBeforeUnload(e: any) {

    if (this.changed || this.createForm) {
      e.preventDefault();
      e.returnValue = '';
    }
  }

  @Input() createForm: boolean = true;
  @Input() isUserCalendarVisible = true;
  @Input() viewUser: User | undefined;
  @Output() userSaved = new EventEmitter<User>();
  @Output() closewindow = new EventEmitter<void>();
  @Input() showBreadcrum: boolean =  true;
  authenticatedUser!: IAuthenticatedUser;
  selectedUser!: User;
  activeProjects!: IProjectMin[];

  trainedOnProjects!: IProjectMin[];
  notTrainedOnProjects!: IProjectMin[];
  requestCodeDropDown!: IDropDownValue[];
  decisionIdDropDown!: IDropDownValue[];
  requestFormFields: IFormFieldVariable[] = [];
  userFields!: IFormFieldVariable[];
  allUsers: User[] = [];
  coreHours: any = {} as any;
  coreHoursInvalid: boolean = false;
  changed: boolean = false;
  readOnly!: boolean;
  requests: IRequest[] = [];
  userFormFields: IFormFieldInstance[] = [];
  activeFormField!: IFormFieldInstance;
  tab1UserFields!: IFormFieldInstance[];
  tab2UserFields!: IFormFieldInstance[];
  tab2_1UserFields!: IFormFieldInstance[];
  tab2_2UserFields!: IFormFieldInstance[];
  tab2_3UserFields!: IFormFieldInstance[];
  tab3UserFields!: IFormFieldInstance[];
  tab4UserFields!: IFormFieldInstance[];
  public roles: IFormFieldVariable | undefined = undefined;
  public activeProjectsDv: IDropDownValue[] = [];
  errorMessage!: string;

  requestTableColumns: string[] = ['RequestType', 'InterviewerEmpName', 'ResourceTeamMemberName', 'RequestDate', 'RequestDetails', 'Decision', 'Notes'];
  noRequestsResultsMessage: string = 'Loading requests...';
  invalid: boolean = true;
  isUserFormInvalid: boolean = false;
  tab2Invalid: boolean = false;
  previewUrl: string | null = null;
  errorMessageForImage: string | null = null;
  acceptedFormats = ['image/jpeg', 'image/png'];
  maxFileSizeMB = 10;
  isEdit: boolean = true;

  public selectedTab: string = 'scheduling';

  constructor(private fb: FormBuilder, private globalsService: GlobalsService,
    private authenticationService: AuthenticationService,
    private usersService: UsersService,
    private configurationService: ConfigurationService,
    private projectsService: ProjectsService,
    private requestsService: RequestsService,
    private logsService: LogsService, private dialog: MatDialog,
    private snackBar: MatSnackBar,) {
    if (this.viewUser) {
      this.selectedUser = this.viewUser;
    }

    this.configurationService.getFormField('Role').subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.roles = <IFormFieldVariable>response.Subject;
        }
      }
    );

    /*    if (!this.createForm) {
          this.selectedUser = this.blankUser();
          this.coreHours = {} as ICoreHours;
        }*/

    //get user form fields to build the forms
    this.configurationService.getUserFields().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let userFields: IFormFieldVariable[] = <IFormFieldVariable[]>response.Subject;
          this.userFields = userFields;

          //set selected user to a blank user object if creating a new object
          if (!this.createForm) {
            this.selectedUser = this.blankUser();
            //this.selectedUser = {} as IUser;
            //set trained/not trained on if not already set and if we have already populated active projects
            if (this.activeProjects) {
              this.setTrainedOn();
              this.setNotTrainedOn();
            }

            //every time we set the selected user we will remap the user fields and reassign to tabs, but only if we have userFields populated already
            if (this.userFields) {
              this.mapUserFieldsAndAssignTabs(this.selectedUser, this.userFields);
            }
          }
          //if the selected user has been set but we haven't assigned user fields, let's map and assign the fields
          else if (this.selectedUser) {
            this.mapUserFieldsAndAssignTabs(this.selectedUser, userFields);
          }
        }
      }
    );

    //get active projects
    this.projectsService.allProjectsMin.subscribe(
      allProjects => {
        this.activeProjects = allProjects.filter(x => (x.active && x.projectType !== 'Administrative'));
        //setup active projects as an iDropDownValue type
        this.activeProjectsDv = Utils.convertObjectArrayToDropDownValues(this.activeProjects, 'projectID', 'projectName');
        this.setNotTrainedOn();
      }
    );

    //get active users
    this.usersService.allUsersMin.subscribe(
      allUsers => {
        this.allUsers = allUsers;
        this.trySetRequestValues();
      }
    );

    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;

        this.readOnly = this.isReadOnly();
      }
    );

    //get requests dropdown configurations
    this.configurationService.getFormFieldsByTable('Requests').subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let requestFormFields: IFormFieldVariable[] = <IFormFieldVariable[]>response.Subject;

          this.requestFormFields = requestFormFields;
          let requestCodeFormField: IFormFieldVariable | undefined = requestFormFields.find(x => x.formField?.columnName == 'requestCodeID');
          if (requestCodeFormField) {
            this.requestCodeDropDown = requestCodeFormField.dropDownValues || [];
          }
          let decisionFormField: IFormFieldVariable | undefined = requestFormFields.find(x => x.formField?.columnName == 'decisionID');
          if (decisionFormField) {
            this.decisionIdDropDown = decisionFormField.dropDownValues || [];
          }
          this.trySetRequestValues();
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  isCurrentMonth(month: Date | null): boolean {
    if (!month) {
      return false;
    }
  
    const currentMonth = moment.utc().startOf('month');
    const monthToCheck = moment.utc(month).startOf('month');
    
    return currentMonth.isSame(monthToCheck);
  }

  ngOnInit(): void {
    this.isEdit = true;
    if (this.createForm) {

      //subscribe to the selected user
      this.usersService.selectedUser.subscribe(
        user => {
          if (this.viewUser)
            this.selectedUser = this.viewUser;

          if (this.selectedUser) {

            let selectedDempoId: string = this.selectedUser.dempoid;

            //get core hours
            this.usersService.getUserCoreHoursByNetId(this.selectedUser.dempoid).subscribe(
              response => {
                if ((response.Status || '').toUpperCase() == 'SUCCESS') {
                  this.coreHours = <ICoreHours>response.Subject;

                  let rebuildCoreHours: boolean = false;
                  if (!this.coreHours) {
                    rebuildCoreHours = true;
                  } else if (!this.coreHours.month1) {
                    rebuildCoreHours = true;
                  }

                  if (rebuildCoreHours) {

                    this.coreHours = {} as ICoreHours;

                    let ch: any = this.coreHours;
                    let currentDate: Date = new Date();
                    ch.month1 = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1);
                    ch.coreHours1 = 0;
                    ch.month2 = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
                    ch.coreHours2 = 0;
                    for (var i = 1; i < 13; i++) {
                      ch['month' + (i + 2)] = new Date(currentDate.getFullYear(), currentDate.getMonth() + i, 1);
                      ch['coreHours' + (i + 2)] = 0;
                    }
                    ch.dempoid = selectedDempoId;
                    this.coreHours = ch;
                  } else {
                    //set current core hours
                    this.setCurrentCoreHours();
                  }
                }
              }
            );

            if (this.selectedUser.dempoid) {
              //get requests for selected user
              this.requestsService.getRequestsByNetId(this.selectedUser.dempoid).subscribe(
                response => {
                  if (response.Status == 'Success') {
                    this.requests = <IRequest[]>response.Subject;
                    this.setNoRequestsResultsMessage();
                    this.trySetRequestValues();
                  } else {
                    this.setNoRequestsResultsMessage('Error loading requests...');
                    this.errorMessage = response.Message;
                    this.logsService.logError(this.errorMessage);
                    console.log(this.errorMessage);
                  }
                },
                error => {
                  this.errorMessage = <string>(error.message);
                  this.logsService.logError(this.errorMessage);
                  console.log(this.errorMessage);
                  this.setNoRequestsResultsMessage('Error loading requests...');
                }
              );
            }

          }

          //set trained/not trained on if not already set and if we have already populated active projects
          if (this.activeProjects) {
            this.setTrainedOn();
            this.setNotTrainedOn();
          }

          //every time we set the selected user we will remap the user fields and reassign to tabs, but only if we have userFields populated already
          if (this.userFields && !this.createForm) {
            this.mapUserFieldsAndAssignTabs(user, this.userFields);
          }
        }
      );

    }
    if (!this.createForm) {


      if (this.viewUser) {
        this.selectedUser = this.viewUser;
      }
      this.coreHours = {} as ICoreHours;

      //default core hours
      let ch: any = this.coreHours;
      let currentDate: Date = new Date();
      ch.month1 = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1);
      ch.coreHours1 = 0;
      ch.month2 = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
      ch.coreHours2 = 0;
      for (var i = 1; i < 13; i++) {
        console.log(ch);
        ch['month' + (i + 2)] = new Date(currentDate.getFullYear(), currentDate.getMonth() + i, 1);
        ch['coreHours' + (i + 2)] = 0;
      }
      console.log(ch);

      this.coreHours = ch;

      //set trained/not trained on if not already set and if we have already populated active projects
      if (this.activeProjects) {
        this.setTrainedOn();
        this.setNotTrainedOn();
      }

      //every time we set the selected user we will remap the user fields and reassign to tabs, but only if we have userFields populated already
      if (this.userFields && !this.createForm) {
        this.mapUserFieldsAndAssignTabs(this.selectedUser, this.userFields);
      }
    }
  }


  formatDateToShortMonthYear(dateToFormat: Date | null): string | null {
    if (!dateToFormat) {
      return null;
    }

    return Utils.formatDateToShortMonthYearUTC(dateToFormat);

  }

  isReadOnly(): boolean {
    if (this.authenticatedUser?.projectTeam || this.authenticatedUser?.admin) {
      return false;
    } else {
      return true;
    }
  }

  public getRoleDisplay(roleId: number): string {
    if (this.roles?.dropDownValues) {
      let role: IDropDownValue | undefined = this.roles.dropDownValues.find(option => option.codeValues == roleId);
      return role?.dropDownItem ? role.dropDownItem : '';
    }
    return '';
  }

  public getTrainingProjects(trainingData: string): string[] {
    if (trainingData) {
      return trainingData
        .split('|')
        .map(projectId => this.getProjectDisplayName(Number(projectId))).filter(x => x != "");
    } else {
      return [];
    }
  }

  private getProjectDisplayName(projectId: number): string {
    return this.getProjectDisplay(projectId);
  }

  public getProjectDisplay(projectId: number) {
    return this.activeProjectsDv.find((project) => project.codeValues === projectId)?.dropDownItem || '';
  }


  trainedOnChange(project: IProjectMin): void {
    //sort new list of trained on
    this.trainedOnProjects.sort((x, y) => {
      return (x.projectName < y.projectName) ? -1 : (x.projectName > y.projectName) ? 1 : 0;
    });

    this.selectedUser.trainedon = this.trainedOnProjects.map(x => x.projectID.toString()).join('|');
    this.setNotTrainedOn();
    this.changed = true;
    this.globalsService.currentChanges.next(true);
    this.validateRequiredFields();
  }


  setNoRequestsResultsMessage(customMessage: string | null = null): void {
    if (customMessage) {
      this.noRequestsResultsMessage = customMessage;
      return;
    }

    if (this.requests.length < 1) {
      this.noRequestsResultsMessage = 'No requests found...';
    } else {
      this.noRequestsResultsMessage = 'Loading requests...';
    }
  }

  setTrainedOn(): void {
    let trainedOnIds: string[] = [];
    if (this.selectedUser?.trainedon) {
      trainedOnIds = this.selectedUser.trainedon.split('|');
    }
    this.trainedOnProjects = this.activeProjects.filter(x => trainedOnIds.includes(x.projectID.toString()));
  }

  setNotTrainedOn(): void {
    //  this.notTrainedOnProjects = this.activeProjects.filter(x => !this.trainedOnProjects.map(y => y.projectID).includes(x.projectID));
    this.notTrainedOnProjects = this.activeProjects;
  }

  //populate string values for coded fields in requests
  trySetRequestValues(): void {
    if (!(this.requests.length > 0 && this.requestFormFields.length > 0 && this.allUsers.length > 0)) {
      return;
    }

    for (var i = 0; i < this.requests.length; i++) {
      let requestTypeCode = this.requestCodeDropDown.find(x => x.codeValues == this.requests[i].requestCodeId);
      let decisionCode = this.decisionIdDropDown.find(x => x.codeValues == this.requests[i].decisionId);
      let interviewerUser = this.allUsers.find(x => x.dempoid == this.requests[i].interviewerEmpId);
      let resourceUser = this.allUsers.find(x => x.dempoid == this.requests[i].resourceTeamMemberId);

      this.requests[i].requestType = (requestTypeCode ? requestTypeCode.dropDownItem : '');
      this.requests[i].decision = (decisionCode ? decisionCode.dropDownItem : '');
      this.requests[i].interviewerEmpName = (interviewerUser ? (interviewerUser.displayName || '') : '');
      this.requests[i].resourceTeamMemberName = (resourceUser ? (resourceUser.displayName || '') : '');
    }
  }

  onTabChanged(tab: string) {
    this.selectedTab = tab;
    // Based on the new tab, some tasks are performed.
    // console.log('Tab changed: ', event);
    // Implementation...
    this.isUserFormInvalid = this.tab2Invalid;

    // Implementation...
    if (tab == 'scheduling') {
      this.isUserCalendarVisible = true;

    } else if (tab == 'user-details') {
      this.isUserCalendarVisible = false;

      this.isUserFormInvalid = false;
    } else if (tab == 'personal-contact-info') {
      this.isUserCalendarVisible = false;

    } else {
      this.isUserCalendarVisible = false;

    }
  }

  //map/assign user and user fields to tabs
  mapUserFieldsAndAssignTabs(user: User, userFields: IFormFieldVariable[]): void {
    //assign values to form field instances
    this.userFormFields = [];
    let u: any = user;
    for (var property in u) {
      let propertyFormFieldVariable: IFormFieldVariable | undefined = userFields.find(x => (x.formField?.columnName ? x.formField?.columnName.toLowerCase() : null) == property.toLowerCase());
      if (propertyFormFieldVariable) {
        let value: any = u[property];

        let userFormField: IFormFieldInstance = {
          value: value,
          formFieldVariable: propertyFormFieldVariable,
          invalid: false,
          missingRequired: false,
          validationError: false,
          validationMessage: ''
        }

        //disable field requirements for interviewer-only roles
        if (this.authenticatedUser.interviewer && !this.authenticatedUser.admin && !this.authenticatedUser.resourceGroup) {
          userFormField.formFieldVariable.formField.required = false;
        }

        this.userFormFields.push(userFormField);
      }
    }

    //set current core hours
    this.setCurrentCoreHours();

    this.userFormFields.sort((x, y) => {
      return x.formFieldVariable.formField.formOrder - y.formFieldVariable.formField.formOrder;
    });

    this.activeFormField = this.userFormFields.find(x => x.formFieldVariable.formField?.columnName == 'active') as IFormFieldInstance;
    //assign to tab arrays
    this.tab1UserFields = this.userFormFields.filter(x => x.formFieldVariable.formField.tab == '1');
    this.tab2UserFields = this.userFormFields.filter(x => x.formFieldVariable.formField.tab == '2' && x.formFieldVariable.formField.formSection == 0);
    this.tab3UserFields = this.userFormFields.filter(x => x.formFieldVariable.formField.tab == '5');
    this.tab4UserFields = this.userFormFields.filter(x => x.formFieldVariable.formField.tab == '4');
    this.tab2_1UserFields = this.userFormFields.filter(x => x.formFieldVariable.formField.tab == '2' && x.formFieldVariable.formField.formSection == 1);
    this.tab2_2UserFields = this.userFormFields.filter(x => x.formFieldVariable.formField.tab == '2' && x.formFieldVariable.formField.formSection == 2);
    this.tab2_3UserFields = this.userFormFields.filter(x => x.formFieldVariable.formField.tab == '2' && x.formFieldVariable.formField.formSection == 3);

    this.validateRequiredFields();

  }


  setCurrentCoreHours(): void {
    if (!this.coreHours) {
      return;
    }

    //set current core hours
    let currentCoreHours = this.userFormFields.find(x => x.formFieldVariable.formField?.columnName == 'corehours');
    if (currentCoreHours) {
      currentCoreHours.value = this.coreHours.corehours2;
    }


    let ch: any = this.coreHours;
    if (!ch.Month1) {
      let currentDate: Date = new Date();
      ch.Month1 = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1);
      ch.Month2 = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
      for (var i = 1; i < 13; i++) {
        ch['Month' + (i + 2)] = new Date(currentDate.getFullYear(), currentDate.getMonth() + i, 1);
      }
    }

    this.coreHours = ch;

  }

  blankUser(): User {
    return new User();
  }
  //function to return list of numbers from 0 to n-1
  numSequence(n: number): Array<number> {
    return Array(n);
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

  setChanged(): void {
    this.changed = true;
    this.globalsService.currentChanges.next(true);
    this.validateRequiredFields();
  }

  nvlHours(item: any, index: number): void {
    let newValue: any = item['Corehours' + index];
    if (newValue) {
      if (newValue.replace(/\s/g, '') == '') {
        item['Corehours' + index] = null;
      }
    } else {
      item['Corehours' + index] = null;
    }
  }
  formatDateOnlyToString(dateToFormat: Date): string {
    if (!dateToFormat) {
      return '';
    }

    return Utils.formatDateOnlyToDateOnlyString(dateToFormat) || '';
  }

  private validateRequiredFields() {
    this.tab2Invalid = false;
    for (var i = 0; i < this.userFormFields.length; i++) {
      if (this.userFormFields[i].formFieldVariable.formField.required && !this.disableRules(this.userFormFields[i])) {
        //if null
        if (!this.userFormFields[i].value) {
          this.invalid = true;
          this.userFormFields[i].invalid = true;
        } else if (this.userFormFields[i].formFieldVariable.formField.fieldType == 'textbox'
          || this.userFormFields[i].formFieldVariable.formField.fieldType == 'longtextbox') {
          //if text is blank or empty
          if (this.userFormFields[i].value.replace(/\s/g, '') == '') {
            this.invalid = true;
            this.userFormFields[i].invalid = true;
          } else {
            this.invalid = false;
            this.userFormFields[i].invalid = false;
          }
        } else {
          this.userFormFields[i].invalid = false;
        }


      }

      //make sure to correctly set invalid to false for default project when it's not required (it can be ruled invalid before determined that it shouldn't be required, but this above only looks at required fields to mark valid/invalid)
      if (this.userFormFields[i].formFieldVariable.formField?.columnName == 'defaultproject'
        && !this.userFormFields[i].formFieldVariable.formField.required) {
        this.userFormFields[i].invalid = false;
      }

    }
    if (this.tab2UserFields.filter(x => x.invalid).length > 0) { this.tab2Invalid = true; } else { this.tab2Invalid = false; }
    if (!this.tab2Invalid) {
      if (this.tab2_1UserFields.filter(x => x.invalid).length > 0) { this.tab2Invalid = true; } else { this.tab2Invalid = false; }
    }
    //tab validation
    this.invalid = this.userFormFields.filter(x => (x.formFieldVariable.formField.required && x.invalid)).length > 0;

  }

  public editUser() {

  }

  public getActiveStatus(selected: User) {
    let color: string = selected.active ? 'Green' : 'Gray';
    return {
      'background-color': color
    }
  }

  public getSwatchColor(item: string) {
    let color: string = this.activeProjects.find((project) => project.projectName === item)?.projectColor || '';
    return {
      'background-color': color
    }
  }

  public getDefaultProjectColor(projectId: Number) {
    let color: string = this.activeProjects.find((project) => project.projectID === projectId)?.projectColor || '';
    return {
      'background-color': color
    }
  }

  validParentChild(parent: string, parentValue: string, valueIsDropdown: boolean = false): boolean {
    let parentField: IFormFieldInstance | undefined = this.userFormFields.find(x => x.formFieldVariable.formField?.columnName == parent);
    if (!parentField) {
      return false;
    }
    if (!parentField.value) {
      return false;
    }

    if (valueIsDropdown) {
      let parentDropdownValues: IDropDownValue | undefined = (parentField.formFieldVariable.dropDownValues || []).find(x => (x.dropDownItem || '').toUpperCase() == parentValue.toUpperCase());
      if (parentDropdownValues) {
        parentValue = (parentDropdownValues.codeValues || '').toString();
      }
    }

    if (<string>parentField.value == parentValue) {
      return true;
    } else {
      return false;
    }
  }

  openDialog(data: any): void {
    const dialogRef = this.dialog.open(UnsavedChangesDialogComponent, {
      width: '300px',
      data: {
        ...data
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == 'close') {
        this.closewindow.emit();
      }if (result == 'discardChanges') {
        this.isEdit = true;
       this.mapUserFieldsAndAssignTabs(this.selectedUser, this.userFields);
      } else if (result == 'save') {
        this.userSaved.emit(this.selectedUser);
      }
    });
  }

  cancelEdits(){
    this.closewindow.next();
  }

  showUnsavedChangesDialog(): void {
    this.openDialog({
      dialogType: 'error',
      isUserProfile: !this.showBreadcrum
    })
  }

  saveUser(): void {
    this.mapUserFieldsBackToUser();
    //set modified metadata
    this.selectedUser.modBy = this.authenticatedUser.netID;
    this.selectedUser.modDt = new Date();

    //set created metadata (if applicable)
    this.selectedUser.entryBy = this.authenticatedUser.netID;
    this.selectedUser.entryDt = new Date();
    this.selectedUser.userImage = this.previewUrl;

    //pass to save user api to save


      this.usersService.saveUser(this.selectedUser).subscribe(
        response => {
          if ((response.Status || '').toUpperCase() == 'SUCCESS') {

            //make sure the netid is set or core hours won't save
            this.coreHours.dempoid = this.selectedUser.dempoid;
            for (var i = 1; i < 15; i++) {
              this.coreHours[`coreHours${i}`] = this.coreHours[`coreHours${i}`] || "0";
            }
            this.usersService.saveUserCoreHours([this.coreHours]).subscribe(
              response => {
                if ((response.Status || '').toUpperCase() == 'SUCCESS') {
                  this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'User saved successfully', ['OK']));
                } else {
                  this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Partial Success', 'User saved successfully but there may have been an issue saving core hours', ['OK']));
                }

                //set changed to false to re-disable the save button
                this.changed = false;
                this.globalsService.currentChanges.next(false);

                //set ID for "selected" user if user is newly added
                /**   if (this.selectedUser.userid < 1) {
                    let subject: User = <User>response.Subject;
                    this.selectedUser.userid = subject.userid;
                  } */

                //sync users trained on
                //this.usersService.setAllUsersMin();

                // this.usersService.setSelectedUser(this.selectedUser.dempoid);

                //this.userSaved.emit(this.selectedUser);
                console.log('Added User Successfully:', response);
                this.showToastMessage(
                  'User update successfully!',
                  'success'
                );
                this.userSaved.emit(this.selectedUser);
              });
          } else {
            this.showToastMessage(
              'Encountered an error while trying to save user',
              'error'
            );
            this.logsService.logError(response.Message);
          }

        },
        error => {
          this.errorMessage = <string>(error.message);
          this.showToastMessage(
            'Encountered an error while trying to save user',
            'error'
          );
          this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
        }
      );

  }

  //map field values back to user
  mapUserFieldsBackToUser(): void {
    //assign values to user from form field instances
    let su: any = this.selectedUser;
    for (var property in su) {
      let propertyFormFieldInstance: IFormFieldInstance | undefined = this.userFormFields.find(x => (x.formFieldVariable.formField?.columnName ? x.formFieldVariable.formField?.columnName.toLowerCase() : null) == property.toLowerCase());

      if (propertyFormFieldInstance) {
        let value: any = propertyFormFieldInstance.value;

        //turn arrays (comboboxes/multi-select types) into pipe-delimited strings
        if (propertyFormFieldInstance.formFieldVariable.formField.fieldType.includes('combo')) {
          if (Array.isArray(value)) {
            value = (<string[]>(value)).join('|');
          }
        }

        if (property === 'userImage') {
          //only set this if the userImage has not changed
          if (this.selectedUser.userImage === value) {
            su[property] = value;
          }
        } else {
          //set the value for all other properties
          //other than UserImage
          su[property] = value;
        }
      }
    }
    this.selectedUser = su;
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file type
    if (!this.acceptedFormats.includes(file.type)) {
      this.errorMessage = 'Invalid format. Please upload a JPEG or PNG.';
      this.previewUrl = null;
      return;
    }

    // Validate file size
    if (file.size > this.maxFileSizeMB * 1024 * 1024) {
      this.errorMessage = `File size exceeds ${this.maxFileSizeMB} MB.`;
      this.previewUrl = null;
      return;
    }

    // Generate preview
    this.errorMessageForImage = null;
    const reader = new FileReader();
    reader.onload = () => {
      this.previewUrl = reader.result as string;
    };
    reader.readAsDataURL(file);
  }

  disableRules(formField: IFormFieldInstance): boolean {
    //netid
    if (formField.formFieldVariable.formField?.columnName == 'dempoid1' && this.createForm) {
      return true;
    }
    if (formField.formFieldVariable.formField?.columnName == 'empstatus') {
      let permStartDateField: IFormFieldInstance | undefined = this.userFormFields.find(x => x.formFieldVariable.formField?.columnName == 'permstartdate');
      let tempStartDateField: IFormFieldInstance | undefined = this.userFormFields.find(x => x.formFieldVariable.formField?.columnName == 'tempstartdate');

      if (formField.value) {
        if(formField.value == 1) {
          if (permStartDateField) {
            permStartDateField.formFieldVariable.formField.required = true;
            permStartDateField.invalid = false;
          }
          if (tempStartDateField) {
            tempStartDateField.formFieldVariable.formField.required = false;
            tempStartDateField.invalid = false;
          }
        } else if(formField.value == 2) {
          if (permStartDateField) {
            permStartDateField.formFieldVariable.formField.required = false;
            permStartDateField.invalid = false;
          }
          if (tempStartDateField) {
            tempStartDateField.formFieldVariable.formField.required = true;
            tempStartDateField.invalid = false;
          }
        }
      }
    }

    //shoehorn in required temp/perm date checking before anything that could return a true/false
    if (formField.formFieldVariable.formField?.columnName == 'permstartdate' || formField.formFieldVariable.formField?.columnName == 'permstartdate') {
      let empstatus: IFormFieldInstance | undefined = this.userFormFields.find(x => x.formFieldVariable.formField?.columnName == 'empstatus');
      if (empstatus && !empstatus.value) {
        let permStartDateField: IFormFieldInstance | undefined = this.userFormFields.find(x => x.formFieldVariable.formField?.columnName == 'permstartdate');
        let tempStartDateField: IFormFieldInstance | undefined = this.userFormFields.find(x => x.formFieldVariable.formField?.columnName == 'tempstartdate');
        if (permStartDateField) {
          permStartDateField.formFieldVariable.formField.required = true;
          if (permStartDateField.value) {
            permStartDateField.formFieldVariable.formField.required = false;
            permStartDateField.invalid = false;

            if (tempStartDateField) {
              tempStartDateField.formFieldVariable.formField.required = false;
              tempStartDateField.invalid = false;
            }
            this.validateRequiredFields();
          }
        }
        if (tempStartDateField) {
          tempStartDateField.formFieldVariable.formField.required = true;
          if (tempStartDateField.value) {

            if (permStartDateField) {
              permStartDateField.formFieldVariable.formField.required = false;
              permStartDateField.invalid = false;
            }

            tempStartDateField.formFieldVariable.formField.required = false;
            tempStartDateField.invalid = false;
            this.validateRequiredFields();
          }
        }
      }
    }

    //employment type
    if (formField.formFieldVariable.formField?.columnName == 'employmenttype' && this.activeFormField.value == true) {
      formField.value = null;
      return true;
    }

    //core hours
    if (formField.formFieldVariable.formField?.columnName == 'corehours') {
      return true;
    }

    //---------------------------------------------------------------------------
    // checkboxes/dates parent/child relationships
    //---------------------------------------------------------------------------
    //phone screen
    if (formField.formFieldVariable.formField?.columnName == 'phonescreendate' && !this.validParentChild('phonescreen', '1')) {
      formField.value = null;
      return true;
    }

    //introduction email
    if (formField.formFieldVariable.formField?.columnName == 'introemaildate' && !this.validParentChild('introemail', '1')) {
      formField.value = null;
      return true;
    }
   /**
    //face to face
    if (formField.formFieldVariable.formField?.columnName == 'facetofacedate' && !this.validParentChild('facetoface', '1')) {
      formField.value = null;
      return true;
    }
      */

    //welcome email
    if (formField.formFieldVariable.formField?.columnName == 'welcomeemaildate' && !this.validParentChild('welcomeemail', '1')) {
      formField.value = null;
      return true;
    }

    //hiatus start date
    if (formField.formFieldVariable.formField?.columnName == 'hiatusstartdate' && !this.validParentChild('employmenttype', 'hiatus', true)) {
      formField.value = null;
      return true;
    }

    //scheduling level
    if (formField.formFieldVariable.formField?.columnName == 'schedulinglevel') {
      if (!this.validParentChild('role', 'Interviewer', true)) {
        formField.value = null;
        formField.formFieldVariable.formField.required = false;
        formField.invalid = false;
        this.validateRequiredFields();
        return true;
      } else {
        formField.formFieldVariable.formField.required = true;
        this.validateRequiredFields();
      }
    }

    return false;
  }


  clickOnEdit(): void {
    this.isEdit = false;
    this.tab2Invalid = false;
  }

  closeWindow(): void {
    this.closewindow.emit();
  }

  showToastMessage(message: string, type: string): void {
    let snackBarClass = 'success-snackbar';
    if (type === 'error') {
      snackBarClass = 'error-snackbar';
    }

    const horizontalPosition: MatSnackBarHorizontalPosition = 'end';
    const verticalPosition: MatSnackBarVerticalPosition = 'top';

    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: [snackBarClass],
      horizontalPosition: horizontalPosition,
      verticalPosition: verticalPosition,
    });
  }

  displaySchedulingLevelInfo(event: any): void {
    let htmlMessage: string = '<div class="scheduling-info">';

    htmlMessage = htmlMessage + '<p class="ft700">Scheduling Level 1</p>';
    htmlMessage = htmlMessage + "<ul>";
    htmlMessage = htmlMessage + "<li>A shift schedule should be at least 4 hours in length.</li>";
    htmlMessage = htmlMessage + "<li>A shift schedule should be no more than 7 hours in length.</li>";
    htmlMessage = htmlMessage + "<li>A shift schedule for a weekday, Monday thru Friday, should begin at or after 1 PM.</li>";
    htmlMessage = htmlMessage + "<li>A shift schedule for Saturday should begin at or after 9 AM.</li>";
    htmlMessage = htmlMessage + "<li>A shift schedule for Sunday should begin at or after 12 noon.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should at a minimum match their core hours total.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should not exceed 20 hours total.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's schedule should include 1 night shift, until at or after 9 PM, every other week.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's schedule should include 1 weekend shift every other week.</li>";
    htmlMessage = htmlMessage + "<ul><li>A Friday night shift schedule with majority of hours after 5 PM, can only have 1 Friday night per month.</li>";
    htmlMessage = htmlMessage + "<li>A Saturday and/or Sunday shift schedule should be 6 hours minimum.</li></ul>";
    htmlMessage = htmlMessage + "</ul>";
    htmlMessage = htmlMessage + "<br />";
    htmlMessage = htmlMessage + '<p class="ft700">Scheduling Level 2</p>';
    htmlMessage = htmlMessage + "<ul>";
    htmlMessage = htmlMessage + "<li>A shift schedule should be at least 4 hours in length.</li>";
    htmlMessage = htmlMessage + "<li>A shift schedule cannot be exactly 8 hours in length.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should at a minimum match their core hours total.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should not exceed 40 hours total.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's schedule should include 1 night shift, until at or after 9 PM, every other week.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's schedule should include 1 weekend shift every other week.</li>";
    htmlMessage = htmlMessage + "<ul><li>A Friday night shift schedule with majority of hours after 5 PM, can only have 1 Friday night per month.</li>";
    htmlMessage = htmlMessage + "<li>A Saturday and/or Sunday shift schedule should be 6 hours minimum.</li></ul>";
    htmlMessage = htmlMessage + "</ul>";
    htmlMessage = htmlMessage + "<br />";
    htmlMessage = htmlMessage + '<p class="ft700">Scheduling Level 3</p>';
    htmlMessage = htmlMessage + "<ul>";
    htmlMessage = htmlMessage + "<li>A shift schedule cannot be exactly 8 hours in length.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should at a minimum match their core hours total.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should not exceed 40 hours total.</li>";
    htmlMessage = htmlMessage + "</ul></div>";

    let hoverMessage: HTMLElement = <HTMLElement>document.getElementById('hover-message');
    hoverMessage.innerHTML = htmlMessage;

    this.globalsService.showHoverMessage.next(true);

    hoverMessage.style.top = '0px';
    hoverMessage.style.left = event.clientX + 'px';
    hoverMessage.style.zIndex = '1005';
    hoverMessage.style.maxHeight ='calc(100vh - 10px)';
    hoverMessage.style.overflow = 'auto';
    hoverMessage.style.fontSize = '12px';
  }

  hideSchedulingLevelInfo(): void {
    this.globalsService.showHoverMessage.next(false);
  }

  moveToPage(event: MouseEvent): void {
    event.stopPropagation();
    const url = "/scheduling-info";
    const width = 350;
    const height = 1000;
    const left = window.screen.width / 2 - width / 2;
    const top = window.screen.height / 2 - height / 2;

    window.open(
      url,
      "_blank",
      `width=${width},height=${height},top=${top},left=${left},resizable=no,scrollbars=no,toolbar=no,menubar=no,location=no,status=no`
    );
  }

}
