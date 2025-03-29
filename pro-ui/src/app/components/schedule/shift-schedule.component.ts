import { HttpClient, HttpParams } from '@angular/common/http';
import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
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
import { ScheduleService } from './schedule.service';

@Component({
  selector: 'app-shift-schedule',
  templateUrl: './shift-schedule.component.html',
  styleUrls: ['./shift-schedule.component.css'],
})
export class ShiftScheduleComponent implements OnInit {
  userList: any[] = [];
  selectedProjects: any[] = []; // Added to track selected projects
  shiftForm!: FormGroup;
  currentDay: string = '';
  duration: string = '0 hr';
  scheduleFetchStatus: boolean = false;
  selectedDate = new FormControl<Date | null>(new Date(), Validators.required);
  currentYear: number = new Date().getFullYear();
  currentMonth: number = new Date().getMonth() + 1;
  shiftSchedule: any[] = [];
  shiftSchedule1: any[] = [];
  weekSchedules: IWeekSchedules[] = []; // Data for Week View
  timeSlots: string[] = [
    '08:00 AM',
    '08:30 AM',
    '09:00 AM',
    '09:30 AM',
    '10:00 AM',
    '10:30 AM',
    '11:00 AM',
    '11:30 AM',
    '12:00 PM',
    '12:30 PM',
    '01:00 PM',
    '01:30 PM',
    '02:00 PM',
    '02:30 PM',
    '03:00 PM',
    '03:30 PM',
    '04:00 PM',
    '04:30 PM',
    '05:00 PM',
    '05:30 PM',
    '06:00 PM',
    '06:30 PM',
    '07:00 PM',
    '07:30 PM',
    '08:00 PM',
    '08:30 PM',
    '09:00 PM',
    '09:30 PM',
    '10:00 PM',
    '10:30 PM',
  ];
  endtimeSlots: string[] = [
    '08:30 AM',
    '09:00 AM',
    '09:30 AM',
    '10:00 AM',
    '10:30 AM',
    '11:00 AM',
    '11:30 AM',
    '12:00 PM',
    '12:30 PM',
    '01:00 PM',
    '01:30 PM',
    '02:00 PM',
    '02:30 PM',
    '03:00 PM',
    '03:30 PM',
    '04:00 PM',
    '04:30 PM',
    '05:00 PM',
    '05:30 PM',
    '06:00 PM',
    '06:30 PM',
    '07:00 PM',
    '07:30 PM',
    '08:00 PM',
    '08:30 PM',
    '09:00 PM',
    '09:30 PM',
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
  addedDate = new FormControl<Date | null>(new Date(), Validators.required);
  selectedProject: any = null;
  @Input() date!: FormControl;
  @Output() selectedUserChange = new EventEmitter<any>();
  filterUser: any = null;
  filterProject: any = null;
  @Output() selectedProjectChange = new EventEmitter<any>();
  private lastCalledDate: string | null = null; // Track last called date
  @Output() selectedDateRangeValue = new EventEmitter<any>();
  dateRange: any;
  tabValue: string = 'Day';
  selectedDayDate: Date | null = null;
  isModified: boolean = false;
  adminProjects: any[] = [];
  otherProjects: any[] = [];
  allProjects: any[] = [];
  changeDate: Date | null = null;
  homeUser: any = null;
  homeSelectedDate: Date | null = null;
  homeSelectedProject: any = null;

  constructor(
    private http: HttpClient,
    private dialogRef: MatDialogRef<ShifCalendarComponent>,
    private configurationService: ConfigurationService,
    private dialog: MatDialog,
    private authenticationService: AuthenticationService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private scheduleService: ScheduleService
  ) {
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
      }
    );
  }
  ngOnChanges(): void {
    // this.getScheduleList();
  }
  getBackgroundColor(time: string): string {
    return time.includes('AM') ? '#FFF5BF' : '#DDE0EF';
  }
  confirmatationClose(): void {
    if (this.isModified) {
      const dialogRef = this.dialog.open(ScheduleCloseDialogComponent, {
        panelClass: 'custom-dialog-container',
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          this.onClose();
        }
      });
    } else {
      this.onClose();
    }
  }
  onClose(): void {
    this.dialogRef.close();
  }

  ngOnInit(): void {
    this.getBlockOutDates();
    this.getAuthor('');
    if (this.selectedUser) {
      this.getProjectInfo(this.selectedUser.dempoId);
    } else {
      this.getProjectInfo('');
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
      if (this.authenticatedUser.admin) {
        this.isModified = true;
      }
      this.updateDuration();
      this.scheduleFetchStatus = this.shiftForm.valid;
      this.clearValidation();
      this.shiftForm.markAsPristine();
      this.shiftForm.markAsUntouched();
      this.shiftForm.updateValueAndValidity({ emitEvent: false });
    });

    this.shiftForm.get('dayWiseDate')?.valueChanges.subscribe((date) => {
      if (date) {
        this.updateDayLabel(date);
      }
    });

    this.shiftForm.get('startTime')?.valueChanges.subscribe(() => {
      this.isModified = true;
      this.clearValidation();
    });

    this.shiftForm.get('endTime')?.valueChanges.subscribe(() => {
      this.isModified = true;
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
    setTimeout(() => {
      this.loadScheduleData();
    }, 100);
  }
  loadScheduleData(): void {
    this.scheduleService.getSchedule().subscribe((data) => {
      if (data) {
        const selectedUser =
          this.userList.find((user) => user?.userId === data?.userid) || null;
        this.homeUser = selectedUser;
        console.log(" this.allProjects--->", this.allProjects);
        console.log(" data?.projectid--->", data?.projectid);
        const selectedProject =
          this.allProjects.find((p) => p?.projectId === data?.projectid) ||
          null;
        console.log("selectedProject--->",selectedProject);
        
        this.homeSelectedProject = selectedProject;
        this.homeSelectedDate = new Date(data.scheduledate); 
        setTimeout(() => {
          this.shiftForm.patchValue({
            user: selectedUser,
            projects: selectedProject,
            dayWiseDate: new Date(data.scheduledate),
            startTime: data.startTime,
            endTime: data.endTime,
            comments: data.comments,
          });
          this.cdr.detectChanges();
        }, 0);
      }
    });
  }

  onDateRangeReceived(dateRange: any): void {
    if (this.tabValue != 'Day') {
      this.dateRange = dateRange;
      console.log(' this.dateRange------>', this.dateRange);
      // this.getScheduleList();
    }
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
    if (this.shiftForm.valid) {
      const formData = this.shiftForm.value;
      const selectedDate = formData.dayWiseDate;
      console.log('selectedDate----------------', selectedDate);
      this.changeDate = new Date(selectedDate);
    }
    const storedSchedule = localStorage.getItem('shiftSchedule');
    if (!this.shiftSchedule || this.shiftSchedule.length === 0) {
      this.shiftSchedule = storedSchedule ? JSON.parse(storedSchedule) : [];
    }
    console.log('storedSchedule----------------', this.shiftSchedule);
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
      const isDuplicate = this.shiftSchedule?.some((shift) => {
        const shiftDateMatch =
          new Date(shift.dayWiseDate).toISOString().split('T')[0] ===
          new Date(selectedDate).toISOString().split('T')[0];
        const shiftUserMatch = shift.user.dempoId === selectedUser.dempoId;
        const shiftStartTime = this.combineDateAndTime(
          shift.dayWiseDate,
          shift.startTime
        ).getTime();
        const shiftEndTime = this.combineDateAndTime(
          shift.dayWiseDate,
          shift.endTime
        ).getTime();
        const isSameShift =
          shiftDateMatch &&
          shiftUserMatch &&
          shiftStartTime === newStartTime &&
          shiftEndTime === newEndTime;

        const isOverlapping =
          shiftDateMatch &&
          shiftUserMatch &&
          newStartTime < shiftEndTime &&
          newEndTime > shiftStartTime;
        return isSameShift || isOverlapping;
      });
      if (isDuplicate) {
        this.scheduleFetchStatus = false;
        this.shiftForm.get('startTime')?.setErrors({ duplicate: true });
        this.shiftForm.get('endTime')?.setErrors({ duplicate: true });
        return;
      }

      const newShift = { ...formData, duration: this.duration };
      this.shiftSchedule1 = this.shiftSchedule1
        ? [...this.shiftSchedule1, newShift]
        : [newShift];

      const formatDate = (date: any) => {
        if (typeof date === 'string') {
          return date;
        }
        return new Date(date).toISOString().split('T')[0];
      };

      const uniqueNewShifts = this.shiftSchedule1?.filter(
        (newShift) =>
          !this.shiftSchedule.some(
            (shift) =>
              formatDate(shift.dayWiseDate) ===
                formatDate(newShift.dayWiseDate) &&
              shift.startTime.trim().toLowerCase() ===
                newShift.startTime.trim().toLowerCase() &&
              shift.endTime.trim().toLowerCase() ===
                newShift.endTime.trim().toLowerCase() &&
              shift.user.dempoId === newShift.user.dempoId
          )
      );

      this.shiftSchedule = [...this.shiftSchedule, ...uniqueNewShifts];
      this.weekSchedules = [...this.shiftSchedule1];
      this.shiftForm.get('startTime')?.setErrors(null);
      this.shiftForm.get('endTime')?.setErrors(null);
      const dialogRef = this.dialog.open(CalendarSaveDialogComponent, {
        panelClass: 'custom-dialog-container',
      });
    }
    console.log('this.shiftSchedule---------', this.shiftSchedule);
    console.log('this.shiftSchedule1 --->', this.shiftSchedule1);
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
    if (this.selectedUser) {
      dempoId = this.selectedUser.dempoId;
    }
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/projects?dempo_id=${dempoId}`;

    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.allProjects = Array.isArray(data) ? data : [];
        this.adminProjects = this.allProjects.filter(
          (project: { projectType: number }) => project.projectType === 4
        );

        const uniqueProjects = new Map();
        this.allProjects.forEach((project: { projectId: number; projectType: number }) => {
          if (project.projectType === 2 && !uniqueProjects.has(project.projectId)) {
            uniqueProjects.set(project.projectId, project);
          }
        });

        this.otherProjects = Array.from(uniqueProjects.values());
        if (this.selectedUser) {
          let defaultProjectId = 0;
          for (let obj of this.allProjects) {
            if (obj.defualtProject && obj.defualtProject > 0) {
              defaultProjectId = obj.defualtProject;
              break;
            }
          }
          this.selectedProject =
            this.allProjects.find(
              (project: { projectId: number }) =>
                project.projectId === defaultProjectId
            ) || null;
            
        }
      },
      error: (error) => console.error('Error fetching projects:', error),
    });
  }

  getAuthor(userId: any): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/authors?user_id=${userId}`;
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

      const isOverlapping = this.shiftSchedule?.some((shift) => {
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
      this.shiftSchedule1 = [...this.shiftSchedule1, newShift];
      this.weekSchedules = [...this.shiftSchedule1];
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
    if (this.authenticatedUser?.admin) {
      this.shiftForm.reset({
        user: null,
        projects: [],
        dayWiseDate: this.addedDate.value,
        startTime: '',
        endTime: '',
        comments: '',
      });
      this.selectedDate = new FormControl<Date | null>(
        null,
        Validators.required
      );
    }
  }

  getBlockOutDates(): void {
    this.configurationService.getBlockOutDates().subscribe(
      (response) => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.blockOutDates = <IBlockOutDate[]>response.Subject;
        }
      },
      (error) => {
        console.error('Error fetching block out dates:', error);
      }
    );
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
        this.getScheduleList();
      },
      error: (error: any) => {
        console.error('Error fetching user info:', error);
      },
    });
  }

  saveSchedule(): void {
    const shiftScheduleList: any[] = [];

    const startTime = this.shiftForm.get('startTime')?.value;
    const endTime = this.shiftForm.get('endTime')?.value;

    if (!startTime) {
      this.shiftForm.get('startTime')?.setErrors({ required: true });
      this.showToastMessage('Start time required.', 'warning');
    }
    if (!endTime) {
      this.showToastMessage('End time required.', 'warning');
      this.shiftForm.get('endTime')?.setErrors({ required: true });
    }

    if (Array.isArray(this.shiftSchedule1) && this.shiftSchedule1.length > 0) {
      for (const shift of this.shiftSchedule1) {
        if (!shift) continue;

        const date = new Date(shift.dayWiseDate);
        const scheduleDate = `${date.getFullYear()}-${String(
          date.getMonth() + 1
        ).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;

        const obj = {
          dempoId: shift.user?.dempoId || null,
          scheduleDate,
          projectId: shift.projects?.projectID || null,
          comments: shift.comments || '',
          startTime: shift.startTime || null,
          endTime: shift.endTime || null,
          entryby: this.authenticatedUser.netID || null,
        };

        shiftScheduleList.push(obj);
      }
    }
    const uniqueScheduleList = Array.from(
      new Map(
        shiftScheduleList.map((item) => [
          `${item.dempoId}_${item.scheduleDate}_${item.startTime}_${item.endTime}`,
          item,
        ])
      ).values()
    );
    this.http
      .post(
        `${environment.DataAPIUrl}/api/userSchedules/save-schedule`,
        uniqueScheduleList
      )
      .subscribe({
        next: (res: any) => {
          this.showToastMessage(res.Message, 'success');
          this.getScheduleList();
          this.shiftSchedule1 = [];
          this.shiftSchedule = [];
        },
        error: (error) => {
          console.error('Error saving shifts:', error);
          this.shiftSchedule1 = [];
          this.shiftSchedule = [];
        },
      });
  }

  onUserSelectionChange(event: MatSelectChange): void {
    const selectedUser = event.value;
    if (this.selectedUser) {
      this.getProjectInfo(event.value.dempoId);
    }
  }
  getScheduleList(): void {
    // let formattedDate = '';
    // let startDateFormat = '';
    // let endDateFormat = '';
    // console.log('this.dateRange----------', this.dateRange);
    // // Handle filtering based on the tab value
    // if (this.tabValue === 'Day' && this.selectedDayDate) {
    //   // Single day selection
    //   startDateFormat = '';
    //   endDateFormat = '';
    //   const scheduleDate = new Date(this.selectedDayDate);
    //   if (!isNaN(scheduleDate.getTime())) {
    //     formattedDate = scheduleDate.toISOString().split('T')[0]; // YYYY-MM-DD
    //   } else {
    //     console.warn('Invalid scheduleDate:', scheduleDate);
    //   }
    // } else if (
    //   (this.tabValue === 'Week' || this.tabValue === 'Month') &&
    //   this.dateRange
    // ) {
    //   // Range selection
    //   formattedDate = '';
    //   startDateFormat = '';
    //   endDateFormat = '';
    //   console.log('this.dateRange----------', this.dateRange);
    //   const startDate = this.dateRange.start
    //     ? new Date(this.dateRange.start)
    //     : null;
    //   const endDate = this.dateRange.end ? new Date(this.dateRange.end) : null;
    //   if (startDate && !isNaN(startDate.getTime())) {
    //     startDateFormat = startDate.toISOString().split('T')[0]; // YYYY-MM-DD
    //   } else {
    //     console.warn('Invalid startDate:', startDate);
    //   }
    //   if (endDate && !isNaN(endDate.getTime())) {
    //     endDateFormat = endDate.toISOString().split('T')[0]; // YYYY-MM-DD
    //   } else {
    //     console.warn('Invalid endDate:', endDate);
    //   }
    // }
    // // Construct the API URL properly
    // let url = `${environment.DataAPIUrl}/api/userSchedules/schedule-list/${this.selectedDayDate}?tab_value=${this.tabValue}`;
    // if (this.filterProject) {
    //   url += `&project_id=${this.filterProject.projectId}`;
    // }
    // if (this.authenticatedUser?.admin && this.filterUser) {
    //   url += `&demId=${this.filterUser?.dempoId}`;
    // } else if (this.selectedUser && this.selectedUser?.dempoId) {
    //   url += `&demId=${this.selectedUser?.dempoId}`;
    // }
    // console.log('Final API URL:', url);
    // // Make the API call
    // this.http.get<any[]>(url).subscribe({
    //   next: (response) => {
    //     console.log('Schedule list retrieved successfully:', response);
    //     this.shiftSchedule = response;
    //   },
    //   error: (error) => {
    //     console.error('Error fetching schedule list:', error);
    //     this.shiftSchedule = [];
    //   },
    // });
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
  onTabValueReceived(tab: any): void {
    this.tabValue = tab;
    // this.getScheduleList();
  }
  onSeletedDayDate(day: any): void {
    if (this.tabValue == 'Day') {
      this.dateRange = null;
      this.selectedDayDate = day;
      // this.getScheduleList();
    }
  }

  handleDate(dateEvent: any) {
    console.log('dateEvent--------<>', dateEvent);
    const selectedDate = dateEvent?.value ? new Date(dateEvent.value) : null;
    if (selectedDate && !isNaN(selectedDate.getTime())) {
      setTimeout(() => {
        this.shiftForm.get('dayWiseDate')?.setValue(selectedDate);
      });
    } else {
      console.error('Invalid Date Selected:', dateEvent?.value);
    }
  }
  handleWeekDate(date: any) {
    console.log('date shift--------<>', date);
    const selectedDate = date ? new Date(date) : null;
    if (selectedDate && !isNaN(selectedDate.getTime())) {
      setTimeout(() => {
        this.shiftForm.get('dayWiseDate')?.setValue(selectedDate);
      });
    } else {
      console.error('Invalid Date Selected:', date);
    }
  }
  handleShiftScheduleChange(data: any[]): void {
    this.shiftSchedule = data;
    console.log('Received shift schedule:', this.shiftSchedule);
  }
}
