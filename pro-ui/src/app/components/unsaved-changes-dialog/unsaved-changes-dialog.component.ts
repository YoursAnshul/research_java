import { Component, Inject } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-unsaved-changes-dialog',
  templateUrl: './unsaved-changes-dialog.component.html',
  styleUrls: ['./unsaved-changes-dialog.component.css'],
})
export class UnsavedChangesDialogComponent {
  dialogType: string;
  netId: string;
  constructor(@Inject(MAT_DIALOG_DATA) public data: any, private dialogRef: MatDialogRef<UnsavedChangesDialogComponent>) {
    console.log('---- data>', data);
    this.dialogType = data.dialogType; // 'confirmation' or 'error' or any other type
    this.netId = data.netId;
  }

  close(): void {
    this.dialogRef.close(true); // Proceed with closing
  }

  cancel(): void {
    this.dialogRef.close(false); // Stay on the page
  }
}