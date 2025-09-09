import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-schedule-close-dialog',
  templateUrl: './block.date.dailog.confirmation.html',
  styleUrls: ['./block.date.dailog.confirmation.css']
})
export class BlockDateConfirmationDialogComponent {
  constructor(public dialogRef: MatDialogRef<BlockDateConfirmationDialogComponent>,  @Inject(MAT_DIALOG_DATA) public data: { isTimeSlot: boolean }) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}