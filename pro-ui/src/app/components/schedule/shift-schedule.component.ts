import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { environment } from '../../../environments/environment';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatTabChangeEvent } from '@angular/material/tabs';
import {
  IAuthenticatedUser,
  IBlockOutDate,
  IWeekSchedules,
} from '../../interfaces/interfaces';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ShifCalendarComponent } from '../calendar/shift.calendar.component';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { BlockdateDialog } from '../calendar/calendar-controls/block.date.dialog.component';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { ScheduleCloseDialogComponent } from './schedule.close.dialog.component';
import { CalendarSaveDialogComponent } from '../calendar/calendar-controls/calendar.save.dialog.component';
import { MatSelectChange } from '@angular/material/select';
import {
  MatSnackBar,
  MatSnackBarHorizontalPosition,
  MatSnackBarVerticalPosition,
} from '@angular/material/snack-bar';

@Component({
  selector: 'app-shift-schedule',
  templateUrl: './shift-schedule.component.html',
  styleUrls: ['./shift-schedule.component.css'],
})
export class ShiftScheduleComponent implements OnInit {
  userList: any[] = [];
  projectList: any[] = [];
  selectedProjects: any[] = []; // Added to track selected projects
  shiftForm!: FormGroup;
  currentDay: string = '';
  duration: string = '0 hr';
  scheduleFetchStatus: boolean = false;
  selectedDate = new FormControl<Date | null>(new Date(), Validators.required);
  currentYear: number = new Date().getFullYear();
  currentMonth: number = new Date().getMonth() + 1;
  shiftSchedule: any[] = [];
  weekSchedules: IWeekSchedules[] = []; // Data for Week View
  timeSlots: string[] = [
    '8:00 AM',
    '8:30 AM',
    '9:00 AM',
    '9:30 AM',
    '10:00 AM',
    '10:30 AM',
    '11:00 AM',
    '11:30 AM',
    '12:00 PM',
    '12:30 PM',
    '1:00 PM',
    '1:30 PM',
    '2:00 PM',
    '2:30 PM',
    '3:00 PM',
    '3:30 PM',
    '4:00 PM',
    '4:30 PM',
    '5:00 PM',
    '5:30 PM',
    '6:00 PM',
    '6:30 PM',
    '7:00 PM',
    '7:30 PM',
    '8:00 PM',
    '8:30 PM',
    '9:00 PM',
    '9:30 PM',
    '10:00 PM',
    '10:30 PM',
  ];
  endtimeSlots: string[] = [
    '8:30 AM',
    '9:00 AM',
    '9:30 AM',
    '10:00 AM',
    '10:30 AM',
    '11:00 AM',
    '11:30 AM',
    '12:00 PM',
    '12:30 PM',
    '1:00 PM',
    '1:30 PM',
    '2:00 PM',
    '2:30 PM',
    '3:00 PM',
    '3:30 PM',
    '4:00 PM',
    '4:30 PM',
    '5:00 PM',
    '5:30 PM',
    '6:00 PM',
    '6:30 PM',
    '7:00 PM',
    '7:30 PM',
    '8:00 PM',
    '8:30 PM',
    '9:00 PM',
    '9:30 PM',
    '10:00 PM',
    '10:30 PM',
    '11:00 PM',
  ];
  isDuplicateSchedule = false;
  blockOutDates: IBlockOutDate[] = [];
  isBlockDate = false;
  authenticatedUser!: IAuthenticatedUser;
  userObj: any;
  selectedUser: any = null;
  @Output() addDateEvent = new EventEmitter<Date>();
  addedDate = new FormControl<Date | null>(new Date(), Validators.required);
  selectedProject: any = null;
  @Input() date!: FormControl;
  @Output() selectedUserChange = new EventEmitter<any>();
  filterUser: any = null;
  filterProject: any = null;
  @Output() selectedProjectChange = new EventEmitter<any>();
  private lastCalledDate: string | null = null; // Track last called date

