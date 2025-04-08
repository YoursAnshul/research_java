import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-block-date-dialog',
  templateUrl: './block.date.dialog.component.html',
  styleUrls: ['./block.date.dialog.component.css']
})
export class BlockdateDialog {
  constructor(public dialogRef: MatDialogRef<BlockdateDialog>, @Inject(MAT_DIALOG_DATA) public data: { isTimeSlot: boolean }) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}