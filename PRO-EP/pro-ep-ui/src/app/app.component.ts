import { Component } from '@angular/core';
import { Utils } from './classes/utils';
import { ICurrentUser } from './interfaces/interfaces';
import { AuthenticationService } from './services/authentication/authentication.service';
import { GlobalsService } from './services/globals/globals.service';
import { UsersService } from './services/users/users.service';
import { ChangeDetectorRef } from '@angular/core';
import { ConfigurationService } from './services/configuration/configuration.service';
import { AppConfig } from './models/app-config';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent {
  title = 'app';
  showSchedule: boolean = false;
  showBlackFilter: boolean = false;
  showScheduleBlackFilter: boolean = false;
  showPopupMessage: boolean = false;
  showHoverMessage: boolean = false;
  userActive: boolean = true;

  isAuthenticated: boolean | undefined = undefined;
  appConfig: AppConfig | undefined = undefined;

  constructor(private globalsService: GlobalsService,
    private usersService: UsersService,
    private authenticationService: AuthenticationService,
    private configurationService: ConfigurationService,
    private cdref: ChangeDetectorRef) {

    this.configurationService.setAppConfiguration();

    this.configurationService.appConfig.subscribe(
      appConfig => {
        if (appConfig) {
          this.appConfig = appConfig;
          if (this.appConfig.activeProfile) {
            console.log(`Environment: ${this.appConfig?.activeProfile}`);
          }

          if (this.appConfig.version) {
            console.log(`Running version v${this.appConfig?.version}`);
          }
          this.checkRedirectToLogin();
        }
      }
    );

    //check/set the authenticated user if there is a server-side session available
    // this.usersService.checkAuthenticatedUser();
    this.authenticationService.checkAuthenticatedUser().subscribe(
      response => {
      }, error => {
        this.authenticationService.isAuthenticated.next(false);
      }
    );

    this.authenticationService.isAuthenticated.subscribe(
      isAuthenticated => {
        this.isAuthenticated = isAuthenticated;
        this.checkRedirectToLogin();
      }
    );


    this.globalsService.showScheduler.subscribe(
      showSchedule => {
        this.showSchedule = showSchedule;
      }
    );

    this.globalsService.showScheduleBlackFilter.subscribe(
      showScheduleBlackFilter => {
        this.showScheduleBlackFilter = showScheduleBlackFilter;
      }
    );

    this.globalsService.showPopupMessage.subscribe(
      showPopupMessage => {
        this.showPopupMessage = showPopupMessage;
      }
    );

    this.globalsService.showHoverMessage.subscribe(
      showHoverMessage => {
        this.showHoverMessage = showHoverMessage;
      }
    );

    this.usersService.currentUser.subscribe(
      currentUser => {
        if (currentUser) {
          if (currentUser.user) {
            this.userActive = currentUser.user.active || false;
          }
        }
      }
    );

  }

  //redirect to login page
  public checkRedirectToLogin(): void {
    if (this.appConfig && this.isAuthenticated == false
      && this.appConfig.assertionUrl && this.appConfig.realm
    ) {
      let loginUrl: string = this.appConfig.assertionUrl + '?wtrealm=' + encodeURI(this.appConfig.realm || '') + '&wa=wsignin1.0';
     // window.location.href = loginUrl;
      // //secure debug login
      // if ((this.appConfig.environment == 'Development' || this.appConfig.environment == 'Validation') && this.appConfig.debug) {
      //   loginUrl = loginUrl + '&wctx=debug%3D';
      // }
    }
  }

  ngOnInit(): void {

    this.globalsService.showBlackFilter.subscribe(
      showBlackFilter => {
        this.showBlackFilter = showBlackFilter;
        this.cdref.detectChanges();
      }
    );

    //check the session every 15 minutes
    // let authService: AuthenticationService = this.authenticationService;
    // setInterval(function () {
    //   authService.setAuthenticatedUser();
    // }, 900000);
    //900000

  }

  //get custom style for the schedule popup
  getScheduleStyle() {
    let resultStyle: Record<string, any> = {};

    //var body = document.body,
    //  html = document.documentElement;

    //var height = "innerHeight" in window
    //  ? window.innerHeight
    //  : document.documentElement.offsetHeight; ;

    //var width = "innerWidth" in window
    //  ? window.innerWidth
    //  : document.documentElement.offsetWidth; ;

    //resultStyle['height'] = (height * 0.9) + 'px';
    //resultStyle['top'] = '8%';
    //resultStyle['width'] = (width * 0.9) + 'px';
    //resultStyle['left'] = '5%';

    if (this.showSchedule) {
      resultStyle['visibility'] = 'visible';
    } else {
      resultStyle['visibility'] = 'hidden';
    }

    return resultStyle;
  }

  blackFilterStyle() {
    var body = document.body,
      html = document.documentElement;

    var height = Math.max(body.scrollHeight, body.offsetHeight,
      html.clientHeight, html.scrollHeight, html.offsetHeight);

    return {
      'height': height + 'px',
    }
  }

  closeModalPopup(): void {
    this.globalsService.hidePopupMessage();
  }

}
