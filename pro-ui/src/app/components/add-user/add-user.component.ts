import {Component, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {GlobalsService} from "../../services/globals/globals.service";
import {LogsService} from "../../services/logs/logs.service";
import {RequestsService} from "../../services/requests/requests.service";
import {ProjectsService} from "../../services/projects/projects.service";
import {ConfigurationService} from "../../services/configuration/configuration.service";
import {UsersService} from "../../services/users/users.service";
import {AuthenticationService} from "../../services/authentication/authentication.service";
import {
  IAuthenticatedUser,
  ICoreHours, IDropDownValue,
  IFormFieldInstance, IFormFieldVariable,
  IProjectMin,
  IRequest,
} from "../../interfaces/interfaces";
import {Utils} from "../../classes/utils";
import { User } from '../../models/data/user';

@Component({
  selector: 'app-add-user',
  templateUrl: './add-user.component.html',
  styleUrls: ['./add-user.component.css'],
})
export class AddUserComponent implements OnInit{
  @HostListener('window:beforeunload') onBeforeUnload(e: any) {

    if (this.changed || this.createForm) {
      e.preventDefault();
      e.returnValue = '';
    }
  }

  @Input() createForm: boolean = false;
  @Input() isUserCalendarVisible = true;
  @Input() isTrainedOnVisible = true;
  @Output() userSaved = new EventEmitter<User>();
  userForm: FormGroup;
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
  tab3UserFields!: IFormFieldInstance[];
  tab4UserFields!: IFormFieldInstance[];
  errorMessage!: string;

  requestTableColumns: string[] = ['RequestType', 'InterviewerEmpName', 'ResourceTeamMemberName', 'RequestDate', 'RequestDetails', 'Decision', 'Notes'];
  noRequestsResultsMessage: string = 'Loading requests...';

  constructor(private fb: FormBuilder,private globalsService: GlobalsService,
  private authenticationService: AuthenticationService,
  private usersService: UsersService,
  private configurationService: ConfigurationService,
  private projectsService: ProjectsService,
  private requestsService: RequestsService,
  private logsService: LogsService) {
    this.userForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      netId: ['', Validators.required],
      uniqueId: ['', Validators.required],
      status: ['', Validators.required],
      preferredFirstName: [''],
      preferredLastName: [''],
      role: ['', Validators.required],
      employmentStatus: ['', Validators.required],
      canNotEditLockedSchedule: [false],
      jobTitle: [''],
      languages: [''],
      manager: [''],
      schedulingLevel: [{ value: '', disabled: false }],
      notes: [''],
      workEmail: ['', Validators.required],
      workPhone: [''],

      badgeId: [''],
      referredBy: [''],
      interviewDate: [''],
      orientationDate: [''],
    });

    if (!this.createForm) {
      this.selectedUser = this.blankUser();
      this.coreHours = {} as ICoreHours;
    }

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
        this.activeProjects = allProjects.filter(x => (x.active /*&& x.projectType !== 'Administrative'*/));
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

  ngOnInit(): void {

    if (this.createForm) {

      //subscribe to the selected user
      this.usersService.selectedUser.subscribe(
        user => {
          this.selectedUser = user;

          if (this.selectedUser) {

            let selectedDempoId: string = this.selectedUser.dempoid;

            //get core hours
         /*   this.usersService.getUserCoreHoursByNetId(this.selectedUser.dempoid).subscribe(
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
            );*/

      /*      if (this.selectedUser.dempoid) {
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
            }*/

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


      this.selectedUser = this.blankUser();
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
    if (this.authenticatedUser?.resourceGroup || this.authenticatedUser?.admin) {
      return false;
    } else {
      return true;
    }
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
   // this.notTrainedOnProjects = this.activeProjects.filter(x => !this.trainedOnProjects.map(y => y.projectID).includes(x.projectID));
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

  onTabChanged(event: any) {
    // The event parameter contains details about the tab change.
    // Based on the new tab, some tasks are performed.
    // For example, loading different data, updating the UI, etc.
    console.log('Tab changed: ', event);
    // Implementation...
    if (event.index == 0) {
      this.isUserCalendarVisible = true;
      this.isTrainedOnVisible = true;
    } else if (event.index == 1) {
      this.isUserCalendarVisible = false;
      this.isTrainedOnVisible = false;
    } else if (event.index == 2) {
      this.isUserCalendarVisible = false;
      this.isTrainedOnVisible = false;
    } else {
      this.isUserCalendarVisible = true;
      this.isTrainedOnVisible = false;

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
    this.tab2UserFields = this.userFormFields.filter(x => x.formFieldVariable.formField.tab == '2');
    this.tab3UserFields = this.userFormFields.filter(x => x.formFieldVariable.formField.tab == '3');
    this.tab4UserFields = this.userFormFields.filter(x => x.formFieldVariable.formField.tab == '4');

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

    return Utils.formatDateOnlyToString(dateToFormat) || '';
  }

  private validateRequiredFields() {

  }
}
