import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { environment } from '../../../environments/environment';

interface Announcement {
  title: string;
  bodyText: string;
  startDate: string;
  authorName: string;
  icon: string;
  isFullText?: boolean;
  projectObject?: any[];
}
@Component({
  selector: 'app-announcements',
  templateUrl: './announcements.component.html',
  styleUrls: ['./announcements.component.css'],
})
export class AnnouncementsComponent implements OnInit {
  announcementsList: Announcement[] = [];
  maxLength = 100;
  constructor(private http: HttpClient) {}
  ngOnInit(): void {
    this.getAnnouncementList();
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
}
