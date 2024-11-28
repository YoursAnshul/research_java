import { Component, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { MatMenuTrigger } from '@angular/material/menu';


@Component({
  selector: 'app-custom',
  templateUrl: './custom.page.component.html',
})
export class CustomPageComponent {
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

  filteredContacts: any[] = [];
  selectAll = false;
  filterAdmin = false;
  filterProjectTeam = false;
  filterInterviewer = false;
  filterEscalationContact = false;


  selectAllValue = 'Select All';
  filterContactsValue = 'Filter Contacts'
  admin = 'Admin';
  projectTeamValue = 'Project Team';
  interviewerValue = 'Interviewer';
  escalationContactValue = 'Escalation Contact';
  roleValue = 'Role';
  escalationValue = 'Escalation #'
  voicemaillist: any[] = [];
  projectinfolist: any[] = [];
  teamContactList: any[] = [];
  loading = false;



  constructor(private http: HttpClient) { }
  @ViewChild(MatMenuTrigger) menuTrigger!: MatMenuTrigger;

  ngOnInit(): void {
    this.handleTabLogic(0);
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
    const height = 1000; 
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

  getVoicMailData(): void {
    this.loading = true; 
    const apiUrl = `${environment.DataAPIUrl}/quick-reference/list`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.voicemaillist = data?.Subject;
        this.loading = false; 
      },
      error: (error: any) => {
        this.loading = false; 
        console.error('Error fetching voicemails:', error);
      },
    });
  }
  getProjectInfo(): void {
    this.loading = true; 
    const apiUrl = `${environment.DataAPIUrl}/quick-reference/project-info`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.projectinfolist = data?.Subject;
        this.loading = false; 
      },
      error: (error: any) => {
        console.error('Error fetching project info:', error);
        this.loading = false; 
      },
    });
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
        this.loading = false; 
        console.error('Error fetching project info:', error);
      },
    });
  }
}
