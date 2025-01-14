import { Component, Input, OnInit, Output, EventEmitter, HostListener } from '@angular/core';
import { Utils } from '../../../classes/utils';
import { IActionButton, IAuthenticatedUser, ICoreHours, IDropDownValue, IFormField, IFormFieldInstance, IFormFieldVariable, IPopupMessage, IProjectMin, IRequest, IUser, IUserMin } from '../../../interfaces/interfaces';
import { AuthenticationService } from '../../../services/authentication/authentication.service';
import { ConfigurationService } from '../../../services/configuration/configuration.service';
import { GlobalsService } from '../../../services/globals/globals.service';
import { LogsService } from '../../../services/logs/logs.service';
import { ProjectsService } from '../../../services/projects/projects.service';
import { RequestsService } from '../../../services/requests/requests.service';
import { UsersService } from '../../../services/users/users.service';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.css']
})
export class UserFormComponent implements OnInit {
  @HostListener('window:beforeunload') onBeforeUnload(e: any) {

    if (this.changed || this.createForm) {
      e.preventDefault();
      e.returnValue = '';
    }

  }

  @Input() createForm: boolean = false;
  @Output() userSaved = new EventEmitter<IUser>();

  authenticatedUser!: IAuthenticatedUser;
  selectedUser!: IUser;
  coreHours: any = {} as any;
  activeProjects!: IProjectMin[];
  trainedOnProjects!: IProjectMin[];
  notTrainedOnProjects!: IProjectMin[];
  userFields!: IFormFieldVariable[];
  userFormFields: IFormFieldInstance[] = [];
  tab1UserFields!: IFormFieldInstance[];
  tab2UserFields!: IFormFieldInstance[];
  tab3UserFields!: IFormFieldInstance[];
  tab4UserFields!: IFormFieldInstance[];
  activeFormField!: IFormFieldInstance;
  requestCodeDropDown!: IDropDownValue[];
  decisionIdDropDown!: IDropDownValue[];
  requestFormFields: IFormFieldVariable[] = [];
  requests: IRequest[] = [];
  allUsers: IUserMin[] = [];
  errorMessage!: string;
  changed: boolean = false;
  readOnly!: boolean;
  invalid: boolean = false;
  tab1Invalid: boolean = false;
  tab2Invalid: boolean = false;
  tab3Invalid: boolean = false;
  tab4Invalid: boolean = false;
  tab5Invalid: boolean = false;
  coreHoursInvalid: boolean = false;
  requestTableColumns: string[] = ['RequestType', 'InterviewerEmpName', 'ResourceTeamMemberName', 'RequestDate', 'RequestDetails', 'Decision', 'Notes'];
  noRequestsResultsMessage: string = 'Loading requests...';

