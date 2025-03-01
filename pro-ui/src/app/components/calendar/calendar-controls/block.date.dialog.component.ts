import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-block-date-dialog',
  templateUrl: './block.date.dialog.component.html',
  styleUrls: ['./block.date.dialog.component.css']
})
export class BlockdateDialog {
  constructor(public dialogRef: MatDialogRef<BlockdateDialog>) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}