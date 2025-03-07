import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-calendar-save-dialog',
  templateUrl: './calendar.save.dialog.component.html',
  styleUrls: ['./calendar.save.dialog.component.css']
})
export class CalendarSaveDialogComponent {
  constructor(public dialogRef: MatDialogRef<CalendarSaveDialogComponent>) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}