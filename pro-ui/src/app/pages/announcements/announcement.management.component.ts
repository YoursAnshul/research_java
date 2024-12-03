import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-manage-announcements',
  templateUrl: './announcement.management.component.html',
  styleUrls: ['./announcement.management.component.css'],
})
export class ManageAnnouncementsComponent implements OnInit {
  displayedColumns: string[] = [
    'start',
    'expiration',
    'title',
    'author',
    'displayTo',
    'actions',
  ];
  announcements: any = [];

  constructor(private dialog: MatDialog) {}

  ngOnInit(): void {
    this.loadAnnouncements();
  }

  loadAnnouncements(): void {
    const data = [
      {
        start: '11/16/2023',
        expiration: '11/18/2023',
        title: 'Happy Birthday, Michelle!',
        author: 'Susan Rogers',
        displayTo: 'All Users',
      },
      {
        start: '11/17/2023',
        expiration: '11/17/2023',
        title: 'Scheduled Maintenance: Friday, November 18',
        author: 'Rick Lane',
        displayTo: 'All Users',
      },
      {
        start: '11/03/2023',
        expiration: '',
        title: "Remember to 'Time In'!",
        author: 'Susan Rogers',
        displayTo: 'All Users',
      },
    ];
    this.announcements = data;
  }

  openAddAnnouncementDialog(): void {
    console.log('Opening add announcement dialog...');
  }

  editAnnouncement(announcement: any): void {
    console.log('Editing announcement:', announcement);
  }

  deleteAnnouncement(announcement: any): void {
    console.log('Deleting announcement:', announcement);
  }
  viewAnnouncement(announcement: any): void {
    console.log('View announcement:', announcement);

  }
}
