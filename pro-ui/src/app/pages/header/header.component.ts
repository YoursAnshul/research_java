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
  voiceMailInstruction1 = '1. Call';
  voiceMailInstructionCopy = '123-555-1919';
  voiceMailInstruction2 = '2. Press the  "*"  star button';
  voiceMailInstruction3 = '3. Enter "ID" [Voicemail Phone]';
  voiceMailInstruction4 = '3. Enter "PIN" [Voicemail PIN]';


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
  loading = false;
  activeTabIndex: number = 0;



  @ViewChild('participantTemplate') participantTemplate!: TemplateRef<any>;

  constructor(
    private authenticationService: AuthenticationService,
    private http: HttpClient,
    private dialog: MatDialog,
    private globalsService: GlobalsService
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
      this.activeTabIndex = 0;
      this.handleTabLogic(0);
    });

    this.menuTrigger.menuClosed.subscribe(() => {
      this.searchTerms = '';
    });
  }
  onTabChange(event: any): void {
    this.activeTabIndex = event.index;
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


  addASchedulePopup(): void {
    this.globalsService.openSchedulePopup();
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
    this.loading = true;
    const apiUrl = `${environment.DataAPIUrl}/quick-reference/list`;
    const mainVmPhoneApiUrl = `${environment.DataAPIUrl}/quick-reference/main-vm-phone`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.voicemaillist = data?.Subject;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error fetching voicemails:', error);
        this.loading = false;
      },
    });
    this.http.get(mainVmPhoneApiUrl).subscribe({
      next: (data: any) => {
        const mainPhoneNumber = data?.Subject;
        if (mainPhoneNumber) {
          this.voiceMailInstruction1 = `1. Access the voicemail system at ${mainPhoneNumber}`;
          this.voiceMailInstructionCopy = mainPhoneNumber;
        }
      },
      error: (error: any) => {
        console.error('Error fetching voicemail phone number:', error);
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
    this.loading = true;
    const apiUrl = `${environment.DataAPIUrl}/quick-reference/project-info`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.projectinfolist = data?.Subject.map((project: any) => ({
          ...project,
          showMore: false,
        }));
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error fetching project info:', error);
        this.loading = false;
      },
    });
  }

  toggleShowMore(data: any): void {
    data.showMore = !data.showMore;
  }

  getContactInfo(): void {
    this.loading = true;
    const apiUrl = `${environment.DataAPIUrl}/quick-reference/team-contact`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.teamContactList = data?.Subject || [];
        this.filteredContacts = [...this.teamContactList];
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error fetching project info:', error);
        this.loading = false;
      },
    });
  }
}
