import { Component, OnInit } from '@angular/core';
import { CalendarComponent } from '../../components/calendar/calendar.component';
import { IAuthenticatedUser } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { GlobalsService } from '../../services/globals/globals.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  authenticatedUser!: IAuthenticatedUser;
  errorMessage!: string;

  constructor(private authenticationService: AuthenticationService,
    private globalsService: GlobalsService) { }

  ngOnInit(): void {
    //set current page for navigation menu to track
    this.globalsService.selectedPage.next('home');

    this.authenticationService.setAuthenticatedUser();
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;
      }
    );

    //this.authenticatedUser = this.authenticationService.getAuthenticatedUser();
  }

}
