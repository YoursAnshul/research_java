import { Component } from '@angular/core';
import { User } from '../../models/data/user';
import { IDropDownValue, IFormFieldVariable, IProjectGroup } from '../../interfaces/interfaces';
import { MatTableDataSource } from '@angular/material/table';
import { TableHeaderItem } from '../../models/presentation/table-header-item';
import { UsersService } from '../../services/users/users.service';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { Utils } from '../../classes/utils';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent {

  public allUsers: User[] | undefined = undefined;
  public filteredUsers: User[] | undefined = undefined;
  public selectedUser: User | null = null;
  public allUsersSelected: boolean = false;

  public roles: IFormFieldVariable | undefined = undefined;

  //Table definition
  public headerItems: TableHeaderItem[] = [
    new TableHeaderItem(null, null, false, false, false, true),
    new TableHeaderItem('NetID', 'dempoid', false, true, true, false),
    new TableHeaderItem('First Name', 'fname', false, true, true, false),
    new TableHeaderItem('Last Name', 'lname', false, true, true, false),
    new TableHeaderItem('Email Address', 'emailaddr', false, true, true, false),
    new TableHeaderItem('User Status', 'status', true, false, true, false),
    new TableHeaderItem('Role', 'role', true, false, true, false),
    new TableHeaderItem('Temp Start Date', 'tempstartdate', false, false, true, false),
    new TableHeaderItem('Perm Start Date', 'permstartdate', false, false, true, false),
    new TableHeaderItem('Edit Schedule Status', 'canedit', true, false, true, false),
    new TableHeaderItem(null, null, false, false, false, false),
  ];
  public pageSize: number = 10;
  public paginatedUsers: User[] = [];

  //General
  public errorMessage!: string;

  constructor(private usersService: UsersService,
    private configurationService: ConfigurationService,
  ) {

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
        this.paginatedUsers = this.filteredUsers.slice(0, this.pageSize);
      },
      error: (error) => {
        this.errorMessage = <string>(error.message);
        console.log(this.errorMessage);
      },
    });

    //get role form field

    //get scheduling frequency values
    this.configurationService.getFormField('Role').subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.roles = <IFormFieldVariable>response.Subject;
        }
      }
    );

  }

  ngOnInit(): void {
  }

  public toggleUserSelection(user: User): void {
    user.selected = !user.selected;
  }

  public toggleAllUsersSelected(): void {
    this.allUsers?.forEach(user => user.selected = this.allUsersSelected);
  }

  public headerItemsChange(): void {
    this.sortUsers();
  }

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

            if (aValue && bValue) {
              if (typeof aValue === 'string') {
                return aValue.localeCompare(bValue);
              } else {
                return aValue - bValue;
              }
            } else {
              return 0;
            }
          });

        }
      }
      
      this.paginatedUsers = this.filteredUsers.slice(0, this.pageSize);

    }
  }

  public getRoleDisplay(roleId: number): string {
    if (this.roles?.dropDownValues) {
      let role: IDropDownValue | undefined = this.roles.dropDownValues.find(option => option.codeValues == roleId);
      return role?.dropDownItem ? role.dropDownItem : '';
    }
    return '';
  }

  public formatDateOnlyToString(dateToFormat: Date | null | undefined, dashFormat: boolean = false, zeroPad: boolean = true, internationalFormat: boolean = false): string | null {
    return Utils.formatDateOnlyToString(dateToFormat);
  }

}
