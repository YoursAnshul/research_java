import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import {
  IAuthenticatedUser,
  IMonthSchedules,
  ISchedule,
  IWeekSchedules,
} from '../../../interfaces/interfaces';
import { FormControl, FormGroup } from '@angular/forms';
import { Utils } from '../../../classes/utils';
import moment from 'moment-timezone';
import { AuthenticationService } from '../../../services/authentication/authentication.service';
import { ScheduleService } from '../../schedule/schedule.service';

@Component({
  selector: 'app-shift-month-view',
  templateUrl: './shift-month-view.component.html',
  styleUrls: ['./shift-month-view.component.css'],
})
export class ShiftMonthViewComponent implements OnInit {
  @Input() monthSchedules!: IMonthSchedules;
  @Input() shiftSchedule: any[] = [];
  @Input() selectedDate!: FormControl;
  @Input() selectedDateRange!: FormGroup;
  @Input() weekSchedules: IWeekSchedules | null = null;
  @Output() resetShiftSchedule = new EventEmitter<void>();
  @Input() selectedUser: any = null;
  @Input() selectedProject: any = null;
  @Output() addDateEvent = new EventEmitter<Date>();
  @Output() monthDate = new EventEmitter<FormControl>();
  @Output() scheduleData = new EventEmitter<FormControl>();
  @Input() isLoading!: boolean;
  @Input() pId: number = 0;
  authenticatedUser!: IAuthenticatedUser;
  type: any = null;
  @Input() isEdit: boolean = false;
  @Input() isScheduleUpdate: boolean = false;

