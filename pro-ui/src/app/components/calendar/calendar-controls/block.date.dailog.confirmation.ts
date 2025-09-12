import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-schedule-close-dialog',
  templateUrl: './block.date.dailog.confirmation.html',
  styleUrls: ['./block.date.dailog.confirmation.css']
})
export class BlockDateConfirmationDialogComponent {
  constructor(public dialogRef: MatDialogRef<BlockDateConfirmationDialogComponent>) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}