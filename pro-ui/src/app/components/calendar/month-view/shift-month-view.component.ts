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

  constructor() {}

  ngOnInit(): void {}
  ngOnChanges(changes: SimpleChanges): void {
    this.processShiftSchedules();
  }
  processShiftSchedules(): void {
    if (!this.selectedDateRange?.value?.start || !this.selectedDateRange?.value?.end) {
      return;
    }
  
    const startOfWeek = new Date(this.selectedDateRange.value.start);
    const endOfWeek = new Date(this.selectedDateRange.value.end);
    startOfWeek.setHours(0, 0, 0, 0);
    endOfWeek.setHours(0, 0, 0, 0);
  
    if (!this.monthSchedules || !this.monthSchedules.weekSchedules) {
      this.monthSchedules = { weekSchedules: [] };
    }
  
    let existingWeekSchedule = this.monthSchedules.weekSchedules.find(
      (week) => week.weekStart.getTime() === startOfWeek.getTime()
    );
  
    if (!existingWeekSchedule) {
      existingWeekSchedule = {
        weekStart: startOfWeek,
        day1Schedules: [],
        day2Schedules: [],
        day3Schedules: [],
        day4Schedules: [],
        day5Schedules: [],
        day6Schedules: [],
        day7Schedules: [],
      };
      this.monthSchedules.weekSchedules.push(existingWeekSchedule);
    } else {
      for (let i = 1; i <= 7; i++) {
        (existingWeekSchedule as any)[`day${i}Schedules`] = [];
      }
    }
  
    const selectedMonth = startOfWeek.toLocaleString('default', { month: 'long' });
  
    for (let i = 0; i < this.shiftSchedule.length; i++) {
      const shift = this.shiftSchedule[i];
      console.log("shift---------", shift);
      
      const shiftDate = new Date(shift.dayWiseDate);
      shiftDate.setHours(0, 0, 0, 0);
  
      if (shiftDate >= startOfWeek && shiftDate <= endOfWeek) {
        const dayIndex = shiftDate.getDay();
        const adjustedDayIndex = dayIndex === 0 ? 7 : dayIndex; 
  
        const schedule: ISchedule = {
          preschedulekey: shift.user.userId,
          displayName: shift.user.userName,
          projectName: shift.projects.projectName,
          projectColor: shift.projects.projectColor,
          scheduledate: shiftDate,
          comments: shift.comments,
          startTime: shift.startTime,
          endTime: shift.endTime,
          duration: parseFloat(shift.duration),
          dayOfWeek: adjustedDayIndex,
          weekStart: startOfWeek,
          weekEnd: endOfWeek,
          month: selectedMonth, 
          requestDetails: '',
          requestCode: '',
          userid: shift.user.userId,
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
  
        (existingWeekSchedule as any)[`day${adjustedDayIndex}Schedules`].push(schedule);
      }
    }
  }
  
  

  addShift(): void {
    this.resetShiftSchedule.emit();
  }
}
