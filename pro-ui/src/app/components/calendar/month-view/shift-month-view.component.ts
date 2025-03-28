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
import moment from 'moment';

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
    if (!this.shiftSchedule || this.shiftSchedule.length === 0) {
      console.log('No shift schedule available.');
      return;
    }
  
    console.log('Selected Date:', this.selectedDate.value);
  
    const referenceDate = new Date(this.selectedDate.value);
    const startOfMonth = new Date(referenceDate.getFullYear(), referenceDate.getMonth(), 1);
    const endOfMonth = new Date(referenceDate.getFullYear(), referenceDate.getMonth() + 1, 0);
  
    startOfMonth.setHours(0, 0, 0, 0);
    endOfMonth.setHours(23, 59, 59, 999);
  
    // Always display 5 weeks starting from the first Monday
    let weekStarts = this.getWeekStarts(startOfMonth);
  
    if (!this.monthSchedules || !this.monthSchedules.weekSchedules) {
      this.monthSchedules = { weekSchedules: [] };
    }
    this.monthSchedules.weekSchedules = [];
  
    // Group shifts by week start date
    const shiftsByWeek: Map<string, ISchedule[]> = new Map();
  
    for (const shift of this.shiftSchedule) {
      const shiftDate = moment(shift.dayWiseDate)
              .tz('America/New_York')
              .startOf('day')
              .toDate();
      shiftDate.setHours(0, 0, 0, 0);
  
      if (shiftDate < startOfMonth || shiftDate > endOfMonth) {
        continue; // Skip shifts outside the current month
      }
  
      const weekStart = this.getWeekStart(shiftDate);
  
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
        dayOfWeek: shiftDate.getDay() === 0 ? 7 : shiftDate.getDay(),
        weekStart: weekStart,
        weekEnd: endOfMonth,
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
      };
  
      const key = weekStart.toISOString();
      if (!shiftsByWeek.has(key)) {
        shiftsByWeek.set(key, []);
      }
      shiftsByWeek.get(key)?.push(schedule);
    }
  
    // Display all 5 weeks and populate matching ones with data
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
  
  getWeekStarts(referenceDate: Date): Date[] {
    const weekStarts: Date[] = [];
    
    const month = referenceDate.getMonth() + 1;
    const numberOfWeeks = (month === 3 || month === 6) ? 6 : 5;
  
    const firstDayOfMonth = new Date(referenceDate.getFullYear(), referenceDate.getMonth(), 1);
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
    this.resetShiftSchedule.emit();
  }
}
