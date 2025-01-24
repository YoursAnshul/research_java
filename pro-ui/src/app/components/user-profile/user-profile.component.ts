import { Component } from '@angular/core';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { UsersService } from '../../services/users/users.service';
import { User } from '../../models/data/user';
import { CanComponentDeactivate } from '../../guards/unsaved-changes.guard';
import { MatDialog } from '@angular/material/dialog';
import { UnsavedChangesDialogComponent } from '../unsaved-changes-dialog/unsaved-changes-dialog.component';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css'
})
export class UserProfileComponent implements CanComponentDeactivate  {
  selectedUser!: User;
  public isUnsavedChanges: boolean = false;
  public discardChanges: boolean = false;
  constructor(private authenticationService: AuthenticationService,
    private usersService: UsersService,     private dialog: MatDialog,) {
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.usersService.getUserByNetId(authenticatedUser.netID).subscribe(
          response => {
            if ((response.Status || '').toUpperCase() == 'SUCCESS') {
              this.selectedUser = (<User>response.Subject);
            }
          }
        );
      }
    );
  }
// Guard method
canDeactivate(): boolean {
  if (this.isUnsavedChanges) {
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
      this.discardChanges = true;
    }
  });
}

setUnsavedChanges(value:boolean) {
  this.isUnsavedChanges = value;
}
}
