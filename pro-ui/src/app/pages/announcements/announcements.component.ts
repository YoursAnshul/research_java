import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-announcements',
  templateUrl: './announcements.component.html',
  styleUrls: ['./announcements.component.css'],
})
export class AnnouncementsComponent implements OnInit {
  announcementsList: any = [];

  private announcements = [
    {
      title: 'This is a project-specific announcement',
      content:
        'Providing information only to users who are trained on or otherwise have access to the specified projects.',
      date: new Date(),
      tags: ['CARRA', 'HERO Together', 'Project Eleven'],
    },
    {
      title: 'Happy Birthday Michelle!',
      content:
        'The PRO team wishes Michelle Rogers a very happy birthday today!',
      date: new Date(),
    },
    {
      title: 'Scheduled Maintenance Friday, November 18',
      content:
        'PRO platform will be unavailable Friday. 11/18/23 from 5:30 PM EST until 6:30 PM EST for routine maintenance. You should be able to resume normal  activity by 6:30 PM EST.',
      date: new Date('2023-11-12'),
    },
    {
      title: 'Remember to time in!',
      content:
        'Reminder to all team members to time in when starting your shift. We have seen an increase in missed and late timestamps this week. Thank you!',
      date: new Date('2023-11-04'),
    },
  ];

  constructor() {}

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
}
