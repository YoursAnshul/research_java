import { Component, OnInit } from '@angular/core';
import { UsersService } from '../../services/users/users.service';
import { IScheduleMin, IUser, IUserMin } from '../../interfaces/interfaces';
import { UserSchedulesService } from '../../services/userSchedules/user-schedules.service';

@Component({
  selector: 'app-test',
  templateUrl: './test.component.html',
  styleUrls: ['./test.component.css']
})
export class TestComponent implements OnInit {

  users!: IUser[];
  currentUser!: IUser;
  testStringArray: string[] = ['test1', 'test2'];

  errorMessage!: string;
  allUsers!: IUserMin[];

  constructor(private usersService: UsersService) {
    //subscribe to users
    this.usersService.allUsersMin.subscribe(
      allUsers => {
        this.allUsers = allUsers;
      },
      error => {
        this.errorMessage = <string>(error.message);
      }
    );

 }

  ngOnInit(): void {
    this.usersService.getAllUsers().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.users = <IUser[]>response.Subject;
        }
      }
    );
  }

  setCurrentUser(id: number): void {
    this.usersService.getUserByNetId("").subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.currentUser = <IUser>response.Subject;
        }
      }
    );
  }

  testStringArrayInsert(): void {
    let insertIndex: number = 1;
    let newItem: string = 'test1.5';
    let newStringArray: string[] = [];
    for (var i = 0; i < this.testStringArray.length; i++) {
      if (i == insertIndex) {
        newStringArray.push(newItem);
        newStringArray.push(this.testStringArray[i]);
        i++;
      } else {
        newStringArray.push(this.testStringArray[i]);
      }
    }
    this.testStringArray = newStringArray;
  }

}
