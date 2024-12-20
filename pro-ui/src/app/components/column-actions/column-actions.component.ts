import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TableHeaderItem } from '../../models/presentation/table-header-item';

@Component({
  selector: 'app-column-actions',
  templateUrl: './column-actions.component.html',
  styleUrl: './column-actions.component.css'
})
export class ColumnActionsComponent {

  @Input() headerItem: TableHeaderItem | undefined = undefined;
  @Output() headerItemChange: EventEmitter<TableHeaderItem> = new EventEmitter<TableHeaderItem>();

  constructor() { }

  public openSearch(): void {
  }

  public openFilter(): void {
  }

  public sort(sortDirection: string): void {
    if (this.headerItem) {
      this.headerItem.sortDirection = sortDirection;
      this.headerItemChange.emit(this.headerItem);
    }
  }

}
