import { Component, Input, OnInit } from '@angular/core';
import { TableHeaderItem } from '../../models/presentation/table-header-item';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { AddAnnouncementDialogComponent } from './add-announcement-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { ConfirmationDialogComponent } from '../../components/delete-dialog/confirmation-dialog.component';
import {
  MatSnackBar,
  MatSnackBarHorizontalPosition,
  MatSnackBarVerticalPosition,
} from '@angular/material/snack-bar';
import { PreviewComponent } from './preview.component';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import {
  IAuthenticatedUser,
  IDropDownValue,
  IFormFieldVariable,
} from '../../interfaces/interfaces';
import { SelectedValue } from '../../models/presentation/selected-value';
import { Utils } from '../../classes/utils';

@Component({
  selector: 'app-manage-announcements',
  templateUrl: './announcement.management.component.html',
  styleUrls: ['./announcement.management.component.css'],
})
export class ManageAnnouncementsComponent implements OnInit {
  @Input() authenticatedUser!: IAuthenticatedUser;
  public roleHeaderItem: TableHeaderItem = new TableHeaderItem(
    'Author',
    'author',
    true,
    false,
    true,
    false,
    undefined,
    true
  );
  public headerItems: TableHeaderItem[] = [
    new TableHeaderItem('Start', 'startdate', false, false, true, false),
    new TableHeaderItem('Expiration', 'expiration', false, false, true, false),
    new TableHeaderItem('Title', 'title', false, true, true, false),
    this.roleHeaderItem,
    new TableHeaderItem(
      'Display Announcements To',
      'display_announcement_to',
      false,
      false,
      false,
      false
    ),
    new TableHeaderItem('Action ', 'action', false, false, false, false),
  ];

  allProjectList: any[] = [];
  announcements: any[] = [];

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
  orderBy: string = '';
  searchTerm: string = '';

  isLoading = false;
  userObj: any;
  public authores: IFormFieldVariable | undefined = undefined;
  public selectedAuthores: SelectedValue[] = [];
  public selectCommaSeparatedAuthores: string = '';
  constructor(
    private http: HttpClient,
    private dialog: MatDialog,
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
    this.getAuthors();
    this.getProjectInfo();
    this.getList(this.pageIndex + 1);
  }

  public headerItemsChange(headerItems: TableHeaderItem[]): void {
    this.headerItems = headerItems;
    this.searchTerm = '';
    this.selectedAuthores = this.roleHeaderItem.filterValue;
    this.applyFilter();
  }
  applyFilter(): void {
    for (var i = 0; i < this.headerItems.length; i++) {
      if (this.headerItems[i].searchValue) {
        this.searchTerm = this.headerItems[i].searchValue;
        this.pageIndex =0;
        this.getList(this.pageIndex + 1);
      }

      if (this.headerItems[i].sortDirection && this.headerItems[i].name) {
        this.sortBy = this.headerItems[i].name || '';
        this.orderBy = this.headerItems[i].sortDirection || '';
        this.pageIndex =0;
        this.getList(this.pageIndex + 1);
      }
    }
    this.selectCommaSeparatedAuthores =
      this.selectedAuthores && this.selectedAuthores.length > 0
        ? this.selectedAuthores
            .map((author) => `'${author.item?.dropDownItem || ''}'`) 
            .join(', ')
        : ''; 

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
    if (this.selectCommaSeparatedAuthores) {
      params = params.set('authorName', this.selectCommaSeparatedAuthores);
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

  openAddAnnouncementDialog(): void {
    const dialogRef = this.dialog.open(AddAnnouncementDialogComponent);
    dialogRef.afterClosed().subscribe((result) => {});
  }
  handlePageEvent(e: PageEvent) {
    this.pageEvent = e;
    this.length = e.length;
    this.pageSize = e.pageSize;
    this.pageIndex = e.pageIndex;
    this.getList(this.pageIndex + 1);
  }
  viewAnnouncement(announcement: any): void {
    this.dialog.open(PreviewComponent, {
      width: '600px',
      data: announcement,
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
    this.http.get<any>(apiUrl).subscribe({
      next: (data: any) => {
        if (data?.Subject) {
          const authorDropDownValues = Utils.convertObjectArrayToDropDownValues(
            data.Subject,
            'userId',
            'userName'
          );
          const roleHeaderItem: TableHeaderItem | undefined =
            this.headerItems.find((x) => x.name === 'author');
          if (roleHeaderItem) {
            roleHeaderItem.filterOptions = authorDropDownValues;
          }
        }
      },
      error: (error: any) => {
        console.error('Error fetching authors:', error);
      },
    });
  }
}
