import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-announcements',
  templateUrl: './announcements.component.html',
  styleUrls: ['./announcements.component.css'],
})
export class AnnouncementsComponent implements OnInit {
  announcementsList: any = [];
  constructor(private http: HttpClient) {}
  ngOnInit(): void {
    this.getAnnouncementList();
  }
  getAnnouncementList(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/list`;
    this.http.get<string[]>(apiUrl).subscribe({
      next: (data: any) => {
        this.announcementsList = data?.Subject || [];
        console.log(' this.announcementsList--', this.announcementsList);
      },
      error: (error: any) => {
        console.error('Error fetching authors:', error);
      },
    });
  }
}
