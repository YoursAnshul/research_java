import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TableHeaderItem } from '../../models/presentation/table-header-item';
import { Utils } from '../../classes/utils';

@Component({
  selector: 'app-table-header',
  templateUrl: './table-header.component.html',
  styleUrl: './table-header.component.css'
})
export class TableHeaderComponent {

  @Input() headerItems: TableHeaderItem[] = [];
  @Output() headerItemsChange: EventEmitter<TableHeaderItem[]> = new EventEmitter<TableHeaderItem[]>();

  @Input() allSelected: boolean = false;
  @Output() allSelectedChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  constructor() { }

  public toggleAllSelected(): void {
    this.allSelected = !this.allSelected;
    this.allSelectedChange.emit(this.allSelected);
  }

  public headerItemChange(headerItem: TableHeaderItem): void {
    const index = this.headerItems.findIndex(item => item === headerItem);

    //unset all other sort directions
    this.headerItems.forEach((item, i) => {
      if (i !== index) {
      item.sortDirection = '';
      } else {
        item.sortDirection = headerItem.sortDirection;
      }
    });

    //set the sort new header item
    this.headerItems[index] = headerItem;
    this.headerItemsChange.emit(this.headerItems);
  }

}
