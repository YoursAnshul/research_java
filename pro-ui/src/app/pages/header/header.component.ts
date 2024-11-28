import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { IAuthenticatedUser } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { MatMenuTrigger } from '@angular/material/menu';
import { HttpClient } from '@angular/common/http';
import { GlobalsService } from '../../services/globals/globals.service';
import { DialogComponent } from '../../components/dialog/dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent implements OnInit {
  @ViewChild(MatMenuTrigger) menuTrigger!: MatMenuTrigger;

  tollFree1 = 'Toll-Free Phone #';
  email1 = 'Email Address';
  voiceMailTollFree1 = 'Voicemail Phone';
  voiceMailPin1 = 'Voicemail PIN';

  firstHeadingVoiceMail = 'Voicemail access instructions';
  voiceMailInstruction1 = '1. Access the voicemail system at 123-555-1919';
  voiceMailInstructionCopy = '123-555-1919';
  voiceMailInstruction2 = '2. Enter the project voicemail phone number';
  voiceMailInstruction3 = '3. Enter the project voicemail PIN';

  searchTerms: string = '';
  authenticatedUser!: IAuthenticatedUser;
  selectAll = false;
  filterAdmin = false;
  filterProjectTeam = false;
  filterInterviewer = false;
  filterEscalationContact = false;

  selectAllValue = 'Select All';
  filterContactsValue = 'Filter Contacts';
  admin = 'Admin';
  projectTeamValue = 'Project Team';
  interviewerValue = 'Interviewer';
  escalationContactValue = 'Escalation Contact';
  roleValue = 'Role';
  escalationValue = 'Escalation #';
  voicemaillist: any[] = [];
  projectinfolist: any[] = [];
  teamContactList: any[] = [];
  filteredContacts: any[] = [];

  @ViewChild('participantTemplate') participantTemplate!: TemplateRef<any>;

  constructor(
    private authenticationService: AuthenticationService,
    private http: HttpClient,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
      }
    );
  }
  ngAfterViewInit(): void {
    this.menuTrigger.menuOpened.subscribe(() => {
      this.handleTabLogic(0);
    });

    this.menuTrigger.menuClosed.subscribe(() => {
      this.searchTerms = '';
    });
  }
  onTabChange(event: any): void {
    this.handleTabLogic(event.index);
  }
  handleTabLogic(tabIndex: number): void {
    switch (tabIndex) {
      case 0:
        this.getVoicMailData();
        break;
      case 1:
        this.getProjectInfo();
        break;
      case 2:
        this.getContactInfo();
        break;
      default:
        break;
    }
  }

  public logout(): void {
    this.authenticationService.logout();
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
    const url = '/book-reference';
    const width = 500;
    const height = 1000;
    const left = window.screen.width / 2 - width / 2;
    const top = window.screen.height / 2 - height / 2;

    window.open(
      url,
      '_blank',
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
      this.filteredContacts = [...this.teamContactList];
      return;
    }
    this.filteredContacts = this.teamContactList.filter((contact) => {
      return (
        (this.filterAdmin && contact.role === 'Admin') ||
        (this.filterProjectTeam && contact.role === 'Project Team') ||
        (this.filterInterviewer && contact.role === 'Interviewer') ||
        (this.filterEscalationContact && contact.escalationphone !== null)
      );
    });
  }
  closeMenu() {
    this.searchTerms = '';
    this.menuTrigger.closeMenu();
  }
  clearSearch() {
    this.searchTerms = '';
  }

  getVoicMailData(): void {
    const apiUrl = `${environment.DataAPIUrl}/quick-reference/list`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.voicemaillist = data?.Subject;
      },
      error: (error: any) => {
        console.error('Error fetching voicemails:', error);
      },
    });
  }

  public openParticipantModal(): void {
    //this.globalsService.openSchedulePopup();
    this.dialog.open(DialogComponent, {
      width: '75.5%',
      data: {
        template: this.participantTemplate,
      },
    });
  }

  getProjectInfo(): void {
    const apiUrl = `${environment.DataAPIUrl}/quick-reference/project-info`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.projectinfolist = data?.Subject;
      },
      error: (error: any) => {
        console.error('Error fetching project info:', error);
      },
    });
  }

  getContactInfo(): void {
    const apiUrl = `${environment.DataAPIUrl}/quick-reference/team-contact`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.teamContactList = data?.Subject || [];
        this.filteredContacts = [...this.teamContactList];
      },
      error: (error: any) => {
        console.error('Error fetching project info:', error);
      },
    });
  }
}
