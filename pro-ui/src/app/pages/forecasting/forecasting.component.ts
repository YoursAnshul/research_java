import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { IAuthenticatedUser } from '../../interfaces/interfaces';
import { UserRole } from '../../models/presentation/enums';


@Component({
  selector: 'app-forecasting',
  templateUrl: './forecasting.component.html',
  styleUrl: './forecasting.component.css'
})
export class ForecastingComponent implements OnInit {

  UserRoles: any = UserRole;

  constructor(

    private authenticationService: AuthenticationService
  ) {
   this.authenticationService.authenticatedUser.subscribe(
        (authenticatedUser) => {
          this.authenticatedUser = authenticatedUser;
        }
      );
  }

  authenticatedUser!: IAuthenticatedUser;

  ngOnInit(): void {

  }

selectedTab: string = 'project-forecasting';
}
