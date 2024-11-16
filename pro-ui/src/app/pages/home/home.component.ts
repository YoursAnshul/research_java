import { Component, OnInit } from '@angular/core';
import { CalendarComponent } from '../../components/calendar/calendar.component';
import { IAuthenticatedUser } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';


@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  authenticatedUser!: IAuthenticatedUser;
  errorMessage!: string;

  constructor(private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
    this.authenticationService.setAuthenticatedUser();
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;
      }
    );

    //this.authenticatedUser = this.authenticationService.getAuthenticatedUser();
  }
}
