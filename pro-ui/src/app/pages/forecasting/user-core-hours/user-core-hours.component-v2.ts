import { Component, OnInit } from '@angular/core';
import { SelectedValue } from '../../../models/presentation/selected-value';
import { IDropDownValue } from '../../../interfaces/interfaces';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import {
  MatSnackBar,
  MatSnackBarHorizontalPosition,
  MatSnackBarVerticalPosition,
} from '@angular/material/snack-bar';

@Component({
  selector: 'app-user-core-hours-v2',
  templateUrl: './user-core-hours.component-v2.html',
  styleUrls: ['./user-core-hours.component.css'],
})
export class UserCoreHoursComponentV2 implements OnInit {
  constructor(
    private readonly http: HttpClient,
    private readonly snackBar: MatSnackBar
  ) {}

  monthsHeader: string[] = [];
  monthKeys: string[] = [];
  list: any[] = [];
  paginatedList: any[] = [];
  currentPage = 1;
  pageSize = 10;
  totalCoreHours: number[] = [];
  projectTotalCorehours: number[] = [];

  dropDownValues: IDropDownValue[] = [
    { codeValues: 1, dropDownItem: 'Interviewer' },
    { codeValues: 2, dropDownItem: 'Resource Group' },
  ];
  selectedValues: SelectedValue[] = [
    new SelectedValue(1, { codeValues: 1, dropDownItem: 'Interviewer' }),
  ];
  editedCoreHours: {
    date: string;
    coreHours: number;
    coreHoursId: number;
  }[] = [];
  ngOnInit(): void {
    this.getMonths();
    this.getList();
    this.calculateProjectTotals();
    this.calculateTotals();
  }

  getMonths() {
    const now = new Date();
    for (let i = 0; i < 14; i++) {
      const date = new Date(now.getFullYear(), now.getMonth() + i, 1);
      const label =
        date.toLocaleString('default', { month: 'short' }) +
        '-' +
        date.getFullYear().toString().slice(-2);
      this.monthsHeader.push(label);

      const key = `${date.getFullYear()}-${(date.getMonth() + 1)
        .toString()
        .padStart(2, '0')}-01`;
      this.monthKeys.push(key);
    }
  }

  userRoleChange(event: any) {
    console.log('Role changed:', event);
    // Optionally re-fetch list based on role
  }

  getList(): void {
    const apiUrl = `${environment.DataAPIUrl}/forecasting/list`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.list = data?.data || [];
        this.paginate();
      },
      error: (error: any) => {
        console.error('Error fetching user forecasting:', error);
      },
    });
  }

  paginate(): void {
    if (this.list.length <= this.pageSize) {
      this.currentPage = 1;
    }

    const maxPage = Math.max(1, Math.ceil(this.list.length / this.pageSize));

    if (this.currentPage < 1) this.currentPage = 1;
    if (this.currentPage > maxPage) this.currentPage = maxPage;

    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;

    this.paginatedList = this.list.slice(start, end);
    this.calculateTotals();
    this.calculateProjectTotals();
  }

  getCoreHour(res: any, monthIndex: number): number {
    const key = this.monthKeys[monthIndex];
    const match = res.coreHoursByMonth?.find((m: any) => m.first === key);
    return match?.second ?? 0;
  }

  calculateTotals(): void {
    const apiUrl = `${environment.DataAPIUrl}/forecasting/user-total-hours`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.totalCoreHours = data ?? [];
      },
      error: (error: any) => {
        console.error('Error fetching user forecasting:', error);
      },
    });
  }
  calculateProjectTotals(): void {
    const apiUrl = `${environment.DataAPIUrl}/forecasting/project-total-hours`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.projectTotalCorehours = data ?? [];
      },
      error: (error: any) => {
        console.error('Error fetching user forecasting:', error);
      },
    });
  }

  onPageChanged(): void {
    this.calculateProjectTotals();
    this.calculateTotals();
  }
  onCoreHourChange(event: Event, monthKey: string, res: any): void {
    const input = event.target as HTMLInputElement;
    const value = parseInt(input.value, 0);
    if (isNaN(value)) return;
    res.coreHoursByMonth ??= {};
    res.coreHoursByMonth[monthKey] = value;
    const existing = this.editedCoreHours.find(
      (e) => e.coreHoursId === res.coreHoursId && e.date === monthKey
    );
    if (existing) {
      existing.coreHours = value;
    } else {
      this.editedCoreHours.push({
        coreHoursId: res.coreHoursId ?? 0,
        date: monthKey,
        coreHours: value,
      });
    }
  }

  saveCoreHours(): void {
    if (this.editedCoreHours.length === 0) {
      this.showToastMessage('No changes to save.', 'error');
      return;
    }
    const apiUrl = `${environment.DataAPIUrl}/forecasting/user-core-update`;
    this.http.put(apiUrl, this.editedCoreHours).subscribe({
      next: (response: any) => {
        this.showToastMessage('Core hours saved successfully!', 'success');
        this.editedCoreHours = [];
        this.getList();
      },
      error: (error: any) => {
        console.error('Error saving core hours:', error);
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
