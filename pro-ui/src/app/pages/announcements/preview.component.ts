import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-preview',
  templateUrl: './preview.component.html',
  styleUrls: ['./preview.component.css'],
})
export class PreviewComponent implements OnInit {
  announcementsList: any = [];

  private announcements = [
    {
      title: 'This is a project-specific announcement',
      content:
        'Providing information only to users who are trained on or otherwise have access to the specified projects.',
      date: new Date(),
      tags: ['CARRA', 'HERO Together', 'Project Eleven'],
    },
  ];
  constructor(public dialogRef: MatDialogRef<PreviewComponent>) {}

  ngOnInit(): void {
    this.announcementsList = this.getAnnouncements();
  }

  getAnnouncements() {
    return this.announcements;
  }
  getTagColor(tag: string): string {
    switch (tag) {
      case 'CARRA':
        return '#74EBA4';
      case 'HERO Together':
        return '#F6C867';
      case 'Project Eleven':
        return '#87CEFA';
      default:
        return '#d3d3d3';
    }
  }
  closePreview(): void {
    this.closeDialog();
  }
  closeDialog(): void {
    this.dialogRef.close();
  }
}
