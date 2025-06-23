import { Component, OnInit } from '@angular/core';
import { SelectedValue } from '../../../models/presentation/selected-value';
import { IDropDownValue } from '../../../interfaces/interfaces';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-user-core-hours',
  templateUrl: './user-core-hours.component-v2.html',
  styleUrls: ['./user-core-hours.component.css'],
})
export class UserCoreHoursComponentV2 implements OnInit {
  constructor(private http: HttpClient) {}

  monthsHeader: string[] = [];
  monthKeys: string[] = [];
  list: any[] = [];
  paginatedList: any[] = [];
  currentPage = 1;
  pageSize = 10;
  totalCoreHours: number[] = [];

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
  }

  getCoreHour(res: any, monthIndex: number): number {
    const key = this.monthKeys[monthIndex];
    return res.coreHoursByMonth?.[key] ??0;
  }

  calculateTotals(): void {
    const totals = Array(this.monthsHeader.length).fill(0);
    for (let i = 0; i < this.monthsHeader.length; i++) {
      for (const res of this.paginatedList) {
        totals[i] += this.getCoreHour(res, i);
      }
    }
    this.totalCoreHours = totals;
  }

  onPageChanged(): void {
    this.calculateTotals();
  }
}
