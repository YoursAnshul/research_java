import { Component, OnInit } from '@angular/core';
import { IAuthenticatedUser } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
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

  firstHeading = 'HERO Together';
  secondHeading = 'Project 42';
  thirdHeading = 'Project Eleven';
  fourtHeading = 'Project Q';
  fifthHeading = 'Purple Project';

  tollFree1 = 'Toll-Free Phone #';
  tollFree2 = 'Toll-Free Phone #';
  tollFree3 = 'Toll-Free Phone #';
  tollFree4 = 'Toll-Free Phone #';
  tollFree5 = 'Toll-Free Phone #';

  tollFreeNum1 = '1-800-999-8888';
  tollFreeNum2 = '1-800-999-8888';
  tollFreeNum3 = '1-800-999-8888';
  tollFreeNum4 = '1-800-999-8888';
  tollFreeNum5 = '1-800-999-8888';

  email1 = 'Email Address';
  email2 = 'Email Address';
  email3 = 'Email Address';
  email4 = 'Email Address';
  email5 = 'Email Address';

  emailAddress1 = 'herotogether@duke.com';
  emailAddress2 = 'email1@inbox.com';
  emailAddress3 = 'email1@inbox.com';
  emailAddress4 = 'email1@inbox.com';
  emailAddress5 = 'email1@inbox.com';

  searchTerms: string = '';
  authenticatedUser!: IAuthenticatedUser;

  constructor(private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;
      }
    );

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

}
