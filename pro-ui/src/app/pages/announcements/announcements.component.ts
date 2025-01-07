import { HttpClient } from '@angular/common/http';
import { Component, Input, OnInit } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { IAuthenticatedUser } from '../../interfaces/interfaces';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

interface Announcement {
  title: string;
  bodyText: string;
  startDate: string;
  authorName: string;
  icon: string;
  isFullText?: boolean;
  projectObject?: any[];
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
  constructor(
    private http: HttpClient,
    private authenticationService: AuthenticationService,private sanitizer: DomSanitizer
  ) {}
  ngOnInit(): void {
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
        this.userObj = this.authenticatedUser;
      }
    );
    this.getAnnouncementList();
    this.getProjectInfo();
  }
  getSafeHtml(htmlString: string): SafeHtml {
    console.log("htmlString---------- ", htmlString);
  
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
}
