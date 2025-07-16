  import { Component, OnInit } from '@angular/core';
  import { SelectedValue } from '../../../models/presentation/selected-value';
  import { IDropDownValue } from '../../../interfaces/interfaces';
  import { environment } from '../../../../environments/environment';
  import {
    MatSnackBar,
    MatSnackBarHorizontalPosition,
    MatSnackBarVerticalPosition,
  } from '@angular/material/snack-bar';
  import { HttpClient } from '@angular/common/http';

  @Component({
    selector: 'app-project-forecasting-v2',
    templateUrl: './project-forecasting.component-v2.html',
    styleUrl: './project-forecasting.component.css',
  })
  export class ProjectForecastingComponentV2 implements OnInit {
    constructor(
      private readonly http: HttpClient,
      private readonly snackBar: MatSnackBar
    ) { }
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
    dropDownValues: IDropDownValue[] = [
      { codeValues: 1, dropDownItem: 'Interviewer' },
      { codeValues: 2, dropDownItem: 'Resource Group' },
    ];
    selectedValues: SelectedValue[] = [
      new SelectedValue(1, { codeValues: 1, dropDownItem: 'Interviewer' }),
    ];

    ngOnInit(): void {
      this.getMonths();
      this.getList();
      this.calculateProjectTotals();
      this.calculateTotals();
    }
    userRoleChange(event: any) {
      console.log('Role changed:', event);
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

    getList(): void {
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
    validateKeyDown(event: KeyboardEvent): void {
      const allowedKeys = [
        'Backspace', 'ArrowLeft', 'ArrowRight', 'Tab', 'Delete'
      ];
      if (
        allowedKeys.includes(event.key) ||
        /^[0-9]$/.test(event.key)
      ) {
        return;
      }

      event.preventDefault();
    }


    onPageChanged(): void {
      this.calculateProjectTotals();
      this.calculateTotals();
    }



  }
