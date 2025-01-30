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
  saveUser: any = () =>{ };
  isUserProfile: boolean  = false;
  constructor(@Inject(MAT_DIALOG_DATA) public data: any, private dialogRef: MatDialogRef<UnsavedChangesDialogComponent>) {
    this.dialogType = data.dialogType; // 'confirmation' or 'error' or any other type
    this.netId = data.netId;
    this.saveUser = data.saveUser;
    this.isUserProfile = data.isUserProfile;
  }

  close(): void {
    this.dialogRef.close(true); // Proceed with closing
  }

  cancel(): void {
    this.dialogRef.close(false); // Stay on the page
  }

  closeWindon(): void {
    this.dialogRef.close("close");
  }

  discardChanges(): void {
    this.dialogRef.close("discardChanges");
  }
  save(): void {
    this.saveUser().then((r:any )=> {
      this.dialogRef.close("save");
    }).catch((e: any) => {
      this.dialogRef.close("reject");
    })
  }

  return(): void {
    this.dialogRef.close("return");
  }
}