import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import {
  IMonthSchedules,
  ISchedule,
  IWeekSchedules,
} from '../../../interfaces/interfaces';
import { FormControl, FormGroup } from '@angular/forms';
import { Utils } from '../../../classes/utils';

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
  @Output() monthDate = new EventEmitter<Date>();

  constructor() {}

  ngOnInit(): void {}
  ngOnChanges(changes: SimpleChanges): void {
    this.processShiftSchedules();
  }
  processShiftSchedules(): void {
    if (
      !this.selectedDateRange?.value?.start ||
      !this.selectedDateRange?.value?.end
    ) {
      return;
    }

    console.log('Selected Date:', this.selectedDate.value);
    console.log('Date Range Start:', this.selectedDateRange.value.start);
    console.log('Date Range End:', this.selectedDateRange.value.end);

    const startOfWeek = new Date(this.selectedDateRange.value.start);
    const endOfWeek = new Date(this.selectedDateRange.value.end);
    let weekStarts = this.getWeekStarts(new Date(this.selectedDate.value)); // Now returns only 5 weeks

    startOfWeek.setHours(0, 0, 0, 0);
    endOfWeek.setHours(23, 59, 59, 999);

    if (!this.monthSchedules || !this.monthSchedules.weekSchedules) {
      this.monthSchedules = { weekSchedules: [] };
    }
    this.monthSchedules.weekSchedules = [];

    for (const weekStart of weekStarts) {
      let weekSchedule: IWeekSchedules = {
        weekStart: weekStart,
        day1Schedules: [],
        day2Schedules: [],
        day3Schedules: [],
        day4Schedules: [],
        day5Schedules: [],
        day6Schedules: [],
        day7Schedules: [],
      };

      const selectedUserId = this.selectedUser?.userId ?? null;
      const selectedProjectId = this.selectedProject?.projectId ?? null;
      const selectedMonth = weekStart.toLocaleString('default', {
        month: 'long',
      });

      if (this.shiftSchedule) {
        for (const shift of this.shiftSchedule) {
          const shiftDate = new Date(shift.dayWiseDate);
          shiftDate.setHours(0, 0, 0, 0);

          const isWithinDateRange =
            shiftDate >= startOfWeek && shiftDate <= endOfWeek;
          const isUserMatch = selectedUserId
            ? shift.user?.userId === selectedUserId
            : true;
          const isProjectMatch = selectedProjectId
            ? shift.projects?.projectId === selectedProjectId
            : true;

          if (isWithinDateRange && isUserMatch && isProjectMatch) {
            const dayIndex = shiftDate.getDay();
            const adjustedDayIndex = dayIndex === 0 ? 7 : dayIndex; // Convert Sunday to day7

            const schedule: ISchedule = {
              preschedulekey: shift.user?.userId || '',
              displayName: shift.user?.userName || '',
              projectName: shift.projects?.projectName || '',
              projectColor: shift.projects?.projectColor || '',
              scheduledate: shiftDate,
              comments: shift.comments || '',
              startTime: shift.startTime || '',
              endTime: shift.endTime || '',
              duration: parseFloat(shift.duration) || 0,
              dayOfWeek: adjustedDayIndex,
              weekStart: weekStart,
              weekEnd: endOfWeek,
              month: selectedMonth,
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
            };

            (weekSchedule as any)[`day${adjustedDayIndex}Schedules`].push(
              schedule
            );
          }
        }
      }

      this.monthSchedules.weekSchedules.push(weekSchedule);
    }
  }

  getWeekStarts(referenceDateTime: Date): Date[] {
    let referenceDate: Date = Utils.formatDateOnly(referenceDateTime) as Date;
    let weekStarts: Date[] = [];

    let firstOfMonth: Date = new Date(
      referenceDate.getFullYear(),
      referenceDate.getMonth(),
      1
    );
    let mondayDifference: number =
      1 - (firstOfMonth.getDay() == 0 ? 7 : firstOfMonth.getDay());
    let firstMonday: Date = new Date(
      firstOfMonth.getFullYear(),
      firstOfMonth.getMonth(),
      firstOfMonth.getDate() + mondayDifference
    );

    let lastDayOfMonth: Date = new Date(
      referenceDate.getFullYear(),
      referenceDate.getMonth() + 1,
      0
    );
    mondayDifference =
      1 - (lastDayOfMonth.getDay() == 0 ? 7 : lastDayOfMonth.getDay());
    let lastMonday: Date = new Date(
      lastDayOfMonth.getFullYear(),
      lastDayOfMonth.getMonth(),
      lastDayOfMonth.getDate() + mondayDifference
    );

    let weeksBetween: number = Math.floor(
      (lastMonday.getTime() - firstMonday.getTime()) / (1000 * 60 * 60 * 24 * 7)
    );

    for (let i = 0; i <= weeksBetween; i++) {
      let currentMonday: Date = new Date(firstMonday);
      currentMonday.setDate(firstMonday.getDate() + i * 7);
      weekStarts.push(currentMonday);
    }
    return weekStarts.slice(-6);
  }

  onResetShiftSchedule(): void {
    this.resetShiftSchedule.emit();
  }
}
