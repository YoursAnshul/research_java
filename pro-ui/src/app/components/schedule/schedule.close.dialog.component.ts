import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-schedule-close-dialog',
  templateUrl: './schedule.close.dialog.component.html',
  styleUrls: ['./schedule.close.dialog.component.css']
})
export class ScheduleCloseDialogComponent {
  constructor(public dialogRef: MatDialogRef<ScheduleCloseDialogComponent>) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}