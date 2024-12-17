import { Component, HostListener, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AddAnnouncementDialogComponent } from './add-announcement-dialog.component';
import { PageEvent } from '@angular/material/paginator';
import { PreviewComponent } from './preview.component';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { MatSort } from '@angular/material/sort';
import {
  MatSnackBar,
  MatSnackBarHorizontalPosition,
  MatSnackBarVerticalPosition,
} from '@angular/material/snack-bar';

@Component({
  selector: 'app-manage-announcements',
  templateUrl: './announcement.management.component.html',
  styleUrls: ['./announcement.management.component.css'],
})
export class ManageAnnouncementsComponent implements OnInit {
  @ViewChild(MatSort)
  sort!: MatSort;

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
  sortBy: string = '';
  orderBy: string = 'asc';
  selectedProjectList: any[] = [];
  allProjectList: any[] = [];
  isLoading = false;
  searchTerm: string = '';
  isSearchActive: boolean = false;

  constructor(
    private dialog: MatDialog,
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.getProjectInfo();
  }
  @HostListener('document:click', ['$event'])
  onClickOutside(event: MouseEvent): void {
    const inputElement = document.querySelector('input[type="text"]');
    if (inputElement && !inputElement.contains(event.target as Node)) {
      this.isSearchActive = false;
      this.searchTerm = '';
    }
  }

  onInputClick(event: MouseEvent) {
    event.stopPropagation();
  }
  applySearch() {
    this.searchTerm = this.searchTerm;
  }
  onEnterPress() {
    this.getList(1);
  }
  searchIconClicked(event: MouseEvent) {
    event.stopPropagation();
    this.isSearchActive = !this.isSearchActive;
    if (!this.isSearchActive) {
      this.getList(1);
      this.searchTerm = '';
    }
  }

  openAddAnnouncementDialog(): void {
    const dialogRef = this.dialog.open(AddAnnouncementDialogComponent);
    dialogRef.afterClosed().subscribe((result) => {
      this.getList(this.pageIndex + 1);
    });
  }

  editAnnouncement(announcement: any): void {
    const dialogRef = this.dialog.open(AddAnnouncementDialogComponent, {
      data: announcement,
    });
    dialogRef.afterClosed().subscribe((result) => {
      this.getList(this.pageIndex + 1);
    });
  }

  deleteAnnouncement(announcement: any): void {
    console.log('Deleting announcement:', announcement);
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/${announcement.id}`;
    this.http.delete(apiUrl).subscribe({
      next: (data: any) => {
        this.getList(1);
        this.showToastMessage('Delete successfully!', 'success');
      },
      error: (error: any) => {
        console.error('Error detete project info:', error);
      },
    });
  }
  viewAnnouncement(announcement: any): void {
    console.log('View announcement:', announcement);
    this.dialog.open(PreviewComponent, {
      width: '600px',
      data: announcement,
    });
  }

  handlePageEvent(e: PageEvent) {
    this.pageEvent = e;
    this.length = e.length;
    this.pageSize = e.pageSize;
    this.pageIndex = e.pageIndex;
    this.getList(this.pageIndex + 1);
  }
  onSort(column: string): void {
    if (this.sortBy === column) {
      this.orderBy = this.orderBy === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.orderBy = 'asc';
    }
    this.getList(this.pageIndex + 1);
  }
  getList(page: number): void {
    this.isLoading = true;
    let params = new HttpParams().set('page', page.toString());
    if (this.pageSize) {
      params = params.set('limit', this.pageSize.toString());
    }

    if (this.sortBy) {
      params = params.set('sortBy', this.sortBy).set('orderBy', this.orderBy);
    }
    if (this.searchTerm) {
      params = params.set('keyword', this.searchTerm);
    }
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/list/${page}`;
    this.http.get(apiUrl, { params }).subscribe({
      next: (data: any) => {
        this.announcements = data?.data?.map((item: any) => {
          const formatDate = (date: any): string => {
            const d = new Date(date);
            return !isNaN(d.getTime())
              ? d.toLocaleDateString('en-US', {
                  month: '2-digit',
                  day: '2-digit',
                  year: 'numeric',
                })
              : '';
          };
          return {
            start: formatDate(item?.startDate),
            expiration: formatDate(item?.expireDate),
            title: item?.title || '',
            authorName: item?.authorName || '',
            displayTo: item?.projectIds?.length
              ? item?.projectIds?.length !== this.allProjectList.length &&
                this.allProjectList.length > 0
                ? item.projectObject
                : 'Any Projects'
              : null,
            id: item?.announcementId,
            bodyText: item?.bodyText,
          };
        });
        this.length = data?.count || data?.data?.length;
        this.isLoading = false;
      },
      error: (error: any) => {
        this.isLoading = false;
        console.error('Error fetching announcements:', error);
      },
    });
  }
  getProjectInfo(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/projects`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.allProjectList = data ? data : [];
        this.getList(1);
      },
      error: (error: any) => {
        console.error('Error fetching project info:', error);
      },
    });
  }
  showToastMessage(message: string, type: string): void {
    let snackBarClass = 'success-snackbar';
    if (type === 'error') {
      snackBarClass = 'error-snackbar';
    }

    const horizontalPosition: MatSnackBarHorizontalPosition = 'end';
    const verticalPosition: MatSnackBarVerticalPosition = 'top';

    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: [snackBarClass],
      horizontalPosition: horizontalPosition,
      verticalPosition: verticalPosition,
    });
  }
}
