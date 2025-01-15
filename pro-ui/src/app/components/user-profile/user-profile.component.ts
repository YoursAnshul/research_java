import { Component } from '@angular/core';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { UsersService } from '../../services/users/users.service';
import { User } from '../../models/data/user';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css'
})
export class UserProfileComponent {
  selectedUser!: User;

  constructor(private authenticationService: AuthenticationService,
    private usersService: UsersService) {
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        usersService.getUserByNetId(authenticatedUser.netID).subscribe(
          response => {
            if ((response.Status || '').toUpperCase() == 'SUCCESS') {
              this.selectedUser = (<User>response.Subject);
            }
          }
        );
      }
    );
  }

}
