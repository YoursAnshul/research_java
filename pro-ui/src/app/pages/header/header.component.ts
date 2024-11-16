import { Component, OnInit } from '@angular/core';
import { IAuthenticatedUser } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  authenticatedUser!: IAuthenticatedUser;

  constructor(private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;
      }
    );

  }

  public logout(): void {
    this.authenticationService.logout();
  }

}
