import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { environment } from '../../../environments/environment';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatTabChangeEvent } from '@angular/material/tabs';

@Component({
  selector: 'app-shift-schedule',
  templateUrl: './shift-schedule.component.html',
  styleUrls: ['./shift-schedule.component.css'],
})
export class ShiftScheduleComponent implements OnInit {
  userList: any[] = [];
  projectList: any[] = [];
  selectedProjects: any[] = []; // Added to track selected projects
  announcementForm!: FormGroup;
  currentDay: string = '';
  duration: string = '0 hr';
  scheduleFetchStatus: boolean = false;

  selectedDate = new FormControl<Date | null>(new Date(), Validators.required);
  currentYear: number = new Date().getFullYear();
  currentMonth: number = new Date().getMonth() + 1;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.getAuthor();
    this.getProjectInfo();
    this.currentDay = new Intl.DateTimeFormat('en-US', {
      weekday: 'long',
    }).format(new Date());

    // Initialize Form
    this.announcementForm = new FormGroup({
      user: new FormControl(null, Validators.required),
      projects: new FormControl([], Validators.required),
      dayWiseDate: new FormControl(new Date(), Validators.required),
      startTime: new FormControl('', Validators.required),
      endTime: new FormControl('', Validators.required),
      comments: new FormControl(''),
    });

    this.announcementForm.valueChanges.subscribe(() => {
      this.updateDuration();
      this.scheduleFetchStatus = this.announcementForm.valid;
    });    
  }
  onSubmit(): void {
    if (this.announcementForm.valid) {
      console.log("Form Submitted:", this.announcementForm.value);
    } else {
      console.log("Form is invalid");
    }
  }
  

  getProjectInfo(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/projects`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.projectList = Array.isArray(data) ? data : [];
        console.log('Project List:', this.projectList);
      },
      error: (error) => console.error('Error fetching projects:', error),
    });
  }

  getAuthor(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/authors`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.userList = Array.isArray(data) ? data : [];
        console.log('Author List:', this.userList);
      },
      error: (error) => console.error('Error fetching authors:', error),
    });
  }

  // Check if all projects are selected
  isAllSelected(): boolean {
    return this.selectedProjects.length === this.projectList.length;
  }

  // Handle project selection checkbox change
  onCheckboxChange(event: any): void {
    const isChecked = event.target.checked;
    this.selectedProjects = isChecked ? [...this.projectList] : [];
  }

  // Determine if checkbox should be indeterminate
  isIndeterminate(): boolean {
    return (
      this.selectedProjects.length > 0 &&
      this.selectedProjects.length < this.projectList.length
    );
  }

  // Get dates for the current month
  getDatesForMonth(): string[][] {
    const dates: string[][] = [];
    let date = new Date(this.currentYear, this.currentMonth - 1, 1);
    while (date.getMonth() === this.currentMonth - 1) {
      const week: string[] = [];
      for (let i = 0; i < 7; i++) {
        week.push(`${date.getDate()}/${date.getMonth() + 1}`);
        date.setDate(date.getDate() + 1);
        if (date.getMonth() !== this.currentMonth - 1) break;
      }
      dates.push(week);
    }
    return dates;
  }

  // Handle tab change event
  onTabChanged(tabChangeEvent: MatTabChangeEvent): void {
    console.log('Tab Change Event =>', tabChangeEvent);
  }

  // Emit selected date
  emitSelectedDate(): void {
    console.log('Selected Date:', this.selectedDate.value);
  }

  addDateUnitsToSelectedDate(unit: number): void {
    let selectedDt = new Date();
    if (this.selectedDate && this.selectedDate.value) {
      selectedDt = new Date(this.selectedDate.value);
    }
    selectedDt.setDate(selectedDt.getDate() + unit);
    this.selectedDate.setValue(selectedDt);
    this.emitSelectedDate();
  }
  updateDuration(): void {
    const start = this.announcementForm.get('startTime')?.value;
    const end = this.announcementForm.get('endTime')?.value;

    if (!start || !end) {
      this.duration = '0 hr 0 min';
      return;
    }

    const startDate = this.parseTime(start);
    const endDate = this.parseTime(end);

    if (endDate <= startDate) {
      endDate.setDate(endDate.getDate() + 1);
    }

    const diffMs = endDate.getTime() - startDate.getTime();
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.round((diffMs % (1000 * 60 * 60)) / (1000 * 60));

    this.duration = `${diffHours} hr ${diffMinutes} min`;
  }
  parseTime(time: string): Date {
    const date = new Date();
    const [timePart, period] = time.split(' ');
    let [hours, minutes] = timePart.split(':').map(Number);

    hours =
      period === 'PM' && hours !== 12
        ? hours + 12
        : period === 'AM' && hours === 12
        ? 0
        : hours;

    date.setHours(hours, minutes, 0, 0);
    return date;
  }
}
