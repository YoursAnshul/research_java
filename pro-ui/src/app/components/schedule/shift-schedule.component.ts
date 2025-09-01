import { HttpClient, HttpParams } from '@angular/common/http';
import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  NgZone,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { environment } from '../../../environments/environment';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatTabChangeEvent } from '@angular/material/tabs';
import {
  IAuthenticatedUser,
  IBlockOutDate,
  IProjectMin,
  IRequest,
  ISchedule,
  IValidationMessage,
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
import { Utils } from '../../classes/utils';
import {
  distinctUntilChanged,
  filter,
  forkJoin,
  pairwise,
  startWith,
} from 'rxjs';
import { ConfirmationShiftDialogComponent } from '../delete-dialog/delete-shift-dialog/confirmation-shift-dialog.component';
import { RequestsService } from '../../services/requests/requests.service';
import { MonthlyBlockDate } from '../calendar/calendar-controls/monthly.block.out.dialog.component';
import { UsersService } from '../../services/users/users.service';
import { SchedulingLevelDialog } from '../calendar/calendar-controls/scheduling-lever-dialog';
import { CalendarComponent } from '../calendar/calendar.component';
import { GlobalsService } from '../../services/globals/globals.service';
import { UserSchedulesService } from '../../services/userSchedules/user-schedules.service';
import { User } from '../../models/data/user';
import { UserRole } from '../../models/presentation/enums';
import moment from 'moment';

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
  isScheduleUpdate: boolean = false;
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
  tab: any = null;
  isEdit: boolean = false;
  blockedTimeSlots: string[] = []; // Store blocked time slots for selected date
  isTabChange: boolean = false;
  isHomeRedirect: boolean = false;
  isClose: boolean = false;
  profileType: string = '';
  newRequest: IRequest = {
    invalidFields: [],
    decisionId: null,
    requestCodeId: null,
    interviewerEmpId: null,
    resourceTeamMemberId: null,
    requestId: 0,
    requestDate: new Date(),
    requestDetails: '',
    notes: '',
    modBy: '',
    entryBy: '',
    scheduleId: null,
  };
  dateOptionValue: number = 0;
  isDateBlockDate: boolean = false;
  private skipValidation = false;
  canEdit: boolean | undefined;
  validationMessages: IValidationMessage[] = [];
  validationMessagesExpanded: boolean = false;
  validationMessagesChecked: boolean = false;
  isEditAble = false;
  previousValue: any;
  schedulinglevel: number = 0;
  private previousStartTime: string | null = null;
  private previousEndTime: string | null = null;
  private previousDate: Date | null = null;
  private previousProjectName: string | null = null;
  isDateModified: boolean = false;
  previousShift: any = null;
  private previousDempoId: string | null = null;
  pId: number = 0;
  isDataLoaded: boolean = false;
  isClosed: boolean = false;
  private previousComments: string | null = null;
  isStartTimeChanged: boolean = false;
  isEndTimeChanged: boolean = false;
  coreHours: number = 0;
  invalidScheduleKeys: string[] = [];
  invalidWeeks: string[] = [];
  isChange: boolean = false;
  contextDate: Date = new Date();
  UserRoles: any = UserRole;

  constructor(
    private http: HttpClient,
    private dialogRef: MatDialogRef<ShifCalendarComponent>,
    private configurationService: ConfigurationService,
    private dialog: MatDialog,
    private authenticationService: AuthenticationService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private scheduleService: ScheduleService,
    private requestsService: RequestsService,
    private userService: UsersService,
    private globalsService: GlobalsService,
    private userScheduleService: UserSchedulesService,
    private zone: NgZone
  ) {
    localStorage.removeItem('resultDate');
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
      }
    );
  }

  ngOnChanges(): void { }
  getBackgroundColor(time: string): string {
    return time.includes('AM') ? '#FFF5BF' : '#DDE0EF';
  }
  confirmatationClose(): void {
    let shouldShowConfirmation = false;

    if (this.authenticatedUser.role == UserRole.Interviewer) {
      const startTimeChanged = this.shiftForm.get('startTime')?.dirty && this.shiftForm.get('startTime')?.value;
      const endTimeChanged = this.shiftForm.get('endTime')?.dirty && this.shiftForm.get('endTime')?.value;
      const commentsChanged = this.shiftForm.get('comments')?.dirty && this.shiftForm.get('comments')?.value?.trim() !== '';
      const userDate = this.shiftForm.get('dayWiseDate')?.dirty && this.shiftForm.get('dayWiseDate')?.value;
      shouldShowConfirmation = startTimeChanged || endTimeChanged || commentsChanged || userDate;
    } else if ((this.authenticatedUser.role == UserRole.Admin || this.authenticatedUser.role == UserRole.OutcomesIT) && this.profileType != 'user-profile') {
      const startTimeChanged = this.shiftForm.get('startTime')?.dirty && this.shiftForm.get('startTime')?.value;
      const endTimeChanged = this.shiftForm.get('endTime')?.dirty && this.shiftForm.get('endTime')?.value;
      const commentsChanged = this.shiftForm.get('comments')?.dirty && this.shiftForm.get('comments')?.value?.trim() !== '';
      const projectsChanged = this.shiftForm.get('projects')?.dirty && this.shiftForm.get('projects')?.value;
      const userChanged = this.shiftForm.get('user')?.dirty && this.shiftForm.get('user')?.value;
      const userDate = this.shiftForm.get('dayWiseDate')?.dirty && this.shiftForm.get('dayWiseDate')?.value;

      shouldShowConfirmation = startTimeChanged || endTimeChanged || commentsChanged || projectsChanged || userChanged || userDate;
    }
    else if ((this.authenticatedUser.role == UserRole.Admin || this.authenticatedUser.role == UserRole.OutcomesIT) && this.profileType == 'user-profile') {
      const startTimeChanged = this.shiftForm.get('startTime')?.dirty && this.shiftForm.get('startTime')?.value;
      const endTimeChanged = this.shiftForm.get('endTime')?.dirty && this.shiftForm.get('endTime')?.value;
      const commentsChanged = this.shiftForm.get('comments')?.dirty && this.shiftForm.get('comments')?.value?.trim() !== '';
      // const projectsChanged = this.shiftForm.get('projects')?.dirty && this.shiftForm.get('projects')?.value;
      const userDate = this.shiftForm.get('dayWiseDate')?.dirty && this.shiftForm.get('dayWiseDate')?.value;

      shouldShowConfirmation = startTimeChanged || endTimeChanged || commentsChanged || userDate;
    }

    if (shouldShowConfirmation) {
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
    localStorage.removeItem('resultDate');
    this.isClosed = true;
    this.dialogRef.close();
  }
  ngOnInit(): void {
    this.isDataLoaded = false;
    if (this.authenticatedUser.role == UserRole.Interviewer) {
      this.getBlockOutDates();
    }
    this.scheduleService.getSchedule().subscribe((data) => {
      if (data) {
        this.isHomeRedirect = data.isHomeRedirect;
        this.pId = data.preschedulekey;
      }
    });
    if (!this.isHomeRedirect) this.tab = 'Month';
    this.scheduleService.getType().subscribe((type) => {
      if (type) {
        this.profileType = type;
      }
    });

    this.shiftForm = new FormGroup({
      user: new FormControl(null, Validators.required),
      projects: new FormControl([], Validators.required),
      dayWiseDate: new FormControl(new Date(), Validators.required),
      startTime: new FormControl(null, Validators.required),
      endTime: new FormControl(null, Validators.required),
      comments: new FormControl(''),
      id: new FormControl(null),
    });
    const userId = '';
    const dempoId = this.selectedUser?.dempoId || '';
    forkJoin([
      this.http.get(
        `${environment.DataAPIUrl}/manage-announement/authors?user_id=${userId}`
      ),
      this.http.get(
        `${environment.DataAPIUrl}/manage-announement/projects?dempo_id=${dempoId}`
      ),
    ]).subscribe({
      next: ([userData, projectData]: any[]) => {
        this.userList = Array.isArray(userData) ? userData : [];
        this.allProjects = Array.isArray(projectData) ? projectData : [];
        this.adminProjects = this.allProjects.filter(
          (p) => p.projectType === 4
        );

        const uniqueProjects = new Map();
        this.allProjects.forEach((p) => {
          if (p.projectType !== 4 && !uniqueProjects.has(p.projectId)) {
            uniqueProjects.set(p.projectId, p);
          }
        });
        this.otherProjects = Array.from(uniqueProjects.values());
        this.authenticationService.authenticatedUser.subscribe(
          (authenticatedUser) => {
            this.authenticatedUser = authenticatedUser;
            this.userObj = this.authenticatedUser;
          }
        );
        if (this.userObj?.eppn && this.authenticatedUser?.role == UserRole.Interviewer) {
          this.getLoginUser(this.userObj.eppn);
        }
        if (this.selectedUser) {
          const defProjectId = this.allProjects.find(
            (p) => p.defualtProject > 0
          )?.defualtProject;
          this.selectedProject =
            this.allProjects.find((p) => p.projectId === defProjectId) || null;
        }
        this.loadScheduleData();
        this.loadUserData();
      },
      error: (error) => {
        console.error('Error loading authors/projects:', error);
      },
    });

    this.shiftForm.valueChanges.subscribe(() => {
      this.updateDuration();
      this.scheduleFetchStatus = this.shiftForm.valid;
    });

    // this.shiftForm.get('dayWiseDate')?.valueChanges.subscribe((date) => {
    //   if (
    //     date &&
    //     this.shiftForm.valid &&
    //     this.previousDate?.toString() !== new Date(date).toString()
    //   ) {
    //     this.isModified = true;
    //     this.isDateModified = true;
    //     this.isEditAble = this.shiftForm.dirty;
    //   } else {
    //     this.isModified = false;
    //     this.isDateModified = false;
    //   }
    //   this.previousDate = new Date(date);
    //   if (this.authenticatedUser?.role == UserRole.Interviewer) {
    //     this.validateBlockOutDate(date);
    //     if (!this.isEdit) {
    //       this.validateDateOption(this.shiftForm.get('dayWiseDate')?.value);
    //     }
    //   }
    //   this.updateDayLabel(date);
    // });

    this.shiftForm
      .get('dayWiseDate')
      ?.valueChanges.pipe(
        distinctUntilChanged(
          (prev, curr) =>
            new Date(prev).toString() === new Date(curr).toString()
        )
      )
      .subscribe((date) => {
        if (this.authenticatedUser.role == UserRole.Interviewer) {
          this.isChange = true;
        }
        if (
          date &&
          this.shiftForm.valid &&
          this.previousDate?.toString() !== new Date(date).toString()
        ) {
          this.isModified = true;
          this.isDateModified = true;
          this.isEditAble = this.shiftForm.dirty;
        } else {
          this.isModified = false;
          this.isDateModified = false;
        }

        this.previousDate = new Date(date);
        if (this.authenticatedUser?.role == UserRole.Interviewer) {
          this.validateBlockOutDate(date);
          if (this.canEdit == null || this.canEdit === false) {
            this.validateDateOption(this.shiftForm.get('dayWiseDate')?.value);
          }
        }

        this.updateDayLabel(date);

        // Trigger change detection to re-evaluate time blocking rules
        // This ensures Saturday and Sunday time restrictions are applied when date changes
        this.cdr.detectChanges();
      });

    this.shiftForm.get('startTime')?.valueChanges.subscribe((startTime) => {
      if (
        startTime &&
        this.previousStartTime &&
        (this.previousStartTime !== startTime || this.isDateModified)
      ) {
        this.isStartTimeChanged = true;
        this.isModified = true;
        this.isEditAble = this.shiftForm.dirty;
      } else {
        this.isModified = false;
      }
      this.previousStartTime = startTime;
      // this.profileType = '';
      this.validateTimeRange();
      // this.clearValidation();
    });

    this.shiftForm.get('endTime')?.valueChanges.subscribe((endTime) => {
      if (
        endTime &&
        this.previousEndTime &&
        (this.previousEndTime !== endTime || this.isDateModified)
      ) {
        this.isEndTimeChanged = true;
        this.isModified = true;
        this.isEditAble = this.shiftForm.dirty;
      } else {
        this.isModified = false;
      }
      this.previousEndTime = endTime;
      // this.profileType = '';
      this.validateTimeRange();
      // this.clearValidation();
    });

    this.shiftForm.get('user')?.valueChanges.subscribe((user) => {
      if (
        this.authenticatedUser?.role !== UserRole.Interviewer &&
        !this.isHomeRedirect &&
        this.profileType != 'user-profile'
      ) {
        if (
          user &&
          this.shiftForm.valid &&
          this.previousDempoId !== user.dempoId
        ) {
          this.isModified = true;
          this.isEditAble = this.shiftForm.dirty;
        } else {
          this.isModified = false;
        }
        this.previousDempoId = user.dempoId;
      }
    });
    this.shiftForm.get('projects')?.valueChanges.subscribe((project) => {
      if (
        !this.skipValidation &&
        (this.authenticatedUser?.role == UserRole.Interviewer ||
          this.isHomeRedirect ||
          this.profileType == 'user-profile')
      ) {
        this.skipValidation = true;
        this.previousProjectName = project.projectName;
        return;
      }
      if (
        project &&
        this.shiftForm.valid &&
        this.previousProjectName !== project.projectName
      ) {
        this.isModified = true;
        this.isEditAble = this.shiftForm.dirty;
      } else {
        this.isModified = false;
      }
      this.previousProjectName = project.projectName;
    });
    this.shiftForm.get('comments')?.valueChanges.subscribe((comment) => {
      if (
        comment &&
        comment.trim() !== '' &&
        this.previousComments !== comment
      ) {
        this.isModified = true;
        this.isEditAble = this.shiftForm.dirty;
      }
      this.previousComments = comment;
    });

    this.currentDay = new Intl.DateTimeFormat('en-US', {
      weekday: 'long',
    }).format(new Date());
    setTimeout(() => {
      this.isDataLoaded = true;
    }, 1000);

    //subscribe to validation messages
    this.userScheduleService.userValidationMessages.subscribe((messages) => {
      this.validationMessages = messages;
    });

    //subscribe to invalid schedules
    this.userScheduleService.invalidSchedulesKeys.subscribe((scheduleKeys) => {
      this.invalidScheduleKeys = scheduleKeys;
    });
  }
  loadUserData(): void {
    this.scheduleService.getUser().subscribe((data) => {
      if (data) {
        const selectedUser =
          this.userList.find((user) => user?.userId === data?.userid) || null;
          
        this.homeUser = selectedUser;
        this.selectedUser = this.homeUser;
        if (this.selectedUser) {
          this.getProjectInfo(this.selectedUser.dempoId);
        }
        console.log('allProjects------->', this.allProjects);

        const selectedProject =
          this.allProjects.find((p) => p?.projectId === data?.defaultproject) ||
          null;

        this.homeSelectedProject = selectedProject;
        this.selectedProject = selectedProject;
        this.shiftForm.patchValue(
          {
            user: selectedUser,
            projects: this.selectedProject,
          },
          { emitEvent: false }
        );
        this.cdr.detectChanges();
      }
    });
    this.scheduleService.getTab().subscribe((tab) => {
      if (tab) {
        this.tab = tab;
      }
    });
  }
  loadScheduleData(): void {
    this.scheduleService.getSchedule().subscribe((data) => {
      if (data) {
        this.tab = data.tab;
        this.isHomeRedirect = data.isHomeRedirect;
        this.pId = data.preschedulekey;
        if (
          (this.tab == 'Week' || this.tab == 'Month') &&
          !this.isHomeRedirect
        ) {
          this.isEdit = true;
        }
        const selectedUser =
          this.userList.find((user) => user?.userId === data?.userid) || null;
        this.homeUser = selectedUser;
        this.selectedUser = this.homeUser;
        let selectedProject =
          this.allProjects.find((p) => p?.projectId === data?.projectid) ||
          null;
        this.homeSelectedProject = selectedProject;
        this.selectedProject = selectedProject;

        this.homeSelectedDate = this.convertToLocalDate(data.scheduledate);
        this.shiftForm.patchValue({
          user: selectedUser,
          projects: selectedProject,
          dayWiseDate: this.convertToLocalDate(data.scheduledate),
          startTime: this.convertTo12HourFormat(data.startTime),
          endTime: this.convertTo12HourFormat(data.endTime),
          comments: data.comments,
        });
        this.cdr.detectChanges();
      }
    });
  }

  convertToLocalDate(dateInput: string | Date): Date {
    if (typeof dateInput === 'string') {
      const [year, month, day] = dateInput.split('-').map(Number);
      return new Date(year, month - 1, day);
    }
    return dateInput;
  }

  onDateRangeReceived(dateRange: any): void {
    this.tryValidateSchedules();
    if (this.tabValue != 'Day') {
      this.dateRange = dateRange;
      this.contextDate = dateRange.startDate;
    }
  }
  validateDateOption(selectedDate: any): void {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const year = today.getFullYear();
    const month = today.getMonth();

    const resultDate = new Date(year, month, this.dateOptionValue);
    resultDate.setHours(0, 0, 0, 0);
    selectedDate.setHours(0, 0, 0, 0);
    this.isDateBlockDate = false;

    if (selectedDate.getFullYear() !== today.getFullYear()) {
      this.shiftForm.get('startTime')?.enable();
      this.shiftForm.get('endTime')?.enable();
      this.shiftForm.get('dayWiseDate')?.setErrors(null);
      return;
    }

    if (selectedDate.getMonth() < today.getMonth()) {
      this.shiftForm.get('startTime')?.enable();
      this.shiftForm.get('endTime')?.enable();
      this.shiftForm.get('dayWiseDate')?.setErrors(null);
      return;
    }
    if (resultDate >= today) {
      let monthVal = selectedDate.getMonth();
      let resultMonth = resultDate.getMonth();
      if (resultMonth > monthVal) {
        if (this.isChange) {
          Promise.resolve().then(() => this.openMonthlyBlockDialog());
        }
        this.shiftForm.get('dayWiseDate')?.setErrors({ required: true });
        this.shiftForm.get('startTime')?.disable();
        this.shiftForm.get('endTime')?.disable();
        this.isDateBlockDate = true;
        return;
      }
    } else {
      let monthVal = selectedDate.getMonth();
      let resultMonth = resultDate.getMonth();
      // localStorage.setItem('resultDate', resultDate.toLocaleDateString());
      if (resultMonth === monthVal || resultMonth + 1 >= monthVal) {
        if (this.isChange) {
          Promise.resolve().then(() => this.openMonthlyBlockDialog());
        }
        this.shiftForm.get('dayWiseDate')?.setErrors({ required: true });
        this.shiftForm.get('startTime')?.disable();
        this.shiftForm.get('endTime')?.disable();
        this.isDateBlockDate = true;
        return;
      }
    }
    this.shiftForm.get('startTime')?.enable();
    this.shiftForm.get('endTime')?.enable();
    this.shiftForm.get('dayWiseDate')?.setErrors(null);
  }

  validateBlockOutDate(selectedDate: any): void {
    this.blockedTimeSlots = [];
    this.isBlockDate = false;
    if (!this.blockOutDates || this.blockOutDates.length === 0) {
      console.log('Block out dates not loaded yet.');
      this.shiftForm.get('startTime')?.enable();
      this.shiftForm.get('endTime')?.enable();
      this.blockedTimeSlots = [];
      return;
    }

    const selectedDateOnly = new Date(
      selectedDate.getFullYear(),
      selectedDate.getMonth(),
      selectedDate.getDate()
    );

    const blockedEntries = this.blockOutDates.filter((blockOut) => {
      const blockOutDate = new Date(blockOut.blockOutDay!);

      const blockOutDateOnly = new Date(
        blockOutDate.getFullYear(),
        blockOutDate.getMonth(),
        blockOutDate.getDate()
      );
      return blockOutDateOnly.getTime() === selectedDateOnly.getTime();
    });
    if (blockedEntries.length === 0) {
      this.shiftForm.get('startTime')?.enable();
      this.shiftForm.get('endTime')?.enable();
      this.blockedTimeSlots = [];
      return;
    }

    const hasTimeBlock = blockedEntries.some(
      (blockOut) => blockOut.startTime && blockOut.endTime
    );
    if (!hasTimeBlock) {
      this.shiftForm.get('dayWiseDate')?.setErrors({ dateBlocked: true });
      this.shiftForm.get('startTime')?.disable();
      this.shiftForm.get('endTime')?.disable();
      this.blockedTimeSlots = [];
      this.isBlockDate = true;
      this.openBlockDialog(false); // date-only block
      return;
    }

    this.blockedTimeSlots = [];
    blockedEntries.forEach((blockOut) => {
      if (blockOut.startTime && blockOut.endTime) {
        const normalizedStart = this.removeLeadingZero(blockOut.startTime);
        const normalizedEnd = this.removeLeadingZero(blockOut.endTime);
        this.blockedTimeSlots.push(
          ...this.generateBlockedTimeSlots(normalizedStart, normalizedEnd)
        );
      }
    });
    this.shiftForm.get('startTime')?.enable();
    this.shiftForm.get('endTime')?.enable();
    this.shiftForm.get('dayWiseDate')?.enable();
    this.shiftForm.get('dayWiseDate')?.setErrors(null);
  }
  removeLeadingZero(time: string): string {
    return time.replace(/^0(\d)/, '$1');
  }

  generateBlockedTimeSlots(startTime: string, endTime: string): string[] {
    const blockedTimes: string[] = [];

    const startIndex = this.timeSlots.indexOf(startTime);
    const endIndex = this.timeSlots.indexOf(endTime);

    if (startIndex === -1 || endIndex === -1) {
      console.warn(`Time not found in timeSlots: startTime=${startTime}, endTime=${endTime}`);
      return blockedTimes;
    }

    for (let i = startIndex; i <= endIndex; i++) {
      blockedTimes.push(this.timeSlots[i]);
    }

    return blockedTimes;
  }

  private isWeekday(day: string): boolean {
    return ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'].includes(day);
  }

  private isBeforeOnePM(time: string): boolean {
    const hour = parseInt(time.split(':')[0]);
    return time.includes('AM') || (time.includes('PM') && hour === 12);
  }

  isStartTimeBlocked(time: string): boolean {

    if (this.authenticatedUser.role == UserRole.Interviewer && this.schedulinglevel && this.schedulinglevel == 1) {
      if (this.currentDay === 'Saturday') {
        const saturdayStartBlocked = ['8:00 AM', '8:30 AM'];
        if (saturdayStartBlocked.includes(time)) {
          return true;
        }
      }

      if (this.currentDay === 'Sunday') {
        const sundayStartBlocked = [
          '8:00 AM', '8:30 AM', '9:00 AM', '9:30 AM',
          '10:00 AM', '10:30 AM', '11:00 AM', '11:30 AM'
        ];
        if (sundayStartBlocked.includes(time)) {
          return true;
        }
      }
    }
    // Check if user is level 1 interviewer (role 3) and it's a weekday before 1 PM
    if (
      this.authenticatedUser?.role === UserRole.Interviewer &&
      this.schedulinglevel === 1 &&
      this.isWeekday(this.currentDay) &&
      this.isBeforeOnePM(time)
    ) {
      return true;
    }
    return this.blockedTimeSlots.includes(time);

  }

  isEndTimeBlocked(time: string): boolean {

    if (this.authenticatedUser.role == UserRole.Interviewer && this.schedulinglevel && this.schedulinglevel == 1) {
      if (this.currentDay === 'Saturday') {
        const saturdayEndBlocked = ['8:00 AM', '8:30 AM', '9:00 AM'];
        if (saturdayEndBlocked.includes(time)) {
          return true;
        }
      }

      if (this.currentDay === 'Sunday') {
        const sundayEndBlocked = [
          '8:00 AM', '8:30 AM', '9:00 AM', '9:30 AM',
          '10:00 AM', '10:30 AM', '11:00 AM', '11:30 AM', '12:00 PM'
        ];
        if (sundayEndBlocked.includes(time)) {
          return true;
        }
      }
    }
    // Check if user is level 1 interviewer (role 3) and it's a weekday before 1 PM
    if (
      this.authenticatedUser?.role === UserRole.Interviewer &&
      this.schedulinglevel === 1 &&
      this.isWeekday(this.currentDay) &&
      this.isBeforeOnePM(time)
    ) {
      return true;
    }
    return this.blockedTimeSlots.includes(time);
  }

  openBlockDialog(isTimeSlot: boolean): void {
    const existingDialog = this.dialog.openDialogs.find(
      (dialog) => dialog.componentInstance instanceof BlockdateDialog
    );

    if (existingDialog) {
      return;
    }

    const dialogRef = this.dialog.open(BlockdateDialog, {
      panelClass: 'custom-dialog-container',
      data: { isTimeSlot },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.closeDialog();
      }
    });
  }

  openMonthlyBlockDialog(): void {
    const existingDialog = this.dialog.openDialogs.find(
      (dialog) => dialog.componentInstance instanceof MonthlyBlockDate
    );

    if (existingDialog) return;

    this.zone.run(() => {
      const dialogRef = this.dialog.open(MonthlyBlockDate, {
        panelClass: 'custom-dialog-container',
        disableClose: true,
        autoFocus: false,
      });
      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          this.closeDialog();
        }
      });
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
  private getWeekStart(date: Date): Date {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1); // adjust when Sunday
    return new Date(d.setDate(diff));
  }

  private getWeekEnd(date: Date): Date {
    const start = this.getWeekStart(date);
    return new Date(start.getFullYear(), start.getMonth(), start.getDate() + 6);
  }

  private isEvenWeek(date: Date): boolean {
    const firstJan = new Date(date.getFullYear(), 0, 1);
    const days = Math.floor((+date - +firstJan) / (24 * 60 * 60 * 1000));
    return Math.floor(days / 7) % 2 === 0;
  }
  onSubmit(): void {
    
    if (this.shiftForm.valid) {
      const formData = this.shiftForm.value;
      const selectedDate = formData.dayWiseDate;
      this.changeDate = new Date(selectedDate);

      this.tryValidateSchedules();
    }
    const storedSchedule = localStorage.getItem('shiftSchedule');
    if (!this.shiftSchedule || this.shiftSchedule.length === 0) {
      this.shiftSchedule = storedSchedule ? JSON.parse(storedSchedule) : [];
    }
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
        ? [...this.shiftSchedule1, { ...newShift, isNew: true }]
        : [{ ...newShift, isNew: true }];
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
      if (!this.isEdit) {
        this.saveSchedule();
      } else {
        this.editSchedule();
      }

      this.tryValidateSchedules();
    }
  }
  saveNewRequest(id: number): void {
    if (
      !(
        this.selectedProject.projectName == 'Sick' ||
        this.selectedProject.projectName == 'Absent' ||
        this.selectedProject.projectName == 'Arriving Late' ||
        this.selectedProject.projectName == 'Leaving Early'
      )
    ) {
      return;
    }
    let requestCodeIdValue = 0;
    let requestTypeValue = '';
    if (this.selectedProject.projectName == 'Sick') {
      requestCodeIdValue = 3;
      requestTypeValue = 'Unexcused Absence-Sick';
    } else if (this.selectedProject.projectName == 'Absent') {
      requestCodeIdValue = 4;
      requestTypeValue = 'Unexcused Absence-Other';
    } else if (this.selectedProject.projectName == 'Arriving Late') {
      requestCodeIdValue = 7;
      requestTypeValue = 'Tardy-Arriving Late';
    } else if (this.selectedProject.projectName == 'Leaving Early') {
      requestCodeIdValue = 8;
      requestTypeValue = 'Tardy-Leaving Early';
    }

    const selectedDate: Date = this.shiftForm.value.dayWiseDate;
    const formattedDate = this.formatDateForRequest(selectedDate);
    const requestDetailsValue = `${this.selectedProject.projectName}: ${formattedDate}: ${this.shiftForm.value.startTime}-${this.shiftForm.value.endTime}`;
    this.newRequest = {
      invalidFields: [],
      decisionId: 1,
      requestCodeId: requestCodeIdValue,
      interviewerEmpId: this.selectedUser.dempoId,
      resourceTeamMemberId: this.selectedUser.dempoId,
      resourceTeamMemberName: this.selectedUser.userName,
      requestId: 0,
      requestDate: new Date(),
      requestDetails: requestDetailsValue,
      notes: '',
      modBy: this.authenticatedUser.netID,
      entryBy: this.authenticatedUser.netID,
      changed: false,
      entryDt: new Date(),
      decision: 'Schedule updated',
      requestType: requestTypeValue,
      invalid: false,
      modDt: new Date(),
      scheduleId: id,
    };
    this.requestsService.saveRequests([this.newRequest]).subscribe(
      (response) => {
        if (response.Status == 'Success') {
          this.newRequest = {
            invalidFields: [],
            decisionId: null,
            requestCodeId: null,
            interviewerEmpId: null,
            resourceTeamMemberId: null,
            requestId: 0,
            requestDate: new Date(),
            requestDetails: '',
            notes: '',
            modBy: '',
            entryBy: '',
            scheduleId: null,
          };
        } else {
        }
      },
      (error) => { }
    );
  }
  updateNewRequest(id: number): void {
    if (
      !(
        this.selectedProject.projectName == 'Sick' ||
        this.selectedProject.projectName == 'Absent' ||
        this.selectedProject.projectName == 'Arriving Late' ||
        this.selectedProject.projectName == 'Leaving Early'
      )
    ) {
      return;
    }

    let requestCodeIdValue = 0;
    let requestTypeValue = '';
    if (this.selectedProject.projectName == 'Sick') {
      requestCodeIdValue = 3;
      requestTypeValue = 'Unexcused Absence-Sick';
    } else if (this.selectedProject.projectName == 'Absent') {
      requestCodeIdValue = 4;
      requestTypeValue = 'Unexcused Absence-Other';
    } else if (this.selectedProject.projectName == 'Arriving Late') {
      requestCodeIdValue = 7;
      requestTypeValue = 'Tardy-Arriving Late';
    } else if (this.selectedProject.projectName == 'Leaving Early') {
      requestCodeIdValue = 8;
      requestTypeValue = 'Tardy-Leaving Early';
    }

    const selectedDate: Date = this.shiftForm.value.dayWiseDate;
    const formattedDate = this.formatDateForRequest(selectedDate);
    const requestDetailsValue = `${this.selectedProject.projectName}: ${formattedDate}: ${this.shiftForm.value.startTime}-${this.shiftForm.value.endTime}`;
    this.newRequest = {
      invalidFields: [],
      decisionId: 1,
      requestCodeId: requestCodeIdValue,
      interviewerEmpId: this.selectedUser.dempoId,
      resourceTeamMemberId: this.selectedUser.dempoId,
      resourceTeamMemberName: this.selectedUser.userName,
      requestId: 0,
      requestDate: new Date(),
      requestDetails: requestDetailsValue,
      notes: '',
      modBy: this.authenticatedUser.netID,
      entryBy: this.authenticatedUser.netID,
      changed: false,
      entryDt: new Date(),
      decision: 'Schedule updated',
      requestType: requestTypeValue,
      invalid: false,
      modDt: new Date(),
      scheduleId: id,
    };
    this.requestsService.updateRequests([this.newRequest]).subscribe(
      (response) => {
        if (response.Status == 'Success') {
          this.newRequest = {
            invalidFields: [],
            decisionId: null,
            requestCodeId: null,
            interviewerEmpId: null,
            resourceTeamMemberId: null,
            requestId: 0,
            requestDate: new Date(),
            requestDetails: '',
            notes: '',
            modBy: '',
            entryBy: '',
            scheduleId: null,
          };
        } else {
        }
      },
      (error) => { }
    );
  }
  formatDateForRequest(date: Date): string {
    const options: Intl.DateTimeFormatOptions = {
      month: 'short',
      day: '2-digit',
      year: 'numeric',
    };
    const localeDate = date.toLocaleDateString('en-US', options);
    const [month, dayWithComma, year] = localeDate.split(' ');

    const day = dayWithComma.replace(',', '');
    return `${month}-${day}, ${year}`;
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
        this.allProjects.forEach(
          (project: { projectId: number; projectType: number }) => {
            if (
              project.projectType != 4 &&
              !uniqueProjects.has(project.projectId)
            ) {
              uniqueProjects.set(project.projectId, project);
            }
          }
        );

        this.otherProjects = Array.from(uniqueProjects.values());
        if (this.selectedProject) {
          this.selectedProject =
            this.allProjects.find(
              (project: { projectId: number }) =>
                project.projectId ===
                (this.selectedProject?.projectId ??
                  this.selectedProject?.projectId)
            ) || null;
        } else {
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
        }
      },
      error: (error) => console.error('Error fetching projects:', error),
    });
    this.previousShift = this.shiftForm.value;
  }
  getProjectInfoNew(dempoId: string, schedule: any): void {
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
        this.allProjects.forEach(
          (project: { projectId: number; projectType: number }) => {
            if (
              project.projectType != 4 &&
              !uniqueProjects.has(project.projectId)
            ) {
              uniqueProjects.set(project.projectId, project);
            }
          }
        );
        this.otherProjects = Array.from(uniqueProjects.values());

        this.selectedProject =
          this.allProjects.find(
            (project: { projectId: number }) =>
              project.projectId ===
              (schedule?.projects?.projectId ?? schedule?.projectId)
          ) || null;

        this.shiftForm.patchValue({
          user: this.selectedUser,
          projects: this.selectedProject,
          dayWiseDate: this.convertToLocalDate(schedule.dayWiseDate),
          startTime: this.convertTo12HourFormat(schedule.startTime),
          endTime: this.convertTo12HourFormat(schedule.endTime),
          comments: schedule.comments,
          id: schedule.preschedulekey,
        });
        this.isEditAble = false;
        console.log('this.shiftForm value --->', this.shiftForm.value);
        this.previousShift = this.shiftForm.value;
      },
      error: (error) => console.error('Error fetching projects:', error),
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
  emitSelectedDate(): void { }

  addDateUnitsToSelectedDate(unit: number): void {
    let selectedDt = new Date();
    if (this.selectedDate && this.selectedDate.value) {
      selectedDt = new Date(this.selectedDate.value);
    }
    selectedDt.setDate(selectedDt.getDate() + unit);
    this.selectedDate.setValue(selectedDt);
    this.contextDate = selectedDt;
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
    this.isEdit = false;
    this.isEditAble = false;
    if (
      (this.authenticatedUser?.role == UserRole.Admin || this.authenticatedUser?.role == UserRole.OutcomesIT) &&
      !this.isHomeRedirect &&
      this.profileType != 'user-profile'
    ) {
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
      this.contextDate = this.selectedDate.value || new Date();
      this.shiftForm.get('startTime')?.setErrors({ required: true });
      this.shiftForm.get('endTime')?.setErrors({ required: true });
    } else {
      this.shiftForm.patchValue({
        startTime: '',
        endTime: '',
        comments: '',
      });
      this.shiftForm.get('startTime')?.setErrors({ required: true });
      this.shiftForm.get('endTime')?.setErrors({ required: true });
      this.shiftForm.get('startTime')?.markAsTouched();
      this.shiftForm.get('endTime')?.markAsTouched();
      this.shiftForm.get('comments')?.markAsTouched();
    }
  }

  getBlockOutDates(): void {
    this.configurationService.getBlockOutDates().subscribe(
      (response) => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.blockOutDates = <IBlockOutDate[]>response.Subject;
          this.validateBlockOutDate(this.selectedDate.value);
        }
      },
      (error) => {
        console.error('Error fetching block out dates:', error);
      }
    );
  }

  handleUser(user: any): void {
    this.filterUser = user;
    this.selectedUser = user;
    this.tryValidateSchedules();
  }
  handleProject(project: any): void {
    this.filterProject = project;
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
          this.getProjectInfo(this.selectedUser.dempoId);
          this.userService
            .getUserByNetId(this.selectedUser.dempoId)
            .subscribe((response) => {
              this.canEdit = response?.Subject?.canedit;
              this.schedulinglevel = response?.Subject?.schedulinglevel;
              if (this.canEdit == null || this.canEdit === false) {
                this.getOptionValue();
              }
            });
        } else if ((this.authenticatedUser?.role == UserRole.Admin || this.authenticatedUser?.role == UserRole.OutcomesIT)) {
          this.getProjectInfo('');
        }
      },
      error: (error: any) => {
        console.error('Error fetching user info:', error);
      },
    });
  }

  saveSchedule(): void {
    this.isModified = false;
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
          projectId: shift.projects?.projectId || null,
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
          this.shiftSchedule1 = [];
          this.shiftSchedule = [];
          this.isScheduleUpdate = true;
          if (res.Message == 'Schedule already exists for this user!') {
            this.shiftForm.get('startTime')?.setErrors({ required: true });
            this.shiftForm.get('endTime')?.setErrors({ required: true });
          }

          this.saveNewRequest(res?.Subject?.preschedulekey);
          this.homeSelectedDate = this.shiftForm.get('dayWiseDate')?.value;
           if(this.authenticatedUser?.role == UserRole.Interviewer){
              this.onResetShiftSchedule();
            }
          localStorage.removeItem('shiftSchedule');
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
      this.userService
        .getUserByNetId(this.selectedUser.dempoId)
        .subscribe((response) => {
          this.schedulinglevel = response?.Subject?.schedulinglevel;
        });
      this.getProjectInfo(event.value.dempoId);
    }
  }
  // getScheduleList(): void {
  //   // let formattedDate = '';
  //   // let startDateFormat = '';
  //   // let endDateFormat = '';
  //   // console.log('this.dateRange----------', this.dateRange);
  //   // // Handle filtering based on the tab value
  //   // if (this.tabValue === 'Day' && this.selectedDayDate) {
  //   //   // Single day selection
  //   //   startDateFormat = '';
  //   //   endDateFormat = '';
  //   //   const scheduleDate = new Date(this.selectedDayDate);
  //   //   if (!isNaN(scheduleDate.getTime())) {
  //   //     formattedDate = scheduleDate.toISOString().split('T')[0]; // YYYY-MM-DD
  //   //   } else {
  //   //     console.warn('Invalid scheduleDate:', scheduleDate);
  //   //   }
  //   // } else if (
  //   //   (this.tabValue === 'Week' || this.tabValue === 'Month') &&
  //   //   this.dateRange
  //   // ) {
  //   //   // Range selection
  //   //   formattedDate = '';
  //   //   startDateFormat = '';
  //   //   endDateFormat = '';
  //   //   console.log('this.dateRange----------', this.dateRange);
  //   //   const startDate = this.dateRange.start
  //   //     ? new Date(this.dateRange.start)
  //   //     : null;
  //   //   const endDate = this.dateRange.end ? new Date(this.dateRange.end) : null;
  //   //   if (startDate && !isNaN(startDate.getTime())) {
  //   //     startDateFormat = startDate.toISOString().split('T')[0]; // YYYY-MM-DD
  //   //   } else {
  //   //     console.warn('Invalid startDate:', startDate);
  //   //   }
  //   //   if (endDate && !isNaN(endDate.getTime())) {
  //   //     endDateFormat = endDate.toISOString().split('T')[0]; // YYYY-MM-DD
  //   //   } else {
  //   //     console.warn('Invalid endDate:', endDate);
  //   //   }
  //   // }
  //   // // Construct the API URL properly
  //   // let url = `${environment.DataAPIUrl}/api/userSchedules/schedule-list/${this.selectedDayDate}?tab_value=${this.tabValue}`;
  //   // if (this.filterProject) {
  //   //   url += `&project_id=${this.filterProject.projectId}`;
  //   // }
  //   //   url += `&demId=${this.filterUser?.dempoId}`;
  //   // } else if (this.selectedUser && this.selectedUser?.dempoId) {
  //   //   url += `&demId=${this.selectedUser?.dempoId}`;
  //   // }
  //   // console.log('Final API URL:', url);
  //   // // Make the API call
  //   // this.http.get<any[]>(url).subscribe({
  //   //   next: (response) => {
  //   //     console.log('Schedule list retrieved successfully:', response);
  //   //     this.shiftSchedule = response;
  //   //   },
  //   //   error: (error) => {
  //   //     console.error('Error fetching schedule list:', error);
  //   //     this.shiftSchedule = [];
  //   //   },
  //   // });
  // }

  getScheduleList(anchorDate: string | null): void {
    this.shiftSchedule = [];
    this.shiftSchedule1 = [];
    let url = '';

    if (this.authenticatedUser?.role == UserRole.Interviewer && this.selectedUser?.dempoId) {
      url = `${environment.DataAPIUrl}/api/userSchedules/schedule-list/${anchorDate}?demId=${this.selectedUser?.dempoId}`;
    } else {
      if (this.authenticatedUser?.role !== UserRole.Interviewer) {
        url = `${environment.DataAPIUrl}/api/userSchedules/schedule-list/${anchorDate}`;
        if (this.selectedUser && this.selectedUser?.dempoId) {
          url += `?demId=${this.selectedUser?.dempoId}`;
        }
      }
    }

    this.http.get<any[]>(url).subscribe({
      next: (response) => {
        this.shiftSchedule = response ?? [];
        localStorage.setItem(
          'shiftSchedule',
          JSON.stringify(this.shiftSchedule)
        );

        // OPTIONAL: merge unsaved new shifts if needed
        const missingSchedules =
          this.shiftSchedule1?.filter(
            (item1) =>
              !this.shiftSchedule.some(
                (item2) =>
                  item1.startTime === item2.startTime &&
                  item1.endTime === item2.endTime &&
                  item1.duration === item2.duration
              )
          ) || [];

        this.shiftSchedule.push(...missingSchedules);

        // clear local temporary additions after successful fetch
        this.shiftSchedule1 = [];
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
  onTabValueReceived(tab: any): void {
    this.tabValue = tab;
    if (!this.isHomeRedirect) {
      this.isEdit = false;
    }
  }

  onSeletedDayDate(day: any): void {
    if (this.tabValue == 'Day') {
      this.dateRange = null;
      this.selectedDayDate = day;
      this.contextDate = day;
      // this.getScheduleList();

      this.tryValidateSchedules();
    }
  }

  handleDate(dateEvent: any) {
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
  editSchedule() {
    const startTime = this.shiftForm.get('startTime')?.value;
    const endTime = this.shiftForm.get('endTime')?.value;
    const dayWiseDate = this.shiftForm.get('dayWiseDate')?.value;

    if (!startTime) {
      this.shiftForm.get('startTime')?.setErrors({ required: true });
      this.showToastMessage('Start time required.', 'warning');
      return;
    }

    if (!endTime) {
      this.shiftForm.get('endTime')?.setErrors({ required: true });
      this.showToastMessage('End time required.', 'warning');
      return;
    }

    if (!dayWiseDate) {
      this.shiftForm.get('dayWiseDate')?.setErrors({ required: true });
      this.showToastMessage('Schedule date required.', 'warning');
      return;
    }

    const date = new Date(dayWiseDate);
    const scheduleDate = `${date.getFullYear()}-${String(
      date.getMonth() + 1
    ).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;

    const shift = this.shiftForm.value || {};
    const obj = {
      dempoId: shift.user?.dempoId || null,
      scheduleDate,
      projectId: shift.projects?.projectId || null,
      comments: shift.comments || '',
      startTime: startTime || null,
      endTime: endTime || null,
      entryby: this.authenticatedUser?.netID || null,
      id: shift.id || null,
    };

    this.http
      .post(`${environment.DataAPIUrl}/api/userSchedules/update-schedule`, obj)
      .subscribe({
        next: (res: any) => {
          this.showToastMessage(res.Message, 'success');
          this.scheduleFetchStatus = false;
          this.isEdit = false;
          this.isScheduleUpdate = true;
          if (this.selectedProject) {
            this.updateNewRequest(shift.id);
          }
          this.homeSelectedDate = this.shiftForm.get('dayWiseDate')?.value;
          this.onResetShiftSchedule();
          this.shiftSchedule = [];
          this.shiftSchedule1 = [];
          localStorage.removeItem('shiftSchedule');
        },
        error: (error) => {
          console.error('Error saving shifts:', error);
          this.showToastMessage('Failed to update schedule.', 'error');
          this.shiftSchedule1 = [];
          this.shiftSchedule = [];
        },
      });
  }

  handleSchedule(schedule: any) {
    const storedSchedule = localStorage.getItem('shiftSchedule');
    if (!this.shiftSchedule || this.shiftSchedule.length === 0) {
      this.shiftSchedule = storedSchedule ? JSON.parse(storedSchedule) : [];
    }
    if (schedule) {
      this.isEdit = schedule.isEdit;
      this.tab = schedule.tab;
      let scheduleDate = null;
      if (schedule.tab == 'Day') {
        const selectedUser =
          this.userList.find(
            (user) => user?.userId === schedule?.user?.userId
          ) || null;
        this.selectedUser = selectedUser;
        const selectedProject =
          this.allProjects.find(
            (p) => p?.projectId === schedule?.projects?.projectId
          ) || null;
        this.selectedProject = selectedProject;
        if (this.selectedUser) {
          this.getProjectInfoNew(this.selectedUser.dempoId, schedule);
        }
        const [year, month, day] = schedule.dayWiseDate.split('-').map(Number);
        scheduleDate = new Date(year, month - 1, day);
      } else {
        const selectedUser =
          this.userList.find((user) => user?.userId === schedule?.userid) ||
          null;
        this.selectedUser = selectedUser;
        if (this.selectedUser) {
          schedule.dayWiseDate = schedule.scheduledate;
          this.getProjectInfoNew(this.selectedUser.dempoId, schedule);
        }
        const selectedProject =
          this.allProjects.find((p) => p?.projectId === schedule.projectId) ||
          null;
        this.selectedProject = selectedProject;
        scheduleDate = new Date(schedule.scheduledate);
      }
      this.shiftForm.patchValue({
        user: this.selectedUser,
        projects: this.selectedProject,
        dayWiseDate: scheduleDate,
        startTime: this.convertTo12HourFormat(schedule.startTime),
        endTime: this.convertTo12HourFormat(schedule.endTime),
        comments: schedule.comments,
        id: schedule.preschedulekey,
      });
      this.isEditAble = false;
    }
    this.tryValidateSchedules();
  }

  deleteSchedule() {
    const shift = this.shiftForm.value || {};
    this.http
      .delete(
        `${environment.DataAPIUrl}/api/userSchedules/delete-schedule/${shift.id}/${this.authenticatedUser.netID}`
      )
      .subscribe({
        next: (res: any) => {
          this.showToastMessage(res.Message, 'success');
          this.scheduleFetchStatus = false;
          this.isEdit = false;
          this.isScheduleUpdate = true;
          this.onResetShiftSchedule();
          this.shiftSchedule = [];
          this.shiftSchedule1 = [];
          localStorage.removeItem('shiftSchedule');
        },
        error: (error) => {
          console.error('Error deleting schedule:', error);
          this.showToastMessage('Failed to delete schedule.', 'error');
        },
      });
  }
  convertTo12HourFormat(time24: string): string {
    const [hourStr, minute] = time24.split(':');
    let hour = parseInt(hourStr, 10);
    const ampm = hour >= 12;
    hour = hour % 12 || 12;
    return `${hour}:${minute}`;
  }
  validateTimeRange(): void {
    const startControl = this.shiftForm.get('startTime');
    const endControl = this.shiftForm.get('endTime');

    const startTime = startControl?.value;
    const endTime = endControl?.value;

    if (!startTime || !endTime) {
      startControl?.setErrors(null);
      endControl?.setErrors(null);
      return;
    }

    const normalizeTime = (time: string): string =>
      time
        .replace(/\s+/g, '')
        .replace(/(AM|PM)$/i, ' $1')
        .toUpperCase();

    const toMinutes = (time: string): number => {
      const [timePart, modifier] = time.split(' ');
      let [hours, minutes] = timePart.split(':').map(Number);

      if (modifier === 'PM' && hours !== 12) hours += 12;
      if (modifier === 'AM' && hours === 12) hours = 0;

      return hours * 60 + minutes;
    };

    const start = toMinutes(normalizeTime(startTime));
    const end = toMinutes(normalizeTime(endTime));

    if (end <= start) {
      startControl?.setErrors({ invalidRange: true });
      endControl?.setErrors({ invalidRange: true });
      startControl?.markAsTouched();
      endControl?.markAsTouched();
    } else {
      startControl?.setErrors(null);
      endControl?.setErrors(null);
    }
  }
  confirmationPopup(): void {
    const dialogRef = this.dialog.open(ConfirmationShiftDialogComponent, {
      panelClass: 'custom-dialog-container',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.deleteSchedule();
      }
    });
  }
  getOptionValue(): void {
    const apiUrl = `${environment.DataAPIUrl}/api/userSchedules/option-value`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.dateOptionValue = data?.Subject?.optionValue;
        if (this.dateOptionValue) {
          if (!this.isHomeRedirect) {
            const resultDate = new Date(
              new Date().getFullYear(),
              new Date().getMonth(),
              this.dateOptionValue
            );
            if (resultDate < new Date()) {
              let calculatedDate = new Date(
                resultDate.getFullYear(),
                resultDate.getMonth() + 2,
                1
              );
              this.shiftForm.get('dayWiseDate')?.setValue(calculatedDate);
            }
          }

          this.validateDateOption(this.shiftForm.get('dayWiseDate')?.value);
        }
      },
    });
  }

  coreHoursValidation(date: Date, dempoId: string): void {
    if (!date) {
      return;
    }
    const formattedDate = date.toISOString().split('T')[0];
    const apiUrl = `${environment.DataAPIUrl}/api/userSchedules/core-hours?scheduleDate=${formattedDate}&dempoId=${dempoId}`;

    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.coreHours = data?.Subject ?? 0;
      },
      error: (error) => {
        console.error('Error fetching core hours:', error);
      },
    });
  }

  getValidationMessages(): void {
    if (!this.selectedDate || !this.authenticatedUser) return;

    //get validation messages
    this.validationMessagesChecked = false;
    const selectedDateValue = this.selectedDate.value
      ? new Date(this.selectedDate.value)
      : new Date();
    this.userScheduleService
      .getUserValidationMessages(
        selectedDateValue,
        this.authenticatedUser.netID
      )
      .subscribe(
        (response) => {
          if ((response.Status || '').toUpperCase() == 'SUCCESS') {
            this.validationMessages = <IValidationMessage[]>response.Subject;

            for (var x = 0; x < this.validationMessages.length; x++) {
              if (this.validationMessages[x].schedules) {
                for (
                  var i = 0;
                  i < (this.validationMessages[x].schedules || []).length;
                  i++
                ) {
                  this.validationMessages[x].validationMessagesId = 0;
                  (this.validationMessages[x].schedules || [])[
                    i
                  ].startdatetime = new Date(
                    (this.validationMessages[x].schedules || [])[i]
                      .startdatetime || ''
                  );
                  (this.validationMessages[x].schedules || [])[i].enddatetime =
                    new Date(
                      (this.validationMessages[x].schedules || [])[i]
                        .enddatetime || ''
                    );
                  (this.validationMessages[x].schedules || [])[i].startTime =
                    Utils.formatDateToTimeString(
                      (this.validationMessages[x].schedules || [])[i]
                        .startdatetime,
                      true
                    ) || '';
                  (this.validationMessages[x].schedules || [])[i].endTime =
                    Utils.formatDateToTimeString(
                      (this.validationMessages[x].schedules || [])[i]
                        .enddatetime,
                      true
                    ) || '';

                  let schedule: ISchedule = (this.validationMessages[x]
                    .schedules || [])[i];

                  let startHours: number =
                    ((schedule.startdatetime || new Date()).getHours() * 60 +
                      (schedule.startdatetime || new Date()).getMinutes()) /
                    60;
                  let endHours: number =
                    ((schedule.enddatetime || new Date()).getHours() * 60 +
                      (schedule.enddatetime || new Date()).getMinutes()) /
                    60;
                  (this.validationMessages[x].schedules || [])[
                    i
                  ].scheduledHours = endHours - startHours;

                  if (this.allProjects) {
                    let projectId: number = (this.validationMessages[x]
                      .schedules || [])[i].projectid as number;
                    let scheduleProject: IProjectMin | undefined =
                      this.allProjects.find((x) => x.projectID == projectId);
                    if (scheduleProject) {
                      (this.validationMessages[x].schedules || [])[
                        i
                      ].projectName = scheduleProject.projectName;
                    }
                  }
                }

                const uniqueValue = (value: any, index: any, self: any) => {
                  return self.indexOf(value) === index;
                };

                this.validationMessages =
                  this.validationMessages.filter(uniqueValue);
              }
            }

            this.validationMessagesChecked = true;
          }
        },
        (error) => { }
      );
  }

  formatDateOnlyWithMonthNameToString(
    dateToFormat: Date | null
  ): string | null {
    return Utils.formatDateOnlyWithMonthNameToString(dateToFormat);
  }

  formatDateMonthNameToString(dateToFormat: Date | null): string | null {
    return Utils.formatDateMonthNameToString(dateToFormat);
  }

  expandValidationMessages(validationMessagesExpanded: boolean): void {
    this.validationMessagesExpanded = validationMessagesExpanded;
    Utils.schedulerPopupDynamicSize(validationMessagesExpanded);
  }

  splitByPipe(stringToSplit: string): string[] {
    if (!stringToSplit) {
      return [];
    }

    return stringToSplit.split('|');
  }

  displaySchedulingLevelInfo(event: any): void {
    let htmlMessage: string = '';

    if (!this.selectedUser) {
      this.selectedUser = {} as User;
      htmlMessage = 'No scheduling level assigned.';
    }

    if (this.selectedUser.schedulinglevel == 1) {
      htmlMessage = htmlMessage + '<p class="bold">Scheduling Level 1</p>';
      htmlMessage = htmlMessage + '<p><ul>';
      htmlMessage =
        htmlMessage +
        '<li>A shift schedule should be at least 4 hours in length.</li>';
      htmlMessage =
        htmlMessage +
        '<li>A shift schedule should be no more than 7 hours in length.</li>';
      htmlMessage =
        htmlMessage +
        '<li>A shift schedule for a weekday, Monday thru Friday, should begin at or after 1 PM.</li>';
      htmlMessage =
        htmlMessage +
        '<li>A shift schedule for Saturday should begin at or after 9 AM.</li>';
      htmlMessage =
        htmlMessage +
        '<li>A shift schedule for Sunday should begin at or after 12 noon.</li>';
      htmlMessage =
        htmlMessage +
        "<li>An Interviewer's weekly schedule should at a minimum match their core hours total.</li>";
      htmlMessage =
        htmlMessage +
        "<li>An Interviewer's weekly schedule should not exceed 20 hours total.</li>";
      htmlMessage =
        htmlMessage +
        "<li>An Interviewer's schedule should include 1 night shift, until at or after 9 PM, every other week.</li>";
      htmlMessage =
        htmlMessage +
        "<li>An Interviewer's schedule should include 1 weekend shift every other week.</li>";
      htmlMessage =
        htmlMessage +
        '<ul><li>A Friday night shift schedule with majority of hours after 5 PM, can only have 1 Friday night per month.</li>';
      htmlMessage =
        htmlMessage +
        '<li>A Saturday and/or Sunday shift schedule should be 6 hours minimum.</li></ul>';
      htmlMessage = htmlMessage + '</ul></p>';
    }

    if (this.selectedUser.schedulinglevel == 2) {
      htmlMessage = htmlMessage + '<p class="bold">Scheduling Level 2</p>';
      htmlMessage = htmlMessage + '<p><ul>';
      htmlMessage =
        htmlMessage +
        '<li>A shift schedule should be at least 4 hours in length.</li>';
      htmlMessage =
        htmlMessage +
        '<li>A shift schedule cannot be exactly 8 hours in length.</li>';
      htmlMessage =
        htmlMessage +
        "<li>An Interviewer's weekly schedule should at a minimum match their core hours total.</li>";
      htmlMessage =
        htmlMessage +
        "<li>An Interviewer's weekly schedule should not exceed 40 hours total.</li>";
      htmlMessage =
        htmlMessage +
        "<li>An Interviewer's schedule should include 1 night shift, until at or after 9 PM, every other week.</li>";
      htmlMessage =
        htmlMessage +
        "<li>An Interviewer's schedule should include 1 weekend shift every other week.</li>";
      htmlMessage =
        htmlMessage +
        '<ul><li>A Friday night shift schedule with majority of hours after 5 PM, can only have 1 Friday night per month.</li>';
      htmlMessage =
        htmlMessage +
        '<li>A Saturday and/or Sunday shift schedule should be 6 hours minimum.</li></ul>';
      htmlMessage = htmlMessage + '</ul></p>';
    }

    if (this.selectedUser.schedulinglevel == 3) {
      htmlMessage = htmlMessage + '<p class="bold">Scheduling Level 3</p>';
      htmlMessage = htmlMessage + '<p><ul>';
      htmlMessage =
        htmlMessage +
        '<li>A shift schedule cannot be exactly 8 hours in length.</li>';
      htmlMessage =
        htmlMessage +
        "<li>An Interviewer's weekly schedule should at a minimum match their core hours total.</li>";
      htmlMessage =
        htmlMessage +
        "<li>An Interviewer's weekly schedule should not exceed 40 hours total.</li>";
      htmlMessage = htmlMessage + '</ul></p>';
    }

    let hoverMessage: HTMLElement = <HTMLElement>(
      document.getElementById('hover-message')
    );
    hoverMessage.innerHTML = htmlMessage;

    this.globalsService.showHoverMessage.next(true);

    hoverMessage.style.top = event.screenY + 'px';
    hoverMessage.style.left = event.clientX + 'px';
  }

  hideSchedulingLevelInfo(): void {
    this.globalsService.showHoverMessage.next(false);
  }

  //send user/months to controller to validate schedules based on level
  validateSchedules(sourceSchedules: ISchedule[], netId: string): void {
    //use validation message object to contain and send user netids and the months for the schedules
    let userMonths: IValidationMessage[] = [];

    for (var i = 0; i < sourceSchedules.length; i++) {
      let schedFirstOf: Date = new Date(sourceSchedules[i].startdatetime || '');
      schedFirstOf.setDate(1);
      let match: IValidationMessage[] = userMonths.filter(
        (x) =>
          x.dempoId == sourceSchedules[i].dempoid &&
          Utils.formatDateOnlyToStringUTC(x.inMonth) ==
          Utils.formatDateOnlyToStringUTC(schedFirstOf)
      );

      if (match.length < 1) {
        let userMonth: IValidationMessage = {
          dempoId: sourceSchedules[i].dempoid,
          inMonth: Utils.formatDateOnly(schedFirstOf) || new Date(),
          messageId: 0,
          validationMessagesId: 0,
          scheduleKeys: null,
          messageText: null,
          details: null,
        };
        userMonths.push(userMonth);
      }
    }

    //call validate schedules
    this.userScheduleService.validateSchedules(userMonths, netId).subscribe(
      (response) => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          try {
            this.getValidationMessages();
            //this.validationMessages = <IValidationMessage[]>(response.Subject).filter(x => x.DempoId == this.authenticatedUser.NetID);
          } catch (ex) {
            console.log(ex);
          }
        }
      },
      (error) => {
        console.error('Error validating schedules:', error);
      }
    );
  }

  public tryValidateSchedules(): void {
    let day: Date = this.contextDate;
    if (
      (this.selectedUser?.dempoId?.length || 0) < 1 &&
      this.authenticatedUser?.role == UserRole.Interviewer
    ) {
      this.selectedUser = this.authenticatedUser;
    }
    console.log(this.selectedUser);
    if (day && this.authenticatedUser && this.selectedUser) {
      let netId: string = this.authenticatedUser?.netID || '';
      if (this.authenticatedUser.role !== UserRole.Interviewer) {
        //if the selected user is the current user, don't valiate schedules
        if (
          (this.selectedUser?.dempoId || '').toLowerCase() ==
          (this.authenticatedUser?.netID || '').toLowerCase()
        ) {
          return;
        }

        //if the selected user is not the current user, set it as the net id and proceed to validate schedules
        if (this.selectedUser?.dempoId) {
          netId = this.selectedUser.dempoId;
        }
      }

      //call validate schedules
      const selectedDateValue = day ? new Date(day) : new Date();

      this.userScheduleService.setUserValidationMessages(
        selectedDateValue,
        netId
      );
    }
  }
  getDuration(): number {
    const start = this.shiftForm.get('startTime')?.value;
    const end = this.shiftForm.get('endTime')?.value;

    if (!start || !end) {
      return 0;
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
    return parseFloat(String(total)) || 0;
  }

}
