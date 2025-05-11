import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-scheduling-lever-dialog',
  templateUrl: './scheduling-lever-dialog.html',
  styleUrls: ['./scheduling-lever-dialog.css'],
})
export class SchedulingLevelDialog {
  constructor(
    public dialogRef: MatDialogRef<SchedulingLevelDialog>,
    @Inject(MAT_DIALOG_DATA) public data: { message: string }
  ) {}
  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
