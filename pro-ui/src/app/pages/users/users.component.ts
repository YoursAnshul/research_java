import {Component, EventEmitter, Input, Output} from '@angular/core';
import { User } from '../../models/data/user';
import { IDropDownValue, IFormFieldVariable, IProjectMin } from '../../interfaces/interfaces';
import { MatTableDataSource } from '@angular/material/table';
import { TableHeaderItem } from '../../models/presentation/table-header-item';
import { UsersService } from '../../services/users/users.service';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { Utils } from '../../classes/utils';
import { ProjectsService } from '../../services/projects/projects.service';
import { SelectedValue } from '../../models/presentation/selected-value';
import { GlobalsService } from '../../services/globals/globals.service';
import { UserActions } from '../../models/presentation/enums';
import {user} from "@joeattardi/emoji-button/dist/icons";

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent {

  public allUsers: User[] | undefined = undefined;
  public filteredUsers: User[] | undefined = undefined;
  public selectedUser!: User ;
  public allUsersSelected: boolean = false;
  @Input() showSelectedUser = false;

  //user status
  public statuses: IFormFieldVariable | undefined = undefined;
  public activeDv: IDropDownValue = {codeValues: 1, dropDownItem: 'Active', sortOrder: 1};
  // public userStatusDv: IDropDownValue[] = [
  //   this.activeDv,
  //   {codeValues: 0, dropDownItem: 'Terminated', sortOrder: 2},
  // ];
  public selectedUserStatus: SelectedValue[] = [new SelectedValue(1, this.activeDv)];

  //edit schedule status
  public canEditDv: IDropDownValue[] = [
    {codeValues: 2, dropDownItem: 'Locked', sortOrder: 1},
    {codeValues: 1, dropDownItem: 'Unlocked', sortOrder: 2},
  ];
  public selectedCanEdit: SelectedValue[] = [];

  //trained on/not trained on
  public trainedOn: number = 1;

  //trained on projects
  public activeProjects: IProjectMin[] = [];
  public activeProjectsDv: IDropDownValue[] = [];
  public selectedProjects: SelectedValue[] = [];

  public roles: IFormFieldVariable | undefined = undefined;
  public selectedRoles: SelectedValue[] = [];
  public createUser: boolean  = false;

  public projectSelected: boolean = false;

  public userStatusAnySelected: boolean = false;
  public canEditAnySelected: boolean = true;
  public projectsAnySelected: boolean = true;
  
  //Table definition
  public netIdHeaderItem: TableHeaderItem = new TableHeaderItem('NetID', 'dempoid', false, true, true, false);
  public firstNameHeaderItem: TableHeaderItem = new TableHeaderItem('First Name', 'fname', false, true, true, false);
  public lastNameHeaderItem: TableHeaderItem = new TableHeaderItem('Last Name', 'lname', false, true, true, false);
  public emailHeaderItem: TableHeaderItem = new TableHeaderItem('Email Address', 'emailaddr', false, true, true, false);
  public userStatusHeaderItem: TableHeaderItem = new TableHeaderItem('User Status', 'status', true, false, true, false, [], true);
  public roleHeaderItem: TableHeaderItem = new TableHeaderItem('Role', 'role', true, false, true, false, undefined, true);
  public canEditHeaderItem: TableHeaderItem = new TableHeaderItem('Edit Schedule Status', 'canedit', true, false, true, false, this.canEditDv, true);
  public headerItems: TableHeaderItem[] = [
    new TableHeaderItem(null, null, false, false, false, true),
    this.netIdHeaderItem,
    this.firstNameHeaderItem,
    this.lastNameHeaderItem,
    this.emailHeaderItem,
    this.userStatusHeaderItem,
    this.roleHeaderItem,
    new TableHeaderItem('Temp Start Date', 'tempstartdate', false, false, true, false),
    new TableHeaderItem('Perm Start Date', 'permstartdate', false, false, true, false),
    this.canEditHeaderItem,
    new TableHeaderItem(null, null, false, false, false, false),
  ];

  public currentPage: number = 1;
  public pageSize: number = 10;
  public paginatedUsers: User[] = [];

  public userActions = UserActions;

  //General
  public errorMessage!: string;

  constructor(private usersService: UsersService,
    private projectsService: ProjectsService,
    private configurationService: ConfigurationService,
    private globalsService: GlobalsService,
  ) {

    this.getAllUsers();

    //get status values
    this.configurationService.getFormField('Status').subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.statuses = <IFormFieldVariable>response.Subject;

          if (this.statuses.dropDownValues) {
            let statusHeaderItem: TableHeaderItem | undefined = this.headerItems.find(x => x.name === 'status');

            if (statusHeaderItem) {
              statusHeaderItem.filterOptions = this.statuses.dropDownValues;
            }
          }

        }
      }
    );

    //get role values
    this.configurationService.getFormField('Role').subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.roles = <IFormFieldVariable>response.Subject;

          if (this.roles.dropDownValues) {
            let roleHeaderItem: TableHeaderItem | undefined = this.headerItems.find(x => x.name === 'role');

            if (roleHeaderItem) {
              roleHeaderItem.filterOptions = this.roles.dropDownValues;
            }
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
      }
    );

  }

  ngOnInit(): void {
    this.userStatusHeaderItem.filterValue = this.selectedUserStatus;
  }

  //set pagination
  public paginate(): void {
    if (this.filteredUsers) {

      //if the filtered users are less than the page size, set the current page to 1
      if (this.filteredUsers.length <= this.pageSize) {
        this.currentPage = 1;
      }

      let maxPage: number = Math.floor((this.filteredUsers || []).length / this.pageSize);
      maxPage = (maxPage == 0 ? 1 : maxPage);
      
      if (this.currentPage < 1) {
        this.currentPage = 1;
      }
      
      if (this.currentPage > maxPage) {
        this.currentPage = maxPage;
      }

      const startIndex = (this.currentPage - 1) * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      this.paginatedUsers = this.filteredUsers.slice(startIndex, endIndex);
    }
  }

  //apply filters
  public applyFilters(): void {

    this.filteredUsers = (this.allUsers || []).
    sort((a, b) => {
      const nameA = a.preferredfname || a.fname;
      const nameB = b.preferredfname || b.fname;
      return nameA.localeCompare(nameB);
    });

    //filter by user status
    if (this.selectedUserStatus.length > 0
      && (this.selectedUserStatus || [new SelectedValue('')])[0].value !== '0'
    ) {
      this.filteredUsers = this.filteredUsers.filter(user => this.selectedUserStatus.map(x => x.value).includes(user.status));
    }

    //filter by can edit
    if (this.selectedCanEdit.length > 0
      && (this.selectedCanEdit || [new SelectedValue('')])[0].value !== '0') {
      this.filteredUsers = this.filteredUsers.filter(user => this.selectedCanEdit.map(x => x.value).includes(user.canedit ? 1 : 2));
    }

    //filter by trained on/not trained on
    if (this.projectSelected
      && this.selectedProjects.length > 0
      && (this.selectedProjects || [new SelectedValue('')])[0].value !== '0') {

      if (this.trainedOn == 1) {

          this.filteredUsers = this.filteredUsers.filter(user =>
            Utils.arrayIncludesAll(
              this.selectedProjects.map(x => x.value.toString()),
              Utils.pipeStringToArray(user.trainedon)
            )
          );

      } else if (this.trainedOn == 0) {
        
        this.filteredUsers = this.filteredUsers.filter(user =>
          !Utils.arrayIncludesAny(
            this.selectedProjects.map(x => x.value.toString()),
            Utils.pipeStringToArray(user.trainedon)
          )
        );

      }
    } else if (this.projectSelected
      && this.selectedProjects.length > 0
      && this.trainedOn == 1) {
      this.filteredUsers = this.filteredUsers.filter(user => (user.trainedon || '').length > 0);
  
    } else if (this.trainedOn == 0) {
      this.filteredUsers = this.filteredUsers.filter(user => !user.trainedon);
    }

    //filter by roles
    if (this.selectedRoles.length > 0
      && (this.selectedRoles || [new SelectedValue('')])[0].value !== '0'
    ) {
      this.filteredUsers = this.filteredUsers.filter(user => this.selectedRoles.map(x => x.value).includes(user.role));
    }

    //filter by netId search
    if (this.netIdHeaderItem.searchValue) {
      this.filteredUsers = this.filteredUsers.filter(user => (user.dempoid || '').toLowerCase
        ().includes((this.netIdHeaderItem.searchValue || '').toLowerCase()));
    }

    //filter by first name search
    if (this.firstNameHeaderItem.searchValue) {
      this.filteredUsers = this.filteredUsers.filter(user => (user.fname || '').toLowerCase
        ().includes((this.firstNameHeaderItem.searchValue || '').toLowerCase()));
    }

    //filter by last name search
    if (this.lastNameHeaderItem.searchValue) {
      this.filteredUsers = this.filteredUsers.filter(user => (user.lname || '').toLowerCase
        ().includes((this.lastNameHeaderItem.searchValue || '').toLowerCase()));
    }

    //filter by email search
    if (this.emailHeaderItem.searchValue) {
      this.filteredUsers = this.filteredUsers.filter(user => (user.emailaddr || '').toLowerCase
        ().includes((this.emailHeaderItem.searchValue || '').toLowerCase()));
    }

    //sort users
    this.sortUsers();

    this.paginate();

  }

  //detect change on quick filters
  public quickFilterChange(event: any = undefined): void {

    this.userStatusHeaderItem.filterValue = this.selectedUserStatus;
    this.canEditHeaderItem.filterValue = this.selectedCanEdit;
    this.applyFilters();

  }

  projectFilterChange(event: any = undefined): void {
    this.projectSelected = true;
    this.quickFilterChange(event);
  }

  //handle change on 
  
  //filter the users on header item change
  public headerItemsChange(headerItems: TableHeaderItem[]): void {
    
    this.selectedUserStatus = this.userStatusHeaderItem.filterValue;
    if ((this.selectedUserStatus.map((x: any) => x.value) || []).includes('0')) {
      this.userStatusAnySelected = true;
    } else {
      this.userStatusAnySelected = false;
    }

    this.selectedCanEdit = this.canEditHeaderItem.filterValue;
    
    if ((this.selectedCanEdit.map((x: any) => x.value) || []).includes('0')) {
      this.canEditAnySelected = true;
    } else {
      this.canEditAnySelected = false;
    }
    this.selectedRoles = this.roleHeaderItem.filterValue;
    this.applyFilters();

  }

  //reset all filters
  public resetFilters(): void {
    this.selectedUserStatus = [new SelectedValue(1, this.activeDv)];
    this.userStatusAnySelected = false;
    // this.quickFilterChange(this.selectedUserStatus);
    this.selectedCanEdit = [new SelectedValue('0')];
    this.canEditAnySelected = true;
    this.selectedRoles = [];
    this.selectedProjects = [new SelectedValue('0')];
    this.projectsAnySelected = true;
    this.trainedOn = 1;
    this.projectSelected = false;

    //reset the inline table header search filters
    this.netIdHeaderItem.searchValue = '';
    this.firstNameHeaderItem.searchValue = '';
    this.lastNameHeaderItem.searchValue = '';
    this.emailHeaderItem.searchValue = '';
    
    //reset the inline table header filters
    this.userStatusHeaderItem.filterValue = this.selectedUserStatus;
    this.canEditHeaderItem.filterValue = this.selectedCanEdit;
    this.roleHeaderItem.filterValue = this.selectedRoles;
    
    this.applyFilters();
  }

  //set a user as selected when clicked/checked
  public toggleUserSelection(user: User): void {
    user.checked = !user.checked;
  }

  //toggle all users selected
  public toggleAllUsersSelected(): void {
    this.filteredUsers?.forEach(user => user.checked = this.allUsersSelected);
    this.applyFilters();
  }

  //sort the users
  public sortUsers(): void {
    if (this.filteredUsers) {
      for (var i = 0; i < this.headerItems.length; i++) {
        if (this.headerItems[i].sortDirection) {
          this.filteredUsers = this.filteredUsers.sort((a, b) => {
            let aValue: any = a[this.headerItems[i].name as keyof User];
            let bValue: any = b[this.headerItems[i].name as keyof User];
            if (this.headerItems[i].sortDirection == 'desc') {
              let temp: any = aValue;
              aValue = bValue;
              bValue = temp;
            }

            if (aValue == null || aValue === '') return (this.headerItems[i].sortDirection == 'desc' ? -1 : 1);
            if (bValue == null || bValue === '') return (this.headerItems[i].sortDirection == 'desc' ? 1 : -1);

            if (aValue && bValue) {
              if (typeof aValue === 'string') {
                return aValue.localeCompare(bValue);
              } else if (aValue instanceof Date && bValue instanceof Date) {
                return aValue.getTime() - bValue.getTime();
              } else {
                return aValue - bValue;
              }
            } else {
              return 0;
            }
          });
        }
      }
      this.paginate();
    }
  }

  //get the display value for a status
  public getStatusDisplay(statusId: string): string {
    if (this.statuses?.dropDownValues) {
      let status: IDropDownValue | undefined = this.statuses.dropDownValues.find(option => option.codeValues == (parseInt(statusId) || 0));
      return status?.dropDownItem ? status.dropDownItem : '';
    }
    return '';
  }

  //get the display value for a role
  public getRoleDisplay(roleId: number): string {
    if (this.roles?.dropDownValues) {
      let role: IDropDownValue | undefined = this.roles.dropDownValues.find(option => option.codeValues == roleId);
      return role?.dropDownItem ? role.dropDownItem : '';
    }
    return '';
  }

  //utility format function
  public formatDateOnlyToString(dateToFormat: Date | null | undefined, dashFormat: boolean = false, zeroPad: boolean = true, internationalFormat: boolean = false): string | null {
    return Utils.formatDateOnlyToDateOnlyString(dateToFormat);
  }

  public makeSelectedUserVisible(user:User){
    this.showSelectedUser=true
    this.selectedUser = user;
  }

  public createNewUser() {
    this.createUser = true;
  }

  onUserAdded(user: any) {
    this.createUser = false;
    this.showSelectedUser =  false;
     //get users
     this.usersService.getAllUsers().subscribe( {
      next: (response) => {

        this.allUsers = <User[]>response.Subject;

        //only users with status
        this.allUsers = this.allUsers.filter(user => user.status);

        //default sort by first name
        this.filteredUsers = this.allUsers.
        sort((a, b) => {
          const nameA = a.preferredfname || a.fname;
          const nameB = b.preferredfname || b.fname;
          return nameA.localeCompare(nameB);
        });

        //default filter by active users
        this.filteredUsers = this.filteredUsers.filter(user => user.status == '1');

        //initial pagination
        this.paginate();
      },
      error: (error) => {
        this.errorMessage = <string>(error.message);
        console.log(this.errorMessage);
      },
    });
   }

  //execute batch action on selected users
  executeUserAction(userAction: UserActions): void {
    if (!this.filteredUsers) {
      return;
    }

    let checkedUsers: User[] = this.filteredUsers.filter(x => x.checked);
    let action = '';

    for (var i = 0; i < checkedUsers.length; i++) {
      checkedUsers[i].checked = false;
      //unlock schedule
      if (userAction == UserActions.Unlock) {
        checkedUsers[i].canEdit = true;
        action = 'User Schedule Unlock';
      }
      //lock schedule
      if (userAction == UserActions.Lock) {
        checkedUsers[i].canEdit = false;
        action = 'User Schedule Lock';
      }
      //deactivate user
      if (userAction == UserActions.Deactivate) {
        checkedUsers[i].active = false;
        action = 'User Deactivation';
      }
      //activate user
      if (userAction == UserActions.Activate) {
        checkedUsers[i].active = true;
        action = 'User Activation';
      }
      
      //unselect all users
      this.allUsersSelected = false;
      this.toggleAllUsersSelected();
      
    }

    //pass to save user api to save
    this.usersService.SaveActiveAndLock(checkedUsers).subscribe(
      response => {

        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          // this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', action + ' complete!', ['OK']));
          this.getAllUsers();
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to execute ' + action + '', ['OK']));
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to execute ' + action + ':<br />' + this.errorMessage, ['OK']));
      }
    );

  }

  getAllUsers() {

    //get users
    this.usersService.getAllUsers().subscribe( {
      next: (response) => {
        
        this.allUsers = <User[]>response.Subject;

        //only users with status
        this.allUsers = this.allUsers.filter(user => user.status);
                
        //default sort by first name
        this.filteredUsers = this.allUsers.
        sort((a, b) => {
          const nameA = a.preferredfname || a.fname;
          const nameB = b.preferredfname || b.fname;
          return nameA.localeCompare(nameB);
        });

        //COMMENT/UNCOMMENT TO SKIP TO THE VIEW USER COMPONENT
        // this.showSelectedUser = true;
        // this.selectedUser = this.filteredUsers.filter(user => user.status == '1')[1];

        //initial pagination
        this.paginate();

        this.applyFilters();

      },
      error: (error) => {
        this.errorMessage = <string>(error.message);
        console.log(this.errorMessage);
      },
    });

  }

  setTrainedOn(trainedOn: number) {
    this.trainedOn = trainedOn;
    this.applyFilters();
  }

}
