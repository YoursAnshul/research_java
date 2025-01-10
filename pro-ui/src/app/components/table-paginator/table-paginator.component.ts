import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-table-paginator',
  templateUrl: './table-paginator.component.html',
  styleUrl: './table-paginator.component.css'
})
export class TablePaginatorComponent {

  @Input() sourceData: any[] | undefined = [];
  @Input() pageSizeOptions: number[] = [5, 10, 20, 50, 100];

  @Input() paginatedData: any[] = [];
  @Output() paginatedDataChange: EventEmitter<any[]> = new EventEmitter<any[]>();
  @Input() currentPage: number = 1;
  @Output() currentPageChange: EventEmitter<number> = new EventEmitter<number>();
  @Input() pageSize: number = 10;
  @Output() pageSizeChange: EventEmitter<number> = new EventEmitter<number>();

  constructor() { }

  //change the page size
  public changePageSize(event: any): void {
    this.pageSize = event.target.value;
    this.pageSizeChange.emit(this.pageSize);
    this.paginateData();
  }

  //paginate the data
  public paginateData(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    this.paginatedData = (this.sourceData || []).slice(start, end);
    this.paginatedDataChange.emit(this.paginatedData);
  }

  //-------------------------------------------
  // Navigation Availability Checks
  //-------------------------------------------

  //check if the user can navigate to the first page
  public canNavFirst(): boolean {

    if (this.currentPage === 1) {
      return false;
    } else {
      return true;
    }
    
  }

  //check if the user can navigate to the previous page
  public canNavPrevious(): boolean {
    if (this.currentPage === 1) {
      return false;
    } else {
      return true;
    }
  }

  //check if the user can navigate to the next page
  public canNavNext(): boolean {
    if (this.currentPage === (this.sourceData || []).length / this.pageSize) {
      return false;
    } else {
      return true;
    }
  }

  //check if the user can navigate to the last page
  public canNavLast(): boolean {
    if (this.currentPage === (this.sourceData || []).length / this.pageSize) {
      return false;
    } else {
      return true;
    }
  }

  //-------------------------------------------
  // Navigation
  //-------------------------------------------
  
  //navigate to the first page
  public navFirst(): void {
    if (!this.canNavFirst()) {
      return;
    }
    this.currentPage = 1;
    this.pageNavigation();
  }

  //navigate to the previous page
  public navPrevious(): void {
    if (!this.canNavPrevious()) {
      return;
    }
    this.currentPage--;
    this.pageNavigation();
  }

  //navigate to the next page
  public navNext(): void {
    if (!this.canNavNext()) {
      return;
    }
    this.currentPage++;
    this.pageNavigation();
  }

  //navigate to the last page
  public navLast(): void {
    if (!this.canNavLast()) {
      return;
    }
    this.currentPage = Math.floor((this.sourceData || []).length / this.pageSize);
    this.pageNavigation();
  }

  //unified navigation actions
  public pageNavigation(): void {
    this.currentPageChange.emit(this.currentPage);
    this.paginateData();
  }

  //get the page start
  getPageStart(): number {
    return (this.currentPage - 1) * this.pageSize + 1;
  }

  //get the page end
  getPageEnd(): number {
    return ((this.sourceData || []).length < this.pageSize ? (this.sourceData || []).length : this.currentPage * this.pageSize);
  }

}
