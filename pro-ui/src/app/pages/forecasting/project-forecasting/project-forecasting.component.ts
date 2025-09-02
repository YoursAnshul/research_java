import { Component, OnInit } from '@angular/core';
import { SelectedValue } from '../../../models/presentation/selected-value';
import {
  IAuthenticatedUser,
  IDropDownValue,
  IFormFieldVariable,
  IProjectMin,
} from '../../../interfaces/interfaces';
import { environment } from '../../../../environments/environment';
import {
  MatSnackBar,
  MatSnackBarHorizontalPosition,
  MatSnackBarVerticalPosition,
} from '@angular/material/snack-bar';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ConfigurationService } from '../../../services/configuration/configuration.service';
import { Utils } from '../../../classes/utils';
import { ProjectsService } from '../../../services/projects/projects.service';
import { AuthenticationService } from '../../../services/authentication/authentication.service';
import { UserRole } from '../../../models/presentation/enums';
@Component({
  selector: 'app-project-forecasting',
  templateUrl: './project-forecasting.component.html',
  styleUrl: './project-forecasting.component.css',
})
export class ProjectForecastingComponent implements OnInit {
  UserRoles: any = UserRole;
  constructor(
    private readonly http: HttpClient,
    private readonly snackBar: MatSnackBar,
    private configurationService: ConfigurationService,
    private projectsService: ProjectsService,
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
      this.authenticationService.authenticatedUser.subscribe(
        (authenticatedUser) => {
          this.authenticatedUser = authenticatedUser;
        }
      );
    });

    this.projectsService.allProjectsMin.subscribe((allProjects) => {
      this.activeProjects = allProjects.filter(
        (x) => x.active && x.projectType !== 'Administrative'
      );

      //setup active projects as an iDropDownValue type
      this.activeProjectsDv = Utils.convertObjectArrayToDropDownValues(
        this.activeProjects,
        'projectID',
        'projectName'
      );
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
    date?: string;
    coreHours?: number;
    entryBy?: string;
    projectId?: number;
    coreHoursByMonth?: any;
  }[] = [];

  dropDownValues: any[] = [];
  selectedValues: SelectedValue[] = [];
  public activeProjects: IProjectMin[] = [];
  public activeProjectsDv: IDropDownValue[] = [];
  public selectedProjects: SelectedValue[] = [];
  public projectsAnySelected: boolean = true;
  authenticatedUser!: IAuthenticatedUser;


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
        this.getList();
        this.calculateProjectTotals();
        this.calculateTotals();
      }
    });
  }
  userRoleChange(event: any) {
    this.selectedValues = event;
    this.getList();
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

  getList(): void {
    // let codeValues = [];
    // for (let i = 0; i < this.selectedProjects.length; i++) {
    //   if (this.selectedProjects[i]?.value !== 0) {
    //     codeValues.push(this.selectedProjects[i].value);
    //   }
    // }
    const apiUrl = `${environment.DataAPIUrl}/forecasting/project-list`;
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
    // let codeValues = [];
    // for (let i = 0; i < this.selectedProjects.length; i++) {
    //   if (this.selectedProjects[i]?.value !== 0) {
    //     codeValues.push(this.selectedProjects[i].value);
    //   }
    // }
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
      (e) => e.forecastHoursId !== res.forecastHoursId
    );
    const rowUpdate = {
      forecastHoursId: res.forecastHoursId ?? 0,
      projectId: res.projectId,
      entryBy: this.authenticatedUser.netID,
      coreHoursByMonth: { ...res.coreHoursByMonth }, 
    };
    this.editedCoreHours.push(rowUpdate);
    // this.recalculateTotalsFromList();
  }

  recalculateTotalsFromList(): void {
    const monthCount = this.monthKeys.length;
    this.projectTotalCorehours = new Array(monthCount).fill(0);

    for (const row of this.list) {
      if (Array.isArray(row.coreHoursByMonth)) {
        const obj: { [key: string]: number } = {};
        for (const item of row.coreHoursByMonth) {
          if (item?.first && typeof item?.second === 'number') {
            obj[item.first] = item.second;
          }
        }
        row.coreHoursByMonth = obj;
      }

      row.coreHoursByMonth ??= {};

      for (let i = 0; i < monthCount; i++) {
        const monthKey = this.monthKeys[i];

        let baseValue = row.coreHoursByMonth[monthKey] ?? 0;
        const edited = this.editedCoreHours.find(
          (e) =>
            e.forecastHoursId === row.forecastHoursId && e.date === monthKey
        );
        if (edited) {
          baseValue = edited.coreHours;
        }

        this.projectTotalCorehours[i] += baseValue;
      }
    }
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

  saveCoreHours(): void {
    if (this.editedCoreHours.length === 0) {
      this.showToastMessage('No changes to save.', 'error');
      return;
    }


    this.editedCoreHours = this.editedCoreHours.map((e) => ({
      ...e,
      entryBy: this.authenticatedUser.netID,
    }));

    const apiUrl = `${environment.DataAPIUrl}/forecasting/update`;

    this.http.put(apiUrl, this.editedCoreHours).subscribe({
      next: (response: any) => {
        this.showToastMessage('Core hours saved successfully!', 'success');
        this.editedCoreHours = [];
        this.getList();
      },
      error: (error: any) => {
        console.error('Error saving core hours:', error);
        this.showToastMessage('Failed to save core hours.', 'error');
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
  projectFilterChange(event: any): void {
    this.selectedProjects = event;
    this.getList();
  }
  export() {
    const apiUrl = `${environment.DataAPIUrl}/forecasting/export`;
    // let codeValues = [];
    // for (let i = 0; i < this.selectedProjects.length; i++) {
    //   if (this.selectedProjects[i]?.value !== 0) {
    //     codeValues.push(this.selectedProjects[i].value);
    //   }
    // }
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
      },
      error: (error: any) => {
        console.error('Error exporting core hours:', error);
      },
    });
  }
}
