import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';


@Component({
  selector: 'app-custom',
  templateUrl: './custom.page.component.html',
})
export class CustomPageComponent {

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



  constructor(private http: HttpClient) { }

  ngOnInit(): void {
   this.getVoicMailData();
   this.getProjectInfo();
   this.getContactInfo();
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
        (this.filterEscalationContact && contact.escalation !== null)
      );
    });
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