  constructor(
    private http: HttpClient,
    private dialogRef: MatDialogRef<ShifCalendarComponent>,
    private configurationService: ConfigurationService,
    private dialog: MatDialog,
    private authenticationService: AuthenticationService,
    private snackBar: MatSnackBar
  ) {
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
      }
    );
  }
  ngOnChanges(): void {}
  getBackgroundColor(time: string): string {
    return time.includes('AM') ? '#FFF5BF' : '#DDE0EF';
  }
  confirmatationClose(): void {
    const dialogRef = this.dialog.open(ScheduleCloseDialogComponent, {
      panelClass: 'custom-dialog-container',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.onClose();
      }
    });
  }
  onClose(): void {
    this.dialogRef.close();
  }

  ngOnInit(): void {
    this.getBlockOutDates();
    this.getAuthor();
    if (!this.authenticatedUser.interviewer) {
      this.getScheduleList();
    }
    this.currentDay = new Intl.DateTimeFormat('en-US', {
      weekday: 'long',
    }).format(new Date());

    this.shiftForm = new FormGroup({
      user: new FormControl(null, Validators.required),
      projects: new FormControl([], Validators.required),
      dayWiseDate: new FormControl(new Date(), Validators.required),
      startTime: new FormControl(null, Validators.required),
      endTime: new FormControl(null, Validators.required),
      comments: new FormControl(''),
    });

    this.shiftForm.valueChanges.subscribe(() => {
      this.updateDuration();
      this.scheduleFetchStatus = this.shiftForm.valid;
    });

    this.shiftForm.get('dayWiseDate')?.valueChanges.subscribe((date) => {
      if (date) {
        this.validateBlockOutDate(new Date(date));
        this.updateDayLabel(date);
      }
    });

    // const dayWiseDateControl = this.shiftForm.get('dayWiseDate');
    // dayWiseDateControl?.setErrors(null);
    // dayWiseDateControl?.markAsTouched();
    // dayWiseDateControl?.markAsDirty();

    this.shiftForm.get('startTime')?.valueChanges.subscribe(() => {
      this.clearValidation();
    });

    this.shiftForm.get('endTime')?.valueChanges.subscribe(() => {
      this.clearValidation();
    });

    this.shiftForm.get('user')?.valueChanges.subscribe(() => {
      this.clearValidation();
    });
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
        this.userObj = this.authenticatedUser;
      }
    );
  }

  validateBlockOutDate(selectedDate: Date): void {
    if (!this.blockOutDates || this.blockOutDates.length === 0) {
      console.log('Block out dates not loaded yet.');
      return;
    }

    const selectedDateOnly = new Date(
      selectedDate.getFullYear(),
      selectedDate.getMonth(),
      selectedDate.getDate()
    );

    const isBlocked = this.blockOutDates.some((blockOut) => {
      const blockOutDate = new Date(blockOut.blockOutDay!);
      const blockOutDateOnly = new Date(
        blockOutDate.getFullYear(),
        blockOutDate.getMonth(),
        blockOutDate.getDate()
      );
      return blockOutDateOnly.getTime() === selectedDateOnly.getTime();
    });

    if (isBlocked && this.authenticatedUser?.interviewer) {
      this.confirmationPopup();
      this.shiftForm.get('dayWiseDate')?.setErrors({ blocked: true });
      this.shiftForm.get('startTime')?.disable();
      this.shiftForm.get('endTime')?.disable();
    } else if (this.authenticatedUser?.interviewer) {
      const dayWiseDateControl = this.shiftForm.get('dayWiseDate');
      dayWiseDateControl?.setErrors(null);
      dayWiseDateControl?.markAsTouched();
      dayWiseDateControl?.markAsDirty();
      this.shiftForm.get('startTime')?.enable();
      this.shiftForm.get('endTime')?.enable();
    }
  }

  confirmationPopup(): void {
    const dialogRef = this.dialog.open(BlockdateDialog, {
      panelClass: 'custom-dialog-container',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.closeDialog();
      }
    });
  }
  closeDialog(): void {
    this.dialogRef?.close();
  }
  clearValidation(): void {
    this.shiftForm.get('startTime')?.setErrors(null);
    this.shiftForm.get('endTime')?.setErrors(null);
    this.isDuplicateSchedule = false;
  }
  updateDayLabel(date: Date | null) {
    if (date) {
      this.currentDay = new Intl.DateTimeFormat('en-US', {
        weekday: 'long',
      }).format(date);
    } else {
      this.currentDay = '';
    }
  }
  onSubmit(): void {
    const startTime = this.shiftForm.get('startTime')?.value;
    const endTime = this.shiftForm.get('endTime')?.value;
    if (!startTime) {
      this.shiftForm.get('startTime')?.setErrors({ required: true });
    }
    if (!endTime) {
      this.shiftForm.get('endTime')?.setErrors({ required: true });
    }
    if (this.shiftForm.valid) {
      const formData = this.shiftForm.value;
      const selectedDate = formData.dayWiseDate;
      const selectedUser = formData.user;

      const newStartTime = this.combineDateAndTime(
        selectedDate,
        formData.startTime
      ).getTime();
      const newEndTime = this.combineDateAndTime(
        selectedDate,
        formData.endTime
      ).getTime();

      if (newStartTime >= newEndTime) {
        this.shiftForm.get('endTime')?.setErrors({ invalidRange: true });
        return;
      }

      const isOverlapping = this.shiftSchedule.some((shift) => {
        const shiftDateMatch =
          new Date(shift.dayWiseDate).toDateString() ===
          new Date(selectedDate).toDateString();
        const shiftUserMatch = shift.user === selectedUser;
        const shiftStartTime = this.combineDateAndTime(
          shift.dayWiseDate,
          shift.startTime
        ).getTime();
        const shiftEndTime = this.combineDateAndTime(
          shift.dayWiseDate,
          shift.endTime
        ).getTime();
        return (
          shiftDateMatch &&
          shiftUserMatch &&
          newStartTime < shiftEndTime &&
          newEndTime > shiftStartTime
        );
      });

      if (isOverlapping) {
        this.scheduleFetchStatus = false;
        this.shiftForm.get('startTime')?.setErrors({ overlap: true });
        this.shiftForm.get('endTime')?.setErrors({ overlap: true });
        return;
      }
      const newShift = { ...formData, duration: this.duration };
      this.shiftSchedule = [...this.shiftSchedule, newShift];
      this.weekSchedules = [...this.shiftSchedule];
      this.shiftForm.get('startTime')?.setErrors(null);
      this.shiftForm.get('endTime')?.setErrors(null);
      const dialogRef = this.dialog.open(CalendarSaveDialogComponent, {
        panelClass: 'custom-dialog-container',
      });
    }
  }

  combineDateAndTime(date: string, time: string): Date {
    const [timePart, period] = time.split(' ');
    let [hours, minutes] = timePart.split(':').map(Number);

    if (period === 'PM' && hours !== 12) {
      hours += 12;
    } else if (period === 'AM' && hours === 12) {
      hours = 0; // Midnight case
    }

    const combinedDate = new Date(date);
    combinedDate.setHours(hours, minutes, 0, 0);

    return combinedDate;
  }

  getProjectInfo(dempoId: string): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/user-projects?dempo_id=${dempoId}`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.projectList = Array.isArray(data) ? data : [];
        this.getDefaultProjectInfo(dempoId);
      },
      error: (error) => console.error('Error fetching projects:', error),
    });
  }
  getDefaultProjectInfo(dempoId: string): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/default-projects?dempo_id=${dempoId}`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        console.log('default project:--', data);
        this.selectedProject =
          this.projectList.find(
            (project) => project?.projectId === data?.projectId
          ) || null;
      },
      error: (error) => console.error('Error fetching projects:', error),
    });
  }

  getAuthor(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/authors`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.userList = Array.isArray(data) ? data : [];
        if (this.userObj?.eppn && this.authenticatedUser?.interviewer) {
          this.getLoginUser(this.userObj.eppn);
        }
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
    if (this.shiftForm.valid) {
      const formData = this.shiftForm.value;
      const selectedDate = formData.dayWiseDate;
      const selectedUser = formData.user;

      const newStartTime = this.combineDateAndTime(
        selectedDate,
        formData.startTime
      ).getTime();
      const newEndTime = this.combineDateAndTime(
        selectedDate,
        formData.endTime
      ).getTime();

      if (newStartTime >= newEndTime) {
        this.shiftForm.get('endTime')?.setErrors({ invalidRange: true });
        return;
      }

      const isOverlapping = this.shiftSchedule.some((shift) => {
        const shiftDateMatch =
          new Date(shift.dayWiseDate).toDateString() ===
          new Date(selectedDate).toDateString();
        const shiftUserMatch = shift.user === selectedUser;
        const shiftStartTime = this.combineDateAndTime(
          shift.dayWiseDate,
          shift.startTime
        ).getTime();
        const shiftEndTime = this.combineDateAndTime(
          shift.dayWiseDate,
          shift.endTime
        ).getTime();
        return (
          shiftDateMatch &&
          shiftUserMatch &&
          newStartTime < shiftEndTime &&
          newEndTime > shiftStartTime
        );
      });

      if (isOverlapping) {
        this.scheduleFetchStatus = false;
        this.shiftForm.get('startTime')?.setErrors({ overlap: true });
        this.shiftForm.get('endTime')?.setErrors({ overlap: true });
        return;
      }
      const newShift = { ...formData, duration: this.duration };
      this.shiftSchedule = [...this.shiftSchedule, newShift];
      this.weekSchedules = [...this.shiftSchedule];
      this.shiftForm.get('startTime')?.setErrors(null);
      this.shiftForm.get('endTime')?.setErrors(null);
      const dialogRef = this.dialog.open(CalendarSaveDialogComponent, {
        panelClass: 'custom-dialog-container',
      });
    }
  }

  // Emit selected date
  emitSelectedDate(): void {}

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
    const start = this.shiftForm.get('startTime')?.value;
    const end = this.shiftForm.get('endTime')?.value;

    if (!start || !end) {
      this.duration = '0hr';
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

    const formattedMinutes = diffMinutes / 60;
    const total = diffHours + formattedMinutes;
    this.duration = `${parseFloat(String(total)) || 0} hr`;
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

  onResetShiftSchedule(): void {
    this.shiftForm.reset({
      user: null,
      projects: [],
      dayWiseDate: this.addedDate.value,
      startTime: '',
      endTime: '',
      comments: '',
    });
    this.selectedDate = new FormControl<Date | null>(null, Validators.required);
  }

  getBlockOutDates(): void {
    this.configurationService.getBlockOutDates().subscribe(
      (response) => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.blockOutDates = <IBlockOutDate[]>response.Subject;
          // const initialDate: Date | null =
          //   this.shiftForm.get('dayWiseDate')?.value;
          // if (initialDate && this.authenticatedUser?.interviewer) {
          //   this.validateBlockOutDate(new Date(initialDate));
          //   this.shiftForm.get('dayWiseDate')?.setErrors({ blocked: true });
          //   this.shiftForm.get('startTime')?.disable();
          //   this.shiftForm.get('endTime')?.disable();
          // } else {
          //   this.shiftForm.get('dayWiseDate')?.setErrors(null);
          //   this.shiftForm.get('startTime')?.enable();
          //   this.shiftForm.get('endTime')?.enable();
          // }
        }
      },
      (error) => {
        console.error('Error fetching block out dates:', error);
      }
    );
  }

  handleAddDate(date: Date): void {
    if (date && date instanceof Date && !isNaN(date.getTime())) {
      this.shiftForm.get('dayWiseDate')?.setValue(date, { emitEvent: false });
      const formattedDate = date.toISOString().split('T')[0];
      if (formattedDate !== this.lastCalledDate) {
        this.getScheduleList();
        this.lastCalledDate = formattedDate;
      }
    } else {
      console.error('Invalid Date:', date);
    }
  }
  handleUser(user: any): void {
    this.filterUser = user;
    this.getScheduleList();
  }
  handleProject(project: any): void {
    this.filterProject = project;
    this.getScheduleList();
  }
  getLoginUser(email: string): void {
    if (!email) {
      console.error('Email is required to fetch login author');
      return;
    }

    const params = new HttpParams().set('email', email);
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/user`;

    this.http.get(apiUrl, { params }).subscribe({
      next: (data: any) => {
        this.selectedUser =
          this.userList.find((user) => user?.userId === data?.userId) || null;
        this.userList = this.userList.filter(
          (user) => user.userId === this.selectedUser?.userId
        );
        if (this.selectedUser) {
          this.getScheduleList();
          this.getProjectInfo(this.selectedUser.dempoId);
        }
      },
      error: (error: any) => {
        console.error('Error fetching user info:', error);
      },
    });
  }
  saveSchedule(): void {
    const shiftScheduleList: any[] = [];
    const uniqueSet = new Set<string>();

    if (Array.isArray(this.shiftSchedule) && this.shiftSchedule.length > 0) {
      for (const shift of this.shiftSchedule) {
        if (!shift) continue;

        const date = new Date(shift.dayWiseDate);
        const scheduleDate =
          date.getFullYear() +
          '-' +
          String(date.getMonth() + 1).padStart(2, '0') +
          '-' +
          String(date.getDate()).padStart(2, '0');

        const obj = {
          dempoId: shift.user?.dempoId || null,
          scheduleDate: scheduleDate,
          projectId: shift.projects?.projectId || null,
          comments: shift.comments || '',
          startTime: shift.startTime || null,
          endTime: shift.endTime || null,
          entryby: this.authenticatedUser.netID || null,
        };

        // Create a unique key to check for duplicates
        const key = `${obj.dempoId}-${obj.scheduleDate}-${obj.projectId}-${obj.startTime}-${obj.endTime}`;

        // Check if the same schedule is already in shiftSchedule (avoid duplicate additions)
        if (
          !uniqueSet.has(key) &&
          !this.shiftSchedule.some(
            (s) =>
              s.user?.dempoId === obj.dempoId &&
              s.scheduleDate === obj.scheduleDate &&
              s.projects?.projectId === obj.projectId &&
              s.startTime === obj.startTime &&
              s.endTime === obj.endTime
          )
        ) {
          uniqueSet.add(key);
          shiftScheduleList.push(obj);
        }
      }
    }

    if (shiftScheduleList.length === 0) {
      console.warn('No new shifts to save.');
      return;
    }

    this.http
      .post(
        `${environment.DataAPIUrl}/api/userSchedules/save-schedule`,
        shiftScheduleList
      )
      .subscribe({
        next: (response) => {
          this.showToastMessage('Shift saved successfully!', 'success');
          this.getScheduleList(); // Fetch updated schedule list after saving
          this.shiftForm.reset();
          this.shiftForm.markAsPristine();
          this.shiftForm.markAsUntouched();
        },
        error: (error) => {
          console.error('Error saving shift:', error);
        },
      });
  }

  onUserSelectionChange(event: MatSelectChange): void {
    const selectedUser = event.value;
    if (this.selectedUser) {
      this.getProjectInfo(event.value.dempoId);
      // this.getScheduleList();
    }
  }
  getScheduleList(): void {
    let scheduleDate = this.shiftForm?.get('dayWiseDate')?.value || new Date();
    let formattedDate = new Date(scheduleDate).toISOString().split('T')[0]; // Converts to 'YYYY-MM-DD'

    this.filterProject = this.filterProject || { projectId: 0 };
    this.filterProject.projectId ||= 0;

    this.filterUser = this.filterUser || { dempoId: 0 };
    this.filterUser.dempoId ||= 0;

    let url = `${environment.DataAPIUrl}/api/userSchedules/schedule-list?project_id=${this.filterProject.projectId}&schedule_date=${formattedDate}`;
    if (this.selectedUser) {
      url += `&dempo_id=${this.selectedUser.dempoId}`;
    }
    if (this.filterUser) {
      url += `&dempo_id=${this.filterUser.dempoId}`;
    }

    this.http.get<any[]>(url).subscribe({
      next: (response) => {
        console.log('Schedule list retrieved successfully:', response);
        this.shiftSchedule = response;
      },
      error: (error) => {
        console.error('Error fetching schedule list:', error);
        this.shiftSchedule = [];
      },
    });
  }

  showToastMessage(message: string, type: string): void {
    let snackBarClass = 'success-snackbar';
    if (type === 'error') {
      snackBarClass = 'error-snackbar';
    }

    const horizontalPosition: MatSnackBarHorizontalPosition = 'end';
    const verticalPosition: MatSnackBarVerticalPosition = 'top';

    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: [snackBarClass],
      horizontalPosition: horizontalPosition,
      verticalPosition: verticalPosition,
    });
  }
}
