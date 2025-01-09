import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { TableHeaderItem } from '../../models/presentation/table-header-item';
import { Utils } from '../../classes/utils';

@Component({
  selector: 'app-column-actions',
  templateUrl: './column-actions.component.html',
  styleUrl: './column-actions.component.css'
})
export class ColumnActionsComponent {

  @Input() headerItem: TableHeaderItem | undefined = undefined;
  @Output() headerItemChange: EventEmitter<TableHeaderItem> = new EventEmitter<TableHeaderItem>();

  public showSearch: boolean = false;
  public showDropdown: boolean = false;

  public guid: string = '';

  constructor() { }

  ngOnInit(): void {
    this.guid = Utils.generateGUID();
  }

  //open the search bar
  public openSearch(): void {
    
    this.showSearch = !this.showSearch;
    
    //if showSearch is true, set focus on the search input
    if (this.showSearch) {
      setTimeout(() => {
        const searchInput = document.querySelector('.search-bar-input') as HTMLElement;
        if (searchInput) {
          searchInput.focus();
        }
      }, 0);
    }

  }

  //toggle the filter dropdown
  public toggleFilter(): void {
    this.showDropdown = !this.showDropdown;
  }

  //apply sorting
  public sort(sortDirection: string): void {
    if (this.headerItem) {
      this.headerItem.sortDirection = sortDirection;
      this.headerItemChange.emit(this.headerItem);
    }
  }

  //apply search filter
  public searchFilter(): void {
    if (this.headerItem) {
      this.headerItemChange.emit(this.headerItem);
    }
  }

  //filter change
  public filterChange(filterValue: any[]): void {
    this.headerItemChange.emit(this.headerItem);
  }

  //close the search bar when clicking outside this component
  @HostListener('document:click', ['$event'])
  handleClickOutside(event: Event) {
    const target = event.target as HTMLElement;
    this.guidClickCheck(target, '.has-guid');
  }

  public guidClickCheck(target: HTMLElement, selector: string): void {
    const clickedComponent = target.closest(selector);
    if (clickedComponent) {
      const clickedGuid = clickedComponent.getAttribute('data-guid');
      if (clickedGuid !== this.guid) {
        this.showSearch = false;
        this.showDropdown = false;
      }
    } else {
      this.showSearch = false;
    }
  }
  
  //close the search bar when the user presses escape
  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    if (event.key === 'Escape'
      || event.key === 'Enter'
      || event.key === 'Tab'
    ) {
      this.showSearch = false;
    }

  }

}
