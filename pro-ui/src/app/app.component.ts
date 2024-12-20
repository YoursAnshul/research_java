import { Component } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import {ConfigurationService} from "./services/configuration/configuration.service";
import {AuthenticationService} from "./services/authentication/authentication.service";
import {UsersService} from "./services/users/users.service";
import { AppConfig } from './models/presentation/app-config';
import { IAuthenticatedUser } from './interfaces/interfaces';
import { GlobalsService } from './services/globals/globals.service';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'pro-ui';
  isAuthenticated: boolean | undefined = undefined;
  appConfig: AppConfig | undefined = undefined;
  userActive: boolean = true;
  authenticatedUser!: IAuthenticatedUser;
  hideHeaderFooter = false;

  constructor(
    private usersService: UsersService,
    private authenticationService: AuthenticationService,
    private configurationService: ConfigurationService,
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer,
    private router: Router
  ) {
    this.configurationService.setAppConfiguration();
    this.matIconRegistry.addSvgIcon(
      'calendar_clock',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/calendar_clock.svg')
    );

    this.matIconRegistry.addSvgIcon(
      'menu',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/menu.svg')

    );

    this.matIconRegistry.addSvgIcon(
      'home',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/home.svg')

    );

    this.matIconRegistry.addSvgIcon(
      'description',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/description.svg')
    );

    this.matIconRegistry.addSvgIcon(
      'list_alt',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/list_alt.svg')
    );

    this.matIconRegistry.addSvgIcon(
      'group',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/group.svg')
    );

    this.matIconRegistry.addSvgIcon(
      'stacks',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/stacks.svg')
    );

    this.matIconRegistry.addSvgIcon(
      'forum',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/forum.svg')
    );

    this.matIconRegistry.addSvgIcon(
      'close',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/close.svg')
    );

    this.matIconRegistry.addSvgIcon(
      'shield_person',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/shield_person.svg')
    );

    this.matIconRegistry.addSvgIcon(
      'calendar_month',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/calendar_month.svg')
    );

    this.matIconRegistry.addSvgIcon(
      'chat',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/chat.svg')
    );

    this.matIconRegistry.addSvgIcon(
      'settings',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/settings.svg')
    );  
    this.matIconRegistry.addSvgIcon(
      'search',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/search.svg')
    );  
    this.matIconRegistry.addSvgIcon(
      'call',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/call.svg')
    );  
    this.matIconRegistry.addSvgIcon(
      'calendar_addon',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/calendar_addon.svg')
    );  
    this.matIconRegistry.addSvgIcon(
      'question_mark',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/question_mark.svg')
    ); 
    this.matIconRegistry.addSvgIcon(
      'quick_book',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/quick_book.svg')
    );   
    this.matIconRegistry.addSvgIcon(
      'person',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/person.svg')
    );     
    this.matIconRegistry.addSvgIcon(
      'visibility',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/visibility.svg')
    );  
    this.matIconRegistry.addSvgIcon(
      'user-square',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/user-square.svg')
    );  
    this.matIconRegistry.addSvgIcon(
      'info',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/info.svg')
    );  
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


    this.usersService.currentUser.subscribe(
      currentUser => {
        if (currentUser) {
          if (currentUser.user) {
            this.userActive = currentUser.user.active || false;
          }
        }
      }
    );
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;
      }
    );
    this.router.events
      .pipe(filter((event: any) => event instanceof NavigationEnd))
      .subscribe(() => {
        const currentRoute = this.router.routerState.snapshot.root.firstChild;
        this.hideHeaderFooter = currentRoute?.data['hideHeaderFooter'] || false;
      });

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
}
