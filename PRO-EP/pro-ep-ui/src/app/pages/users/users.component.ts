import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { empty } from 'rxjs';
import { Utils } from '../../classes/utils';
import { IActionButton, IAuthenticatedUser, IDropDownValue, IFormFieldInstance, IFormFieldVariable, IProjectMin, IUser, IUserMin } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { GlobalsService } from '../../services/globals/globals.service';
import { LogsService } from '../../services/logs/logs.service';
import { ProjectsService } from '../../services/projects/projects.service';
import { UsersService } from '../../services/users/users.service';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {

  authenticatedUser!: IAuthenticatedUser;
  allUsers!: IUserMin[];
  activeProjects!: IProjectMin[];
  activeProjectsDv!: IDropDownValue[];
  employmentType!: IFormFieldVariable;
  userFields: IFormFieldVariable[] = [];

  filteredUsers: IUserMin[] = [];
  selectedProjects: string[] = [];
  selectedUser!: IUser;

  showModalPopup: boolean = false;
  trainedOnRadio!: number | null;
  trainedOnRadioDisabled: boolean = true;
  selectAll: boolean = false;
  currentChanges: boolean = false;
  errorMessage!: string;
  initialized: boolean = false;

  noUsersSelected: boolean = true;
  actionsControl: FormControl = new FormControl('0');

  //default to active users
  userListFilter: FormControl = new FormControl('2');
  employmentTypeFilter: FormControl = new FormControl('0');
  userProjectFilter: FormControl = new FormControl(['0']);

  constructor(private globalsService: GlobalsService,
    private authenticationService: AuthenticationService,
    private usersService: UsersService,
    private projectsService: ProjectsService,
    private configurationService: ConfigurationService,
    private logsService: LogsService) {
    this.noUsersSelected = true;
    //get active projects
    this.projectsService.allProjectsMin.subscribe(
      allProjects => {
        this.activeProjects = allProjects.filter(x => (x.active && x.projectType !== 'Administrative'));

        //setup active projects as an iDropDownValue type
        this.activeProjectsDv = Utils.convertObjectArrayToDropDownValues(this.activeProjects, 'projectID', 'projectName');
      }
    );

    //get user form fields to build the forms
    this.configurationService.getUserFields().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.userFields = <IFormFieldVariable[]>response.Subject;

          if (this.userFields) {
            this.employmentType = this.userFields.find(x => x.formField?.columnName == 'employmenttype') as IFormFieldVariable;
          }
        }
      });

    //subscribe to current data changes
    this.globalsService.currentChanges.subscribe(
      currentChanges => {
        this.currentChanges = currentChanges;
      }
    );

  }

  ngOnInit(): void {
    this.noUsersSelected = true;
    //set current page for navigation menu to track
    this.globalsService.selectedPage.next('users');

    //subscribe to the selected user
    this.usersService.selectedUser.subscribe(
      selectedUser => {
        this.selectedUser = selectedUser;
      }
    );

    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;
        //set selected user as the current user if we don't have one already selected

        if (!this.selectedUser.dempoid) {
          this.setSelectedUser(authenticatedUser.netID);
        }
      }
    );

    //subscribe to users
    this.usersService.allUsersMin.subscribe(
      allUsers => {
        this.allUsers = allUsers;

        //initiate user filtering
        this.filterUserList();
        this.checkIfUsersSelected();
      }
    );

  }

  //add user
  addUser(): void {

  }

  //filter user list
  filterUserList(): void {

    //default to all users
    this.filteredUsers = this.allUsers;

    //filter users by status
    switch (this.userListFilter.value) {
      case '1':
        //redundant per above default
        //this.filteredUsers = this.allUsers;
        break;
      case '2':
        this.filteredUsers = this.allUsers.filter(x => x.active);
        break;
      case '3':
        this.filteredUsers = this.allUsers.filter(x => !x.active);
        break;
      default:
    }

    if (this.employmentTypeFilter.value !== '0') {
      console.log(this.employmentTypeFilter.value);
      this.filteredUsers = this.filteredUsers.filter(x => (x.employmenttype ? x.employmenttype.toString() : x.employmenttype) == this.employmentTypeFilter.value);
    }

    //filter users by trained on/not trained on
    if (this.trainedOnRadio && this.selectedProjects) {
      //trained on
      if (this.trainedOnRadio == 1) {
        this.filteredUsers = this.filteredUsers.filter(x => Utils.arrayIncludesAll(this.selectedProjects, x.trainedOnArray));
      }

      //not trained on
      if (this.trainedOnRadio == 2) {
        this.filteredUsers = this.filteredUsers.filter(x => !Utils.arrayIncludesAny(this.selectedProjects, x.trainedOnArray));
      }

    }

    //

  }

  formFieldStyle(index: number) {
    let remainder: number = 0;
    let display: string = 'block';

    if (index > 0) {
      remainder = index % 2;
    }

    if (remainder != 0) {
      display = 'inline-block';
    }

    return {
      'display': display,
    }
  }

  trainedOnRadioToggle(): void {
    //initiate user filtering
    this.filterUserList();
  }

  projectsFilterChange(selectedValues: FormControl): void {
    let selectedValuesArray: string[] = selectedValues.value;
    //this.selectedProjects = this.activeProjects.filter(x => selectedValuesArray.includes(x.ProjectID.toString()));
    this.selectedProjects = this.activeProjects.filter(x => selectedValuesArray.includes(x.projectID.toString())).map(x => x.projectID.toString());

    //if "Any Project" set, uncheck trained on/not trained on radio and disable
    if (selectedValuesArray.length == 1) {
      if (selectedValuesArray[0] == '0') {
        this.trainedOnRadio = null;
        this.trainedOnRadioDisabled = true;
      }
      //else, set disabled to false
      else {
        this.trainedOnRadio = 1;
        this.trainedOnRadioDisabled = false;
        this.trainedOnRadioToggle();
      }
    }

    //initiate user filtering
    this.filterUserList();
  }

  //execute batch action on selected users
  executeAction(): void {
    let checkedUsers: IUserMin[] = this.filteredUsers.filter(x => x.checked);
    let action = '';

    for (var i = 0; i < checkedUsers.length; i++) {
      checkedUsers[i].checked = false;
      //unlock schedule
      if (this.actionsControl.value == '1') {
        checkedUsers[i].canEdit = true;
        action = 'User Schedule Unlock';
      }
      //lock schedule
      if (this.actionsControl.value == '2') {
        checkedUsers[i].canEdit = false;
        action = 'User Schedule Lock';
      }
      //deactivate user
      if (this.actionsControl.value == '3') {
        checkedUsers[i].active = false;
        action = 'User Deactivation';
      }
      //activate user
      if (this.actionsControl.value == '4') {
        checkedUsers[i].active = true;
        action = 'User Activation';
      }
    }

    //pass to save user api to save
    this.usersService.SaveActiveAndLock(checkedUsers).subscribe(
      response => {

        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', action + ' complete!', ['OK']));
          this.filterUserList();
          this.setSelectedUser(this.selectedUser.dempoid);

          //unselect all users
          this.selectAll = false;
          this.toggleSelectAllUsers();
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to execute ' + action + '', ['OK']));
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to execute ' + action + ':<br />' + this.errorMessage, ['OK']));
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

    this.actionsControl = new FormControl('0');

  }

  toggleSelectAllUsers(): void {
    for (var i = 0; i < this.filteredUsers.length; i++) {
      this.filteredUsers[i].checked = this.selectAll;
    }

    this.checkIfUsersSelected();
  }

  userCheckAction(): void {
    this.checkIfUsersSelected();
  }

  checkIfUsersSelected(): void {
    if (this.allUsers.filter(x => x.checked).length > 0) {
       this.noUsersSelected = false;
    } else {
      this.noUsersSelected = true;
    }
  }

  closeModalPopup(): void {
    if (this.showModalPopup) {
      if (confirm('You have unsaved changes. Are you sure you want to close the Add a New User window?')) {
        this.showModalPopup = false;
      }
    }
  }

  //set selected user
  setSelectedUser(netId: string): void {
    //get user to bind form field values
    this.usersService.setSelectedUser(netId);
  }

  //confirmation before going to another user if changes were made
  confirmIfChanges(netId: string): void {

    let leaveFunction: Function = (): void => {
      this.setSelectedUser(netId);
      this.globalsService.currentChanges.next(false);
    }

    if (this.currentChanges) {
      let leaveButton: IActionButton = {
        label: 'Leave',
        callbackFunction: leaveFunction
      }

      let stayButton: IActionButton = {
        label: 'Stay',
        callbackFunction: (): void => { }
      }

      this.globalsService.displayPopupMessage(Utils.generatePopupMessageWithCallbacks('Confirm', '<p>You have unsaved changes for this user.</p><p>Are you sure you want to navigate away?</p>', [leaveButton, stayButton], true));

    } else {
      this.setSelectedUser(netId);
    }

  }

  //callback when user is saved
  userSaved(savedUser: IUser): void {
    let userListInstance: IUserMin | undefined = this.filteredUsers.find(x => x.dempoid == savedUser.dempoid);

    if (userListInstance) {
      userListInstance.active = savedUser.active || undefined;
      userListInstance.language = savedUser.language;
      userListInstance.canEdit = savedUser.canedit || undefined;
      userListInstance.preferredfname = savedUser.preferredfname;
      userListInstance.preferredlname = savedUser.preferredlname;
    }

    this.usersService.setAllUsersMin();
  }

  userCreated(newUser: IUser): void {
    this.usersService.setAllUsersMin();
    this.showModalPopup = false;
  }

}
