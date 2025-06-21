import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-table-paginator',
  templateUrl: './table-paginator.component.html',
  styleUrls: ['./table-paginator.component.css'],
})
export class TablePaginatorComponent {
  @Input() sourceData: any[] | undefined = [];
  @Input() pageSizeOptions: number[] = [5, 10, 20, 50, 100];

  @Input() paginatedData: any[] = [];
  @Output() paginatedDataChange = new EventEmitter<any[]>();
  @Input() currentPage = 1;
  @Output() currentPageChange = new EventEmitter<number>();
  @Input() pageSize = 10;
  @Output() pageSizeChange = new EventEmitter<number>();

  constructor() {}

  public changePageSize(event: any): void {
    this.pageSize = Number(event.target.value);
    this.pageSizeChange.emit(this.pageSize);
    this.currentPage = 1;
    this.currentPageChange.emit(this.currentPage);
    this.paginateData();
  }

  public paginateData(): void {
    const total = this.sourceData?.length || 0;
    const start = (this.currentPage - 1) * this.pageSize;
    const end = Math.min(start + this.pageSize, total);
    this.paginatedData = (this.sourceData || []).slice(start, end);
    this.paginatedDataChange.emit(this.paginatedData);
  }

  get totalPages(): number {
    const totalItems = this.sourceData?.length || 0;
    return Math.max(1, Math.ceil(totalItems / this.pageSize));
  }

  public canNavFirst(): boolean {
    return this.currentPage > 1;
  }

  public canNavPrevious(): boolean {
    return this.currentPage > 1;
  }

  public canNavNext(): boolean {
    return this.currentPage < this.totalPages;
  }

  public canNavLast(): boolean {
    return this.currentPage < this.totalPages;
  }

  public navFirst(): void {
    if (this.canNavFirst()) {
      this.currentPage = 1;
      this.pageNavigation();
    }
  }

  public navPrevious(): void {
    if (this.canNavPrevious()) {
      this.currentPage--;
      this.pageNavigation();
    }
  }

  public navNext(): void {
    if (this.canNavNext()) {
      this.currentPage++;
      this.pageNavigation();
    }
  }

  public navLast(): void {
    if (this.canNavLast()) {
      this.currentPage = this.totalPages;
      this.pageNavigation();
    }
  }

  public pageNavigation(): void {
    this.currentPageChange.emit(this.currentPage);
    this.paginateData();
  }

  getPageStart(): number {
    return this.sourceData?.length
      ? (this.currentPage - 1) * this.pageSize + 1
      : 0;
  }

  getPageEnd(): number {
    const totalItems = this.sourceData?.length || 0;
    return Math.min(this.currentPage * this.pageSize, totalItems);
  }
}
