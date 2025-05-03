import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-monthly-block-date-dialog',
  templateUrl: './monthly.block.out.dialog.component.html',
  styleUrls: ['./monthly.block.out.dialog.component.css']
})
export class MonthlyBlockDate {
  constructor(public dialogRef: MatDialogRef<MonthlyBlockDate>) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}