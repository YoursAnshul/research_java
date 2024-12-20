import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-preview',
  templateUrl: './preview.component.html',
  styleUrls: ['./preview.component.css'],
})
export class PreviewComponent implements OnInit {
  announcementsList: any = [];
  previewobj: any;
  constructor(
    public dialogRef: MatDialogRef<PreviewComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.previewobj = data;    
  }
  ngOnInit(): void {}

  closePreview(): void {
    this.closeDialog();
  }
  closeDialog(): void {
    this.dialogRef.close();
  }
  isString(value: any): boolean {
    return typeof value === 'string';
  }

  isArray(value: any): boolean {
    return Array.isArray(value);
  }
}
