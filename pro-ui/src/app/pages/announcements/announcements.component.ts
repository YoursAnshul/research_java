import { HttpClient } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { IAuthenticatedUser } from '../../interfaces/interfaces';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { User } from '../../models/data/user';
import { UsersService } from '../../services/users/users.service';
import { Utils } from '../../classes/utils';

interface Announcement {
  title: string;
  bodyText: string;
  startDate: string;
  authorName: string;
  icon: string;
  isFullText?: boolean;
  projectObject?: any[];
  projectIds?: number[];
  isAuthor: boolean;
}
@Component({
  selector: 'app-announcements',
  templateUrl: './announcements.component.html',
  styleUrls: ['./announcements.component.css'],
})
export class AnnouncementsComponent implements OnInit {
  @Input() authenticatedUser!: IAuthenticatedUser;
  announcementsList: Announcement[] = [];
  maxLength = 100;
  userObj: any;
  allProjectList: any[] = [];
  currentUser: User = new User();

  constructor(
    private http: HttpClient,
    private authenticationService: AuthenticationService,
    private sanitizer: DomSanitizer,
    private usersService: UsersService,
  ) {}
  ngOnInit(): void {
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
        this.userObj = this.authenticatedUser;
        
        this.usersService.getUserByNetId(this.authenticatedUser.netID).subscribe(
          (data: any) => {
            this.currentUser = data?.Subject ? data.Subject : new User();
            this.setTrainedOn();
            this.getAnnouncementList();
          },
          (error) => {
            console.error('Error fetching user:', error);
        });
      }
    );
    this.getProjectInfo();
    
  }

  getSafeHtml(htmlString: string): SafeHtml {  
    if (!htmlString) return '';
      const updatedHtmlString = htmlString.replace(/<a\s+(.*?)href="(?!https?:\/\/)(.*?)"/g, (match, preAttributes, url) => {
      return `<a ${preAttributes}href="https://${url}"`;
    });
  
    return this.sanitizer.bypassSecurityTrustHtml(updatedHtmlString);
  }

  getAnnouncementList(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/list`;
    this.http.get<any[]>(apiUrl).subscribe({
      next: (data: any) => {

        this.announcementsList =
          data?.Subject?.map((item: any) => ({
            ...item,
            isFullText: false,
          })) || [];
          console.log(this.authenticatedUser);

          if (!(this.authenticatedUser.resourceGroup
            || this.authenticatedUser.admin)
          ) {
            this.announcementsList = this.announcementsList.filter((announcement) => Utils.arrayIncludesAny(this.currentUser.trainedOnArray, (announcement.projectIds || []).map(x => x.toString())));
          }

      },
      error: (error: any) => {
        console.error('Error fetching announcements:', error);
      },
    });
  }

  getTruncatedText(announcement: Announcement): string {
    if (announcement.isFullText) {
      return announcement.bodyText;
    }
    console.log("announcement.bodyText.length---------- ",announcement.bodyText.length);
    
    return announcement.bodyText.length > this.maxLength
      ? announcement.bodyText.substring(0, this.maxLength) + '...'
      : announcement.bodyText;
  }

  toggleText(announcement: Announcement): void {
    announcement.isFullText = !announcement.isFullText;
  }
  getHighlightedTextParts(text: string): { text: string; isLink: boolean }[] {
    const regex = /(\b[\w.-]+(?:\.[a-z]{2,})\b)/g;
    let match;
    let lastIndex = 0;
    const parts = [];

    while ((match = regex.exec(text)) !== null) {
      if (match.index > lastIndex) {
        parts.push({ text: text.slice(lastIndex, match.index), isLink: false });
      }
      parts.push({ text: match[0], isLink: true });
      lastIndex = regex.lastIndex;
    }

    if (lastIndex < text.length) {
      parts.push({ text: text.slice(lastIndex), isLink: false });
    }

    return parts;
  }
  getProjectInfo(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/projects`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.allProjectList = data ? data : [];
      },
      error: (error: any) => {
        console.error('Error fetching project info:', error);
      },
    });
  }

  setTrainedOn(): void {
    let trainedOnIds: string[] = [];
    
     if (this.currentUser?.trainedon) {
       trainedOnIds = this.currentUser.trainedon.split('|');
     }
     this.currentUser.trainedOnArray = trainedOnIds;
     
   }
 
}
