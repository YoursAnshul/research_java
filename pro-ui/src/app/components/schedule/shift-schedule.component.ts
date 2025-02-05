import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { environment } from '../../../environments/environment';
import { FormControl, FormGroup } from '@angular/forms';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { Utils } from '../../classes/utils';


@Component({
  selector: 'app-shift-schedule',
  templateUrl: './shift-schedule.component.html',
  styleUrls: ['./shift-schedule.component.css'],
})
export class ShiftScheduleComponent implements OnInit {
  userList: any[] = [];
  selectedUser: any = null;
  projectList: any[] = [];
  selectedProjects: any[] = [];
  allSelected = false;
  isAnyProjectsSelected: boolean = false;
  announcementForm!: FormGroup;
  daysOfWeek = [
    'Monday',
    'Tuesday',
    'Wednesday',
    'Thursday',
    'Friday',
    'Saturday',
    'Sunday',
  ];
  currentMonth = 1;
  currentYear = 2025;
  selectedDate: FormControl = new FormControl(new Date());
  scheduleFetchMessage: string = '';
  scheduleFetchStatus!: boolean;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.getAuthor();
    this.getProjectInfo();
    this.scheduleFetchMessage =
      'last refresh: ' + Utils.formatDateToTimeString(new Date(), false, true);
  }
  getProjectInfo(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/projects`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.projectList = data;
        console.log(' this.projectList--', this.projectList);
      },
      error: (error: any) => {
        console.error('Error fetching project info:', error);
      },
    });
  }
  getAuthor(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/authors`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.userList = Array.isArray(data) ? data : [];
        console.log('Author List:', this.userList);
      },
      error: (error: any) => {
        console.error('Error fetching authors:', error);
      },
    });
  }
  isAllSelected(): boolean {
    return (this.isAnyProjectsSelected =
      this.selectedProjects.length === this.projectList.length);
  }
  onCheckboxChange(event: any): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      this.selectedProjects = [...this.projectList];
    } else {
      this.selectedProjects = [];
    }
  }
  isIndeterminate(): boolean {
    return (
      this.selectedProjects.length > 0 &&
      this.selectedProjects.length < this.projectList.length
    );
  }
  getDatesForMonth(): string[][] {
    const dates: string[][] = [];
    let date = new Date(this.currentYear, this.currentMonth - 1, 1);
    while (date.getMonth() === this.currentMonth - 1) {
      const week: string[] = [];
      for (let i = 0; i < 7; i++) {
        week.push(date.getDate() + '/' + (date.getMonth() + 1));
        date.setDate(date.getDate() + 1);
        if (date.getMonth() !== this.currentMonth - 1) break;
      }
      dates.push(week);
    }
    return dates;
  }
  onTabChanged(tabChangeEvent: MatTabChangeEvent): void {
    console.log('tabChangeEvent => ', tabChangeEvent);
  }
  emitSelectedDate(): void {
    let selectedDt: Date = new Date(this.selectedDate.value);
    console.log('selectedDt--->', selectedDt);
  }

  public addDateUnitsToSelectedDate(unit: number): void {
    let selectedDt: Date = new Date(this.selectedDate.value);
    selectedDt.setDate(selectedDt.getDate() + unit);
    this.selectedDate.setValue(selectedDt);
    this.emitSelectedDate();
  }
  public getAllUserSchedulesByAnchorDate(): void {
  }
}
