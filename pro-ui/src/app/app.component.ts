import { Component } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import {ConfigurationService} from "./services/configuration/configuration.service";
import {AuthenticationService} from "./services/authentication/authentication.service";
import {UsersService} from "./services/users/users.service";
import { AppConfig } from './models/presentation/app-config';
import { IAuthenticatedUser } from './interfaces/interfaces';
import { GlobalsService } from './services/globals/globals.service';

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
  hideSidebarAndHeader: boolean = false;

  description1: string =
    'This is a brief description of HERO Together. It will give you a quick overview of what the study is about. This might be helpful for when interviewers take incoming calls from participants inquiring about studies on which the interviewer isn’t trained.';
  description2: string =
    'This is a brief description of Project 42. It will give you a quick overview of what the study is about. This might be helpful for when interviewers take incoming calls from participants inquiring about studies on which the interviewer isn’t trained.';
  description3: string =
    'This is a brief description of Project Eleven. It will give you a quick overview of what the study is about. This might be helpful for when interviewers take incoming calls from participants inquiring about studies on which the interviewer isn’t trained.';
  description4: string =
    'This is a brief description of Project Q. It will give you a quick overview of what the study is about. This might be helpful for when interviewers take incoming calls from participants inquiring about studies on which the interviewer isn’t trained.';
  description5: string =
    'This is a brief description of Purple Project. It will give you a quick overview of what the study is about. This might be helpful for when interviewers take incoming calls from participants inquiring about studies on which the interviewer isn’t trained.';
    description6: string =
    'This is a brief description of XYZ Trial. It will give you a quick overview of what the study is about. This might be helpful for when interviewers take incoming calls from participants inquiring about studies on which the interviewer isn’t trained.';

  firstHeading = 'HERO Together';
  secondHeading = 'Project 42';
  thirdHeading = 'Project Eleven';
  fourtHeading = 'Project Q';
  fifthHeading = 'Purple Project';
  sixthHeading = 'XYZ Trial';

  tollFree1 = 'Toll-Free Phone #';
  tollFree2 = 'Toll-Free Phone #';
  tollFree3 = 'Toll-Free Phone #';
  tollFree4 = 'Toll-Free Phone #';
  tollFree5 = 'Toll-Free Phone #';
  tollFree6 = 'Toll-Free Phone #';

  tollFreeNum1 = '1-800-999-8888';
  tollFreeNum2 = '1-800-999-8888';
  tollFreeNum3 = '1-800-999-8888';
  tollFreeNum4 = '1-800-999-8888';
  tollFreeNum5 = '1-800-999-8888';
  tollFreeNum6 = '1-800-999-8888';

  email1 = 'Email Address';
  email2 = 'Email Address';
  email3 = 'Email Address';
  email4 = 'Email Address';
  email5 = 'Email Address';
  email6 = 'Email Address';

  emailAddress1 = 'herotogether@duke.com';
  emailAddress2 = 'email1@inbox.com';
  emailAddress3 = 'email1@inbox.com';
  emailAddress4 = 'email1@inbox.com';
  emailAddress5 = 'email1@inbox.com';
  emailAddress6 = 'email1@inbox.com';

   // voice mail
   voiceMailTollFree1 = 'Voicemail Phone';
   voiceMailPin1 = 'Voicemail PIN';
 
   voiceMailtollFreeNum1 = '111-555-1234';
   voiceMailtollFreeNum2 = '222-444-5432';
   voiceMailtollFreeNum3 = '919-785-8888';
   voiceMailtollFreeNum4 = '555-123-9874';
   voiceMailtollFreeNum5 = '777-111-5252';
   voiceMailtollFreeNum6 = '122-778-3355';
 
   voiceMailtollPIN1 = '7859';
   voiceMailtollPIN2 = '1111';
   voiceMailtollPIN3 = '7899';
   voiceMailtollPIN4 = '3336';
   voiceMailtollPIN5 = '3939';
   voiceMailtollPIN6 = '5362';
 
   firstHeadingVoiceMail = 'Voicemail access instructions';
   voiceMailInstruction1 = '1. Access the voicemail system at 123-555-1919';
   voiceMailInstruction2 = '2. Enter the project voicemail phone number';
   voiceMailInstruction3 = '3. Enter the project voicemail PIN';

  searchTerms: string = '';

  contacts = [
    {
      name: 'Ashley Blake',
      role: 'Interviewer',
      email: 'ashley.blake@duke.edu',
      escalation: null,
    },
    {
      name: 'Gina Hernandez',
      role: 'Admin',
      email: 'gina.hernandez@duke.edu',
      escalation: '1-919-123-9876',
    },
    {
      name: 'Heather Campbell',
      role: 'Project Team',
      email: 'heather.s.campbell@duke.edu',
      escalation: '1-919-555-7722',
    },
    {
      name: 'Labriah Wilson',
      role: 'Interviewer',
      email: 'lamwilson@duke.edu',
      escalation: null,
    },
    {
      name: 'Lauren Watkins',
      role: 'Interviewer',
      email: 'lauren.watkins@duke.edu',
      escalation: null,
    },
    {
      name: 'Lauren Conroy',
      role: 'Interviewer',
      email: 'lauren.conroy@duke.edu',
      escalation: null,
    },
    {
      name: 'Miroslava Martinez',
      role: 'Interviewer',
      email: 'mim17@duke.edu',
      escalation: null,
    },
    {
      name: 'Nicole Boone',
      role: 'Interviewer',
      email: 'nicole.boone17@duke.edu',
      escalation: null,
    },
    {
      name: 'Quanita Byers',
      role: 'Interviewer',
      email: 'quanita.byers17@duke.edu',
      escalation: null,
    },
    {
      name: 'Shayla Mitchell',
      role: 'Interviewer',
      email: 'shayla.mitchell17@duke.edu',
      escalation: null,
    },
  ];
  filteredContacts = [...this.contacts];
  selectAll = false;
  filterAdmin = false;
  filterProjectTeam = false;
  filterInterviewer = false;
  filterEscalationContact = false;



  constructor(
    private usersService: UsersService,
    private authenticationService: AuthenticationService,
    private configurationService: ConfigurationService,
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer
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

  }

  toggleSidebarAndHeader(): void {
    this.hideSidebarAndHeader = true;
    console.log("hideSidebarAndHeader----------- ", this.hideSidebarAndHeader);
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

  copyToClipboard(text: string): void {
    if (navigator.clipboard) {
      navigator.clipboard
        .writeText(text)
        .then(() => {})
        .catch((err) => {
          console.error('Error copying text: ', err);
        });
    } else {
      console.error('Clipboard API not supported');
    }
  }
  moveToPage(event: MouseEvent): void {
    event.stopPropagation();
    const url = "/book-reference"; 
    const width = 500;
    const height = 660; 
    const left = window.screen.width / 2 - width / 2; 
    const top = window.screen.height / 2 - height / 2; 
    
    window.open(
      url,
      "_blank",
      `width=${width},height=${height},top=${top},left=${left},resizable=no,scrollbars=no,toolbar=no,menubar=no,location=no,status=no`
    );
  }

  toggleSelectAll() {
    if (this.selectAll) {
      this.filterAdmin =
        this.filterProjectTeam =
        this.filterInterviewer =
        this.filterEscalationContact =
          true;
    } else {
      this.filterAdmin =
        this.filterProjectTeam =
        this.filterInterviewer =
        this.filterEscalationContact =
          false;
    }
    this.filterContacts();
  }

  filterContacts() {
    if (
      !this.filterAdmin &&
      !this.filterProjectTeam &&
      !this.filterInterviewer &&
      !this.filterEscalationContact
    ) {
      this.filteredContacts = [...this.contacts];
      return;
    }
    this.filteredContacts = this.contacts.filter((contact) => {
      return (
        (this.filterAdmin && contact.role === 'Admin') ||
        (this.filterProjectTeam && contact.role === 'Project Team') ||
        (this.filterInterviewer && contact.role === 'Interviewer') ||
        (this.filterEscalationContact && contact.escalation !== null)
      );
    });
  }
}
