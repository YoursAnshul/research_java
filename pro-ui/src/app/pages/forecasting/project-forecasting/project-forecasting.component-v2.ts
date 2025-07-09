import { Component, OnInit } from '@angular/core';
import { SelectedValue } from '../../../models/presentation/selected-value';
import {
  IDropDownValue,
  IFormFieldVariable,
} from '../../../interfaces/interfaces';
import { environment } from '../../../../environments/environment';
import {
  MatSnackBar,
  MatSnackBarHorizontalPosition,
  MatSnackBarVerticalPosition,
} from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { ConfigurationService } from '../../../services/configuration/configuration.service';

@Component({
  selector: 'app-project-forecasting-v2',
  templateUrl: './project-forecasting.component-v2.html',
  styleUrl: './project-forecasting.component.css',
})
export class ProjectForecastingComponentV2 implements OnInit {
  constructor(
    private readonly http: HttpClient,
    private readonly snackBar: MatSnackBar,
    private configurationService: ConfigurationService
  ) {
    this.configurationService.getFormField('Role').subscribe((response) => {
      if ((response.Status || '').toUpperCase() == 'SUCCESS') {
        this.dropDownValues =
          (response?.Subject?.dropDownValues as IFormFieldVariable[]) || [];
        this.dropDownValues.unshift({
          codeValues: 0,
          dropDownItem: 'All Roles',
        });
        const selectedItem = this.dropDownValues.find(
          (item) => item.codeValues === 3
        );

        this.selectedValues = selectedItem
          ? [new SelectedValue(selectedItem.dropDownItem, selectedItem)]
          : [];
      }
    });
  }
  filterData: any[] = [];
  monthsHeader: string[] = [];
  monthKeys: string[] = [];
  list: any[] = [];
  paginatedList: any[] = [];
  currentPage = 1;
  pageSize = 10;
  totalCoreHours: number[] = [];
  projectTotalCorehours: number[] = [];
  editedCoreHours: {
    forecastHoursId: number;
    date: string;
    coreHours: number;
  }[] = [];
  dropDownValues: any[] = [];
  selectedValues: SelectedValue[] = [];

  ngOnInit(): void {
    this.configurationService.getFormField('Role').subscribe((response) => {
      if ((response.Status || '').toUpperCase() === 'SUCCESS') {
        this.dropDownValues =
          (response?.Subject?.dropDownValues as IFormFieldVariable[]) || [];

        this.dropDownValues.unshift({
          codeValues: 0,
          dropDownItem: 'All Roles',
        });

        const selectedItem = this.dropDownValues.find(
          (item) => item.codeValues === 3
        );

        this.selectedValues = selectedItem
          ? [new SelectedValue(selectedItem.dropDownItem, selectedItem)]
          : [];

        this.getMonths();
        this.getList(this.selectedValues[0]?.item?.codeValues ?? 0);
        this.calculateProjectTotals();
        this.calculateTotals();
      }
    });
  }
  userRoleChange(event: any) {
    console.log('Role changed:', event);
    this.selectedValues = event;
    this.getList(this.selectedValues[0]?.item?.codeValues || 0);
  }
  getMonths(codeValues: number = 0): void {
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

  getList(codeValues: number): void {
    const apiUrl = `${environment.DataAPIUrl}/forecasting/project-list?codeValues=${codeValues}`;
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
    const apiUrl = `${
      environment.DataAPIUrl
    }/forecasting/user-total-hours?codeValues=${
      this.selectedValues[0]?.item?.codeValues || 0
    }`;
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
    if (isNaN(value) || value < 0) {
      input.value = '';
      return;
    }
    res.coreHoursByMonth ??= {};
    res.coreHoursByMonth[monthKey] = value;
    const existing = this.editedCoreHours.find(
      (e) => e.forecastHoursId === res.forecastHoursId && e.date === monthKey
    );
    if (existing) {
      existing.coreHours = value;
    } else {
      this.editedCoreHours.push({
        forecastHoursId: res.forecastHoursId ?? 0,
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
    const apiUrl = `${environment.DataAPIUrl}/forecasting/update`;
    this.http.put(apiUrl, this.editedCoreHours).subscribe({
      next: (response: any) => {
        this.showToastMessage('Core hours saved successfully!', 'success');
        this.editedCoreHours = [];
        this.getList(this.selectedValues[0]?.item?.codeValues || 0);
        this;
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