  constructor(
    private authenticationService: AuthenticationService,
    private scheduleService: ScheduleService
  ) {
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
      }
    );
  }

  ngOnInit(): void {
    this.processShiftSchedules();
  }
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isEdit']) {
      if (this.shiftSchedule?.length) {
        this.shiftSchedule.forEach((sch) => (sch.isEdit = false));
      }
    }
    if (
      changes['selectedUser'] ||
      changes['selectedProject'] ||
      changes['shiftSchedule'] ||
      changes['selectedDate']
    ) {
      this.processShiftSchedules();
    }
  }
  handleMonthDate(event: FormControl) {
    this.monthDate.emit(event);
  }
  handleWeekSchedule(schedule: any) {
    if (schedule) {
      this.resetAllIsEditFlags(schedule);
      schedule.tab = 'Month';
      ('');
      // schedule.isEdit = true;
      this.scheduleData.emit(schedule);
    }
  }
  resetAllIsEditFlags(scheduleToSkip: ISchedule): void {
    if (!this.monthSchedules?.weekSchedules?.length) return;

    for (const week of this.monthSchedules.weekSchedules) {
      for (let i = 1; i <= 7; i++) {
        const daySchedules: ISchedule[] =
          (week as any)[`day${i}Schedules`] || [];
        for (const schedule of daySchedules) {
          if (
            schedule &&
            schedule.preschedulekey !== scheduleToSkip.preschedulekey &&
            schedule.isEdit
          ) {
            schedule.isEdit = false;
          }
        }
      }
    }
  }

  processShiftSchedules(): void {
    if (!this.monthSchedules) {
      this.monthSchedules = { weekSchedules: [] };
    }

    this.monthSchedules.weekSchedules = [];

    const referenceDate = new Date(this.selectedDate?.value || new Date());
    const startOfMonth = new Date(
      referenceDate.getFullYear(),
      referenceDate.getMonth(),
      1
    );
    const weekStarts = this.getWeekStarts(startOfMonth);
    const startOfCalendarView = new Date(weekStarts[0]);
    const endOfCalendarView = new Date(weekStarts[weekStarts.length - 1]);
    endOfCalendarView.setDate(endOfCalendarView.getDate() + 6);
    startOfCalendarView.setHours(0, 0, 0, 0);
    endOfCalendarView.setHours(23, 59, 59, 999);
    if (!this.shiftSchedule || this.shiftSchedule.length === 0) {
      for (const weekStart of weekStarts) {
        this.monthSchedules.weekSchedules.push(
          this.createEmptyWeekSchedule(weekStart)
        );
      }
      return;
    }

    const shiftsByWeek: Map<string, ISchedule[]> = new Map();

    for (const shift of this.shiftSchedule) {
      const shiftDate = moment(shift.dayWiseDate)
        .tz('America/New_York')
        .startOf('day')
        .toDate();
      shiftDate.setHours(0, 0, 0, 0);
      if (shiftDate < startOfCalendarView || shiftDate > endOfCalendarView)
        continue;
      let isValid = true;

      if (
        this.selectedUser &&
        this.selectedUser.userId &&
        this.selectedUser.userId !== 0
      ) {
        isValid = isValid && shift.user?.userId === this.selectedUser.userId;
      }

      if (
        this.selectedProject &&
        this.selectedProject.projectId &&
        this.selectedProject.projectId !== 0
      ) {
        isValid =
          isValid &&
          shift.projects?.projectId === this.selectedProject.projectId;
      }

      if (!isValid) continue;

      const weekStart = this.getWeekStart(shiftDate);
      const schedule: ISchedule = {
        preschedulekey: shift?.preschedulekey || '',
        displayName: shift.user?.userName || '',
        projectName: shift.projects?.projectName || '',
        projectColor: shift.projects?.projectColor || '',
        scheduledate: shiftDate,
        comments: shift.comments || '',
        startTime: shift.startTime || '',
        endTime: shift.endTime || '',
        duration: parseFloat(shift.duration) || 0,
        dayOfWeek: shiftDate.getDay() === 0 ? 7 : shiftDate.getDay(),
        weekStart: weekStart,
        weekEnd: endOfCalendarView,
        month: moment(shiftDate).format('MMMM'),
        requestDetails: '',
        requestCode: '',
        userid: shift.user?.userId || '',
        trainedon: '',
        language: null,
        entryBy: null,
        dempoid: null,
        fname: null,
        lname: null,
        preferredfname: null,
        preferredlname: null,
        userName: null,
        expr1: null,
        isNew: shift.isNew === true || !shift.preschedulekey,
        projectId: shift.projects?.projectId,
        isEdit: shift.isEdit,
      };

      const key = weekStart.toISOString();
      if (!shiftsByWeek.has(key)) {
        shiftsByWeek.set(key, []);
      }
      shiftsByWeek.get(key)!.push(schedule);
    }

    for (const weekStart of weekStarts) {
      const weekSchedule = this.createEmptyWeekSchedule(weekStart);
      const key = weekStart.toISOString();

      if (shiftsByWeek.has(key)) {
        const weekShifts = shiftsByWeek.get(key) || [];
        for (const schedule of weekShifts) {
          const dayIndex = schedule.dayOfWeek;
          (weekSchedule as any)[`day${dayIndex}Schedules`].push(schedule);
        }
      }

      this.monthSchedules.weekSchedules.push(weekSchedule);
    }
  }

  createEmptyWeekSchedule(weekStart: Date): IWeekSchedules {
    return {
      weekStart: new Date(weekStart),
      day1Schedules: [],
      day2Schedules: [],
      day3Schedules: [],
      day4Schedules: [],
      day5Schedules: [],
      day6Schedules: [],
      day7Schedules: [],
    };
  }

  getWeekStarts(referenceDate: Date): Date[] {
    const weekStarts: Date[] = [];

    const month = referenceDate.getMonth() + 1;
    const numberOfWeeks = month === 3 || month === 6 ? 6 : 5;

    const firstDayOfMonth = new Date(
      referenceDate.getFullYear(),
      referenceDate.getMonth(),
      1
    );
    const dayOfWeek = firstDayOfMonth.getDay();
    const daysToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;

    const firstMonday = new Date(firstDayOfMonth);
    firstMonday.setDate(firstDayOfMonth.getDate() + daysToMonday);

    for (let i = 0; i < numberOfWeeks; i++) {
      const currentMonday = new Date(firstMonday);
      currentMonday.setDate(firstMonday.getDate() + i * 7);
      weekStarts.push(currentMonday);
    }

    return weekStarts;
  }

  getWeekStart(date: Date): Date {
    const day = date.getDay();
    const diff = day === 0 ? -6 : 1 - day;
    const weekStart = new Date(date);
    weekStart.setDate(date.getDate() + diff);
    weekStart.setHours(0, 0, 0, 0);
    return weekStart;
  }

  onResetShiftSchedule(): void {
    // this.resetShiftSchedule.emit();
  }
}