  constructor(private globalsService: GlobalsService,
    private authenticationService: AuthenticationService,
    private usersService: UsersService,
    private configurationService: ConfigurationService,
    private projectsService: ProjectsService,
    private requestsService: RequestsService,
    private logsService: LogsService) {

    if (this.createForm) {
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
          if (this.createForm) {
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

    }

    if (!this.createForm) {
      //subscribe to the selected user
      this.usersService.selectedUser.subscribe(
        user => {
          this.selectedUser = user;

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
                    this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
                  }
                },
                error => {
                  this.errorMessage = <string>(error.message);
                  this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
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

  }

  //map/assign user and user fields to tabs
  mapUserFieldsAndAssignTabs(user: IUser, userFields: IFormFieldVariable[]): void {
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

  validateRequiredFields(): void {
    //set fields as invalid
    for (var i = 0; i < this.userFormFields.length; i++) {
      if (this.userFormFields[i].formFieldVariable.formField.required) {
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

    //tab validation
    this.invalid = this.userFormFields.filter(x => (x.formFieldVariable.formField.required && x.invalid)).length > 0;
    if (this.tab1UserFields.filter(x => x.invalid).length > 0) { this.tab1Invalid = true; } else { this.tab1Invalid = false; }
    if (this.tab2UserFields.filter(x => x.invalid).length > 0) { this.tab2Invalid = true; } else { this.tab2Invalid = false; }
    if (this.tab3UserFields.filter(x => x.invalid).length > 0) { this.tab3Invalid = true; } else { this.tab3Invalid = false; }
    if (this.tab4UserFields.filter(x => x.invalid).length > 0) { this.tab4Invalid = true; } else { this.tab4Invalid = false; }

  }

  //save user profile
  saveUserProfile(): void {
    this.mapUserFieldsBackToUser();

    if (this.createForm) {
      if (!confirm('The Net ID entered for this user is "' + this.selectedUser.dempoid + '". Click below to confirm this is the correct Net ID.\r\nThis cannot be changed after confirmation.')) {
        return;
      }
    }

    //set modified metadata
    this.selectedUser.modBy = this.authenticatedUser.netID;
    this.selectedUser.modDt = new Date();

    //set created metadata (if applicable)
    if (this.selectedUser.userid < 1) {
      this.selectedUser.entryBy = this.authenticatedUser.netID;
      this.selectedUser.entryDt = new Date();
    }

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
              if (this.selectedUser.userid < 1) {
                let subject: IUser = <IUser>response.Subject;
                this.selectedUser.userid = subject.userid;
              }

              //sync users trained on
              this.usersService.setAllUsersMin();

              this.usersService.setSelectedUser(this.selectedUser.dempoid);

              this.userSaved.emit(this.selectedUser);

            });

        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save user', ['OK']));
          this.logsService.logError(response.Message);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save user', ['OK']));
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  disableRules(formField: IFormFieldInstance): boolean {
    //netid
    if (formField.formFieldVariable.formField?.columnName == 'dempoid' && !this.createForm) {
      return true;
    }

    //shoehorn in required temp/perm date checking before anything that could return a true/false
    if (formField.formFieldVariable.formField?.columnName == 'permstartdate' || formField.formFieldVariable.formField?.columnName == 'permstartdate') {
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

    //face to face
    if (formField.formFieldVariable.formField?.columnName == 'facetofacedate' && !this.validParentChild('facetoface', '1')) {
      formField.value = null;
      return true;
    }

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

  setTrainedOn(): void {
    let trainedOnIds: string[] = [];
    if (this.selectedUser?.trainedon) {
      trainedOnIds = this.selectedUser.trainedon.split('|');
    }
    this.trainedOnProjects = this.activeProjects.filter(x => trainedOnIds.includes(x.projectID.toString()));
  }

  setNotTrainedOn(): void {
    this.notTrainedOnProjects = this.activeProjects.filter(x => !this.trainedOnProjects.map(y => y.projectID).includes(x.projectID));
  }

  //open modal scheduler
  openUserSchedule(): void {
    //tab index of 0 = Add schedule tab
    this.globalsService.showContextualPopup(0, this.selectedUser.dempoid, null, null);
  }

  uploadPicture(): void {
    //do nothing if current user is interviewer-only
    if (!(this.authenticatedUser?.resourceGroup || this.authenticatedUser?.admin)) {
      return;
    }

    let pictureUpload: HTMLInputElement = (<HTMLInputElement>document.getElementById('picture-upload'));
    pictureUpload.click();
  }

  validatePictureFile(): void {
    let pictureFile: HTMLInputElement = (<HTMLInputElement>document.getElementById('picture-upload'));
    let filePath: string = pictureFile.value;
    let lastSlash: number = 0;
    let extensionDot: number = filePath.lastIndexOf('.');
    let acceptedExtensions: string[] = ['png', 'jpg', 'jpeg'];
    //get last 'slash' in file path
    if (filePath.indexOf('\\') !== -1) {
      lastSlash = filePath.lastIndexOf('\\');
    } else if (filePath.indexOf('/') !== -1) {
      lastSlash = filePath.lastIndexOf('/');
    }

    let fileName: string = filePath.substring(lastSlash + 1, extensionDot);
    let extension: string = filePath.substring(extensionDot + 1, filePath.length).toLowerCase();

    //check if the file's extension is valid
    let validExtension: boolean = false;
    if (acceptedExtensions.indexOf(extension) !== -1) {
      validExtension = true;
    }

    if (!validExtension) {
      //remove file and filename/path if not valid
      if ((pictureFile.files || []).length > 0) {
        pictureFile.files = null;
      }
      pictureFile.value = '';

      this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Error: the file type is not .png, .jpg or .jpeg and cannot be uploaded.', ['OK']));
    } else {
      this.readBase64String();
      this.changed = true;
      this.globalsService.currentChanges.next(true);
    }

  }

  readBase64String(): void {
    let pictureFile: HTMLInputElement = (<HTMLInputElement>document.getElementById('picture-upload'));
    let files: FileList | null = pictureFile.files;

    if (!files) {
      return;
    }

    if (!files.length) {
      return;
    }

    let file: File = files[0];
    let start: number = 0;
    let stop: number = file.size - 1;

    let reader: FileReader = new FileReader();

    let fileBlob: Blob = {} as Blob;
    if (file.slice) {
      fileBlob = file.slice(start, stop + 1);
    }
    //else if (file.webkitSlice) {
    //  blob = file.webkitSlice(start, stop + 1);
    //} else if (file.mozSlice) {
    //  blob = file.mozSlice(start, stop + 1);
    //}

    if (fileBlob.size > 0) {
      try {
        reader.readAsDataURL(fileBlob);
      } catch (err) {
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'An unexpected error occurred. If you are using a very out-of-date browser, you may need to update or use a more modern browser before trying to upload a file', ['OK']));
      }
    }

    //create a local reference of the selected user to reference in the following FileReader callback
    let selectedUser = this.selectedUser;
    reader.onload = function (event) {
      let base64String: string = (reader.result ? reader.result.toString() : '');
      base64String = base64String.replace('data:application/octet-stream;base64,', '');
      selectedUser.userImage = base64String;

      if ((pictureFile.files || []).length > 0) {
        pictureFile.files = null;
      }
      pictureFile.value = '';
    }

  }

  base64toBlob(base64Data: any, contentType: any): Blob {
    contentType = contentType || '';
    var sliceSize = 1024;
    var byteCharacters = atob(base64Data);
    var bytesLength = byteCharacters.length;
    var slicesCount = Math.ceil(bytesLength / sliceSize);
    var byteArrays = new Array(slicesCount);

    for (var sliceIndex = 0; sliceIndex < slicesCount; ++sliceIndex) {
      var begin = sliceIndex * sliceSize;
      var end = Math.min(begin + sliceSize, bytesLength);

      var bytes = new Array(end - begin);
      for (var offset = begin, i = 0; offset < end; ++i, ++offset) {
        bytes[i] = byteCharacters[offset].charCodeAt(0);
      }
      byteArrays[sliceIndex] = new Uint8Array(bytes);
    }
    return new Blob(byteArrays, { type: contentType });
  }

  blankUser(): IUser {
    return {
      userid: 0,
      dempoid: '',
      status: '',
      active: null,
      corehours: null,
      employmenttype: null,
      fname: '',
      lname: '',
      permstartdate: null,
      permenddate: null,
      tempstartdate: null,
      tempenddate: null,
      uinit: '',
      role: null,
      schedulinglevel: null,
      scheduledisplay: null,
      title: '',
      defaultproject: null,
      phonenumber: '',
      phonenumber1: '',
      phonenumber2: '',
      emailaddr: '',
      emailaddr1: '',
      bypasslockout: null,
      aislenumber: null,
      teamgroup: '',
      workgroup: '',
      cubeoffice: null,
      spanish: null,
      language: '',
      canedit: null,
      trainedon: '',
      citidate: null,
      introemail: null,
      introemaildate: null,
      phonescreen: null,
      phonescreendate: null,
      facetoface: null,
      facetofacedate: null,
      welcomeemail: null,
      welcomeemaildate: null,
      orientationdate: null,
      referral: '',
      tempagency: '',
      userImage: null,
      notes: '',
      emercontactnumber: '',
      emercontactnumber2: '',
      emercontactname: '',
      emercontactname2: '',
      emercontactrel: '',
      emercontactrel2: '',
      dob: null,
      state: '',
      city: '',
      zipcode: '',
      homeaddress: '',
      phonenumber3: '',
      hiatusstartdate: null,
      computernumber: '',
      badgeidnumber: '',
      acdagentnumber: '',
      uniqueid: '',
      preferredfname: '',
      preferredlname: '',
      entryFormName: '',
      entryDt: null,
      entryBy: '',
      modDt: null,
      modBy: '',
    };
  }

  formatDateToShortMonthYear(dateToFormat: Date | null): string | null {
    if (!dateToFormat) {
      return null;
    }

    return Utils.formatDateToShortMonthYearUTC(dateToFormat);

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

  formatDateOnlyToString(dateToFormat: Date): string {
    if (!dateToFormat) {
      return '';
    }

    return Utils.formatDateOnlyToString(dateToFormat) || '';
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

  startDatesEmpty(): boolean {
    let permStartDateField: IFormFieldInstance | undefined = this.userFormFields.find(x => x.formFieldVariable.formField?.columnName == 'permstartdate');
    let tempStartDateField: IFormFieldInstance | undefined = this.userFormFields.find(x => x.formFieldVariable.formField?.columnName == 'tempstartdate');

    if (!permStartDateField && !tempStartDateField) {
      return true;
    } else if (!(permStartDateField || {}).value && !(tempStartDateField || {}).value) {
      return true;
    } else {
      return false;
    }
  }

  displaySchedulingLevelInfo(event: any): void {
    let htmlMessage: string = '';

    htmlMessage = htmlMessage + '<p class="bold">Scheduling Level 1</p>';
    htmlMessage = htmlMessage + "<p><ul>";
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
    htmlMessage = htmlMessage + "</ul></p>";
    htmlMessage = htmlMessage + "<br />";
    htmlMessage = htmlMessage + '<p class="bold">Scheduling Level 2</p>';
    htmlMessage = htmlMessage + "<p><ul>";
    htmlMessage = htmlMessage + "<li>A shift schedule should be at least 4 hours in length.</li>";
    htmlMessage = htmlMessage + "<li>A shift schedule cannot be exactly 8 hours in length.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should at a minimum match their core hours total.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should not exceed 40 hours total.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's schedule should include 1 night shift, until at or after 9 PM, every other week.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's schedule should include 1 weekend shift every other week.</li>";
    htmlMessage = htmlMessage + "<ul><li>A Friday night shift schedule with majority of hours after 5 PM, can only have 1 Friday night per month.</li>";
    htmlMessage = htmlMessage + "<li>A Saturday and/or Sunday shift schedule should be 6 hours minimum.</li></ul>";
    htmlMessage = htmlMessage + "</ul></p>";
    htmlMessage = htmlMessage + "<br />";
    htmlMessage = htmlMessage + '<p class="bold">Scheduling Level 3</p>';
    htmlMessage = htmlMessage + "<p><ul>";
    htmlMessage = htmlMessage + "<li>A shift schedule cannot be exactly 8 hours in length.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should at a minimum match their core hours total.</li>";
    htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should not exceed 40 hours total.</li>";
    htmlMessage = htmlMessage + "</ul></p>";

    let hoverMessage: HTMLElement = <HTMLElement>document.getElementById('hover-message');
    hoverMessage.innerHTML = htmlMessage;

    this.globalsService.showHoverMessage.next(true);

    hoverMessage.style.top = '0px';
    hoverMessage.style.left = event.clientX + 'px';
  }

  hideSchedulingLevelInfo(): void {
    this.globalsService.showHoverMessage.next(false);
  }

}
