import {
  Component,
  HostListener,
  Input,
  OnInit,
  ViewChild,
} from '@angular/core';
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
import { ConfirmationDialogComponent } from '../../components/delete-dialog/confirmation-dialog.component';
import { IAuthenticatedUser } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';

@Component({
  selector: 'app-manage-announcements',
  templateUrl: './announcement.management.component.html',
  styleUrls: ['./announcement.management.component.css'],
})
export class ManageAnnouncementsComponent implements OnInit {
  @Input() authenticatedUser!: IAuthenticatedUser;

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
  allAuthors: string[] = [];
  filteredAuthors: string[] = [];
  selectedAuthors: string[] = [];
  isFilterActive: boolean = false;
  path: string | null = null;
  userObj: any;

  constructor(
    private dialog: MatDialog,
    private http: HttpClient,
    private snackBar: MatSnackBar,
    private authenticationService: AuthenticationService
  ) {}

  ngOnInit(): void {
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
        this.userObj = this.authenticatedUser;
        console.log(this.userObj);
      }
    );
    this.getProjectInfo();
  }
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const inputElement = document.querySelector('input[type="text"]');
    if (inputElement && !inputElement.contains(event.target as Node)) {
      this.isSearchActive = false;
      if (this.searchTerm) {
        this.searchTerm = '';
        this.getList(this.pageIndex + 1);
      }
      this.pageIndex = 0;
    }
    if (!this.announcements || this.announcements.length === 0) {
      this.searchTerm = '';
      this.pageIndex = 0;
      this.getList(this.pageIndex + 1);
    }
  }

  onKeyDown(event: Event): void {
    const keyboardEvent = event as KeyboardEvent;
    if (keyboardEvent.key === ' ') {
      event.stopPropagation();
    }
  }
  isAllSelected(): boolean {
    return this.selectedAuthors.length === this.allAuthors.length;
  }
  isIndeterminate(): boolean {
    return (
      this.selectedAuthors.length > 0 &&
      this.selectedAuthors.length < this.allAuthors.length
    );
  }
  selectAllAuthors(event: any): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      this.selectedAuthors = [...this.allAuthors];
    } else {
      this.selectedAuthors = [];
    }
  }
  applyFilter(): void {
    this.isFilterActive = false;
    this.pageIndex = 0;
    this.getList(this.pageIndex + 1);
  }

  resetFilter(): void {
    this.selectedAuthors = [];
    this.isFilterActive = false;
    this.pageIndex = 0;
    this.getList(this.pageIndex + 1);
  }
  onSelectionChange(event: any): void {
    this.selectedAuthors = this.selectedAuthors.filter(
      (author) => author !== undefined && author !== null
    );
  }

  filterIconClicked(event: MouseEvent): void {
    event.stopPropagation();
    this.isFilterActive = !this.isFilterActive;
    if (this.isFilterActive) {
      this.getAuthors();
    }
  }

  onInputClick(event: MouseEvent) {
    event.stopPropagation();
  }
  applySearch() {
    this.searchTerm = this.searchTerm;
  }
  onEnterPress() {
    this.pageIndex = 0;
    this.getList(this.pageIndex + 1);
  }
  searchIconClicked(event: MouseEvent) {
    event.stopPropagation();
    this.isSearchActive = !this.isSearchActive;
    if (!this.isSearchActive) {
      this.pageIndex = 0;
      this.getList(this.pageIndex + 1);
      this.searchTerm = '';
    }
  }

  openAddAnnouncementDialog(): void {
    const dialogRef = this.dialog.open(AddAnnouncementDialogComponent);
    dialogRef.afterClosed().subscribe((result) => {
      this.pageIndex = 0;
      this.getList(this.pageIndex + 1);
    });
  }

  editAnnouncement(announcement: any): void {
    const dialogRef = this.dialog.open(AddAnnouncementDialogComponent, {
      data: announcement,
    });
    dialogRef.afterClosed().subscribe((result) => {
      this.pageIndex = 0;
      this.getList(this.pageIndex + 1);
    });
  }

  deleteAnnouncement(announcement: any): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent);

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        const apiUrl = `${environment.DataAPIUrl}/manage-announement/${announcement.id}`;
        this.http.delete(apiUrl).subscribe({
          next: (data: any) => {
            this.pageIndex = 0;
            this.getList(this.pageIndex + 1);
            this.showToastMessage('Delete successfully!', 'success');
          },
          error: (error: any) => {
            console.error('Error deleting announcement:', error);
          },
        });
      } else {
        console.log('Deletion cancelled');
      }
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
    this.pageIndex = 0;
    this.getList(this.pageIndex + 1);
  }
  getList(page: number): void {
    this.isLoading = true;
    let params = new HttpParams();
    if (this.pageSize) {
      params = params.set('limit', this.pageSize.toString());
    }

    if (this.sortBy) {
      params = params.set('sortBy', this.sortBy).set('orderBy', this.orderBy);
    }
    if (this.searchTerm) {
      params = params.set('keyword', this.searchTerm);
    }
    if (this.selectedAuthors && this.selectedAuthors.length > 0) {
      params = params.set(
        'authorName',
        this.selectedAuthors.map((author) => `'${author}'`).join(',')
      );
    }
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/list/${page}`;
    this.http.get(apiUrl, { params }).subscribe({
      next: (data: any) => {
        this.announcements = data?.data?.map((item: any) => {
          const formatDate = (date: any): string => {
            if (!date) return '';
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
            isAuthor: item?.isAuthor,
            icon: item?.icon,
          };
        });
        this.length = 0;
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
        this.pageIndex = 0;
        this.getList(this.pageIndex + 1);
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

  getAuthors(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/all-authors`;
    this.http.get<string[]>(apiUrl).subscribe({
      next: (data: any) => {
        this.allAuthors = data ? data : [];
        this.filteredAuthors = this.allAuthors;
      },
      error: (error: any) => {
        console.error('Error fetching authors:', error);
      },
    });
  }
}
