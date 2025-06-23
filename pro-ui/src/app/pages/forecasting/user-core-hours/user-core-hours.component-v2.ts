import { Component, OnInit } from '@angular/core';
import { SelectedValue } from '../../../models/presentation/selected-value';
import { IDropDownValue } from '../../../interfaces/interfaces';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-user-core-hours',
  templateUrl: './user-core-hours.component-v2.html',
  styleUrl: './user-core-hours.component.css',
})
export class UserCoreHoursComponentV2 implements OnInit {
  constructor(private http: HttpClient) {}
  monthsHeader: string[] = [];
  list: any[] = [];
  pageSize = 10;
  paginatedList: any[] = [];
  public currentPage: number = 1;
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

  userRoleChange(event: any) {
    console.log(event);
  }
  getMonths() {
    const now = new Date();
    for (let i = 0; i < 14; i++) {
      const date = new Date(now.getFullYear(), now.getMonth() + i);
      const month = date.toLocaleString('default', { month: 'short' });
      const year = date.getFullYear().toString().slice(-2);
      this.monthsHeader.push(`${month}-${year}`);
    }
  }
  getList(): void {
    let params = new HttpParams();

    const apiUrl = `${environment.DataAPIUrl}/forecasting/list`;
    this.http.get(apiUrl, { params }).subscribe({
      next: (data: any) => {
        this.list = data?.data;
        this.paginate();
      },
      error: (error: any) => {
        console.error('Error fetching user forecasting:', error);
      },
    });
  }
  public paginate(): void {
    if (this.list) {
      if (this.list.length <= this.pageSize) {
        this.currentPage = 1;
      }
      let maxPage: number = Math.floor(
        (this.list || []).length / this.pageSize
      );
      maxPage = maxPage == 0 ? 1 : maxPage;

      if (this.currentPage < 1) {
        this.currentPage = 1;
      }

      if (this.currentPage > maxPage) {
        this.currentPage = maxPage;
      }

      const startIndex = (this.currentPage - 1) * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      this.paginatedList = this.list.slice(startIndex, endIndex);
      this.calculateTotals();
    }
  }

  calculateTotals(): void {
    const totals = Array(14).fill(0);

    for (const res of this.paginatedList) {
      for (let i = 0; i < 14; i++) {
        const val = Number(res[`corehours${i + 1}`]);
        if (!isNaN(val)) {
          totals[i] += val;
        }
      }
    }

    this.totalCoreHours = totals;
  }
  onPageChanged(): void {
    this.calculateTotals();
  }
}
