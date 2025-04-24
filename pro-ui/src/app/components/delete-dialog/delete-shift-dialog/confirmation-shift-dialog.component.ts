import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-shift-confirmation-dialog',
  templateUrl: './confirmation-shift-dialog.component.html',
  styleUrls: ['./confirmation-shift-dialog.component.css']
})
export class ConfirmationShiftDialogComponent {
  constructor(public dialogRef: MatDialogRef<ConfirmationShiftDialogComponent>) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}