import { Component, OnInit } from '@angular/core';
import { SelectedValue } from '../../../models/presentation/selected-value';
import {
  IAuthenticatedUser,
  IDropDownValue,
  IFormFieldVariable,
} from '../../../interfaces/interfaces';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import {
  MatSnackBar,
  MatSnackBarHorizontalPosition,
  MatSnackBarVerticalPosition,
} from '@angular/material/snack-bar';
import { ConfigurationService } from '../../../services/configuration/configuration.service';
import { AuthenticationService } from '../../../services/authentication/authentication.service';

@Component({
  selector: 'app-user-core-hours',
  templateUrl: './user-core-hours.component.html',
  styleUrls: ['./user-core-hours.component.css'],
})
export class UserCoreHoursComponent implements OnInit {
  constructor(
    private readonly http: HttpClient,
    private readonly snackBar: MatSnackBar,
    private configurationService: ConfigurationService,
    private authenticationService: AuthenticationService
  ) {
    this.configurationService.getFormField('Role').subscribe((response) => {
      if ((response.Status || '').toUpperCase() == 'SUCCESS') {
        this.dropDownValues =
          (response?.Subject?.dropDownValues as IFormFieldVariable[]) || [];
        this.dropDownValues.unshift({
          codeValues: 0,
          dropDownItem: 'All Users',
        });
        this.dropDownValues = this.dropDownValues.filter((item) =>
          ['All Users', 'Project Team', 'Interviewer'].includes(
            item.dropDownItem
          )
        );
        const selectedItem = this.dropDownValues.find(
          (item) => item.codeValues === 3
        );

        this.selectedValues = selectedItem
          ? [new SelectedValue(selectedItem.dropDownItem, selectedItem)]
          : [];
      }
    });
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
      }
    );
  }

  monthsHeader: string[] = [];
  monthKeys: string[] = [];
  list: any[] = [];
  paginatedList: any[] = [];
  currentPage = 1;
  pageSize = 10;
  totalCoreHours: number[] = [];
  projectTotalCorehours: number[] = [];

  dropDownValues: any[] = [];
  selectedValues: SelectedValue[] = [];
  editedCoreHours: {
    coreHoursId: number;
    date?: string;
    coreHours?: number;
    entryBy?: string;
    projectId?: number;
    coreHoursByMonth?: any;
    dempoid?: string
  }[] = [];
  authenticatedUser!: IAuthenticatedUser;
  isLoading: boolean = false;
  save: boolean = false;
  exportIsDisabled: boolean = false;
  ngOnInit(): void {
    this.configurationService.getFormField('Role').subscribe((response) => {
      if ((response.Status || '').toUpperCase() === 'SUCCESS') {
        this.dropDownValues =
          (response?.Subject?.dropDownValues as IFormFieldVariable[]) || [];

        this.dropDownValues.unshift({
          codeValues: 0,
          dropDownItem: 'All Users',
        });
        this.dropDownValues = this.dropDownValues.filter((item) =>
          ['All Users', 'Project Team', 'Interviewer'].includes(
            item.dropDownItem
          )
        );
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
    this.selectedValues = event;
    this.getList(this.selectedValues[0]?.item?.codeValues ?? 0);
  }

  getList(codeValues: number): void {
    this.isLoading = true;
    const apiUrl = `${environment.DataAPIUrl}/forecasting/list?codeValues=${codeValues}`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.list = data?.data || [];
        this.paginate();
        this.isLoading = false;
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
    // this.recalculateLocalTotals();
    this.calculateProjectTotals();
  }

  getCoreHour(res: any, monthIndex: number): number {
    const key = this.monthKeys[monthIndex];
    if (
      res.coreHoursByMonth &&
      typeof res.coreHoursByMonth === 'object' &&
      !Array.isArray(res.coreHoursByMonth)
    ) {
      return res.coreHoursByMonth[key] ?? 0;
    }
    const match = res.coreHoursByMonth?.find?.((m: any) => m.first === key);
    return match?.second ?? 0;
  }

  validateKeyDown(event: KeyboardEvent): void {
    const allowedKeys = [
      'Backspace',
      'ArrowLeft',
      'ArrowRight',
      'Tab',
      'Delete',
    ];
    if (allowedKeys.includes(event.key) || /^[0-9]$/.test(event.key)) {
      return;
    }

    event.preventDefault();
  }

  calculateTotals(): void {
    const apiUrl = `${
      environment.DataAPIUrl
    }/forecasting/user-total-hours?codeValues=${
      this.selectedValues[0]?.item?.codeValues || 0
    }  `;
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
  const value = parseInt(input.value, 10);
  if (isNaN(value) || value < 0) {
    input.value = '';
    return;
  }

  if (Array.isArray(res.coreHoursByMonth)) {
    const obj: { [key: string]: number } = {};
    for (const item of res.coreHoursByMonth) {
      if (item?.first && typeof item?.second === 'number') {
        obj[item.first] = item.second;
      }
    }
    res.coreHoursByMonth = obj;
  }

  res.coreHoursByMonth ??= {};
  res.coreHoursByMonth[monthKey] = value;

  this.editedCoreHours = this.editedCoreHours.filter(
    (e) => e.coreHoursId !== res.coreHoursId
  );

  const rowUpdate = {
    coreHoursId: res.coreHoursId ?? 0,
    projectId: res.projectId,
    entryBy: this.authenticatedUser.netID,
    coreHoursByMonth: { ...res.coreHoursByMonth }, 
    dempoId: res.dempoId
  };

  this.editedCoreHours.push(rowUpdate);

  // this.recalculateLocalTotals();
}

  recalculateLocalTotals(): void {
    const monthLength = this.monthKeys.length;
    this.totalCoreHours = new Array(monthLength).fill(0);

    for (const res of this.list) {
      for (let i = 0; i < monthLength; i++) {
        const key = this.monthKeys[i];
        let val = 0;

        if (Array.isArray(res?.coreHoursByMonth)) {
          const match = res.coreHoursByMonth.find((m: any) => m.first === key);
          val = match?.second ?? 0;
        } else if (
          res?.coreHoursByMonth &&
          typeof res.coreHoursByMonth === 'object'
        ) {
          val = res.coreHoursByMonth[key] ?? 0;
        }

        this.totalCoreHours[i] += val;
      }
    }
  }

  saveCoreHours(): void {
    this.save = true;
    if (this.editedCoreHours.length === 0) {
      this.showToastMessage('No changes to save.', 'error');
      this.isLoading = false;
      this.save = false;
      return;
    }

    const payload = this.editedCoreHours.map((e) => ({
      ...e,
      entryBy: this.authenticatedUser?.netID ?? 'system',
    }));

    const apiUrl = `${environment.DataAPIUrl}/forecasting/user-core-update`;

    this.http.put(apiUrl, payload).subscribe({
      next: (response: any) => {
        this.showToastMessage('Core hours saved successfully!', 'success');
        this.editedCoreHours = [];

        const codeValue = this.selectedValues[0]?.item?.codeValues ?? 0;
        this.save = false;
        this.getList(codeValue);
      },
      error: (error: any) => {
        console.error('Error saving core hours:', error);
        this.save = false;
        this.showToastMessage('Error saving core hours.', 'error');
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
  export() {
    this.exportIsDisabled = true;
    const apiUrl = `${environment.DataAPIUrl}/forecasting/export`;
    const params = new HttpParams().set(
      'codeValues',
      this.selectedValues[0]?.item?.codeValues || 0
    );

    this.http.get(apiUrl, { params, responseType: 'blob' }).subscribe({
      next: (response: Blob) => {
        const blob = new Blob([response], { type: 'application/vnd.ms-excel' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'forecasting.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
        this.exportIsDisabled = false;
      },
      error: (error: any) => {
        this.exportIsDisabled = false;
        console.error('Error exporting core hours:', error);
      },
    });
  }
}
