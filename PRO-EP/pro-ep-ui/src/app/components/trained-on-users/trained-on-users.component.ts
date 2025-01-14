import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { IUserMin } from '../../interfaces/interfaces';

@Component({
  selector: 'app-trained-on-users',
  templateUrl: './trained-on-users.component.html',
  styleUrls: ['./trained-on-users.component.css']
})
export class TrainedOnUsersComponent implements OnInit {
  @Input() trainedOnUsers!: IUserMin[];
  @Input() notTrainedOnUsers!: IUserMin[];
  @Input() listingOnly!: boolean;
  @Output() trainedOnChange = new EventEmitter<IUserMin>();
  userToAdd!: IUserMin | null;
  userToRemove!: IUserMin | null;

  constructor() { }

  ngOnInit(): void {
  }

  addTrainedOn(): void {
    if (!this.userToAdd) {
      return;
    }

    this.userToAdd.selected = false;
    this.userToAdd.changed = true;

    this.trainedOnUsers.push(this.userToAdd);
    this.notTrainedOnUsers.splice(this.notTrainedOnUsers.indexOf(this.userToAdd), 1);
    this.trainedOnChange.emit(this.userToAdd);
    this.userToAdd = null;
  }

  removeTrainedOn(): void {
    if (!this.userToRemove) {
      return;
    }

    this.userToRemove.selected = false;
    this.userToRemove.changed = true;

    this.notTrainedOnUsers.push(this.userToRemove);
    this.trainedOnUsers.splice(this.trainedOnUsers.indexOf(this.userToRemove), 1);
    this.trainedOnChange.emit(this.userToRemove);
    this.userToRemove = null;
  }

  setUserToAdd(userToAdd: IUserMin): void {
    this.userToAdd = userToAdd;
    this.userToRemove = null;

    this.notTrainedOnUsers.forEach(function (user, index) {
      if (user.userid == userToAdd.userid) {
        user.selected = true;
      } else {
        user.selected = false;
      }
    });
  }

  setUserToRemove(userToRemove: IUserMin): void {
    this.userToRemove = userToRemove;
    this.userToAdd = null;

    this.trainedOnUsers.forEach(function (user, index) {
      if (user.userid == userToRemove.userid) {
        user.selected = true;
      } else {
        user.selected = false;
      }
    });
  }

  buttonAllowed(addButton: boolean): boolean {
    if (addButton && !this.userToAdd) {
      return true;
    }

    if (!addButton && !this.userToRemove) {
      return true;
    }

    return false;
  }

}
