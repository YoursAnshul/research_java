import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AddAnnouncementDialogComponent } from './add-announcement-dialog.component';
import { PageEvent } from '@angular/material/paginator';
import { PreviewComponent } from './preview.component';
import { EditAnnouncementDialogComponent } from './edit-announcement-dialog.component';

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
  length = 4;
  pageSize = 10;
  pageIndex = 0;
  pageSizeOptions = [5, 10, 15];

  hidePageSize = false;
  showPageSizeOptions = true;
  showFirstLastButtons = true;
  disabled = false;

  pageEvent!: PageEvent;

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
    const dialogRef = this.dialog.open(AddAnnouncementDialogComponent, {
      width: '600px',
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.announcements.push(result);
      }
    });
  }

  editAnnouncement(announcement: any): void {
    console.log('Editing announcement:', announcement);
    this.dialog.open(EditAnnouncementDialogComponent, {
      width: '600px',
    });
  }

  deleteAnnouncement(announcement: any): void {
    console.log('Deleting announcement:', announcement);
  }
  viewAnnouncement(announcement: any): void {
    console.log('View announcement:', announcement);
    this.dialog.open(PreviewComponent, {
      width: '600px',
    });
  }
  handlePageEvent(e: PageEvent) {
    this.pageEvent = e;
    this.length = e.length;
    this.pageSize = e.pageSize;
    this.pageIndex = e.pageIndex;
  }

  setPageSizeOptions(setPageSizeOptionsInput: string) {
    if (setPageSizeOptionsInput) {
      this.pageSizeOptions = setPageSizeOptionsInput
        .split(',')
        .map((str) => +str);
    }
  }
}
