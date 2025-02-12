import { Component, Input, OnInit } from '@angular/core';
import { ISchedule, IUserSchedule, IMonthSchedules, IWeekSchedules, IAuthenticatedUser, IWeekStartAndEnd, IProject } from '../../interfaces/interfaces';
import { UserSchedulesService } from '../../services/userSchedules/user-schedules.service';
import { FormControl, FormGroup } from '@angular/forms';
import { Utils } from '../../classes/utils';
import { LogsService } from '../../services/logs/logs.service';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { User } from '../../models/data/user';

@Component({
  selector: 'app-shift-calendar',
  templateUrl: './shift.calendar.component.html',
  styleUrls: ['./shift.calendar.component.css']
})
export class ShifCalendarComponent implements OnInit {
  @Input() authenticatedUser!: IAuthenticatedUser;
  @Input() contextUser!: User;
  user!: User;
  @Input() formData!: any; 

  @Input() contextProject!: IProject;
  userSchedulesMonth!: ISchedule[];
  filteredUserSchedulesMonth!: ISchedule[];
  filteredUserSchedulesWeek!: ISchedule[];
  userSchedulesDay!: IUserSchedule[];

  monthSchedules!: IMonthSchedules;
  weekSchedules!: IWeekSchedules | null;

  errorMessage!: string;

  selectedDate: FormControl = new FormControl(new Date());
  selectedWeekStartAndEnd: IWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date(this.selectedDate.value));
  selectedDateRange!: FormGroup;

  scheduleFetchStatus!: boolean;
  scheduleFetchMessage: string = '';

  tabIndex = 0;

  constructor(
    private userSchedulesService: UserSchedulesService,
    private logsService: LogsService
  ) {

    this.userSchedulesService.userSchedules.subscribe(
      userSchedules => {
        this.userSchedulesMonth = userSchedules.filter(x => !x.addInitial);
        this.syncData(this.userSchedulesMonth);
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  ngOnInit(): void {  
    console.log("Received Form Data in Shift Calendar:", this.formData);  
     this.userSchedulesService.scheduleFetchStatus.subscribe(
      scheduleFetchStatus => {
        this.scheduleFetchStatus = scheduleFetchStatus;
        if (!scheduleFetchStatus) {
          this.scheduleFetchMessage = 'last refresh: ' + Utils.formatDateToTimeString(new Date(), false, true);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
    this.userSchedulesService.selectedDate.subscribe(
      selectedDate => {
        this.selectedDate = new FormControl((selectedDate).toISOString());
        this.selectedWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date(this.selectedDate.value));
        this.selectedDateRange = new FormGroup({
          start: new FormControl(new Date(this.selectedWeekStartAndEnd.weekStart)),
          end: new FormControl(new Date(this.selectedWeekStartAndEnd.weekEnd)),
        });

      }
    );

    this.userSchedulesService.selectedDate.next(new Date());
    this.selectedWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date());
    this.selectedDateRange = new FormGroup({
      start: new FormControl(new Date(this.selectedWeekStartAndEnd.weekStart)),
      end: new FormControl(new Date(this.selectedWeekStartAndEnd.weekEnd)),
    });
  }

  ngOnChanges(): void {
  }

  onTabChanged(tabChangeEvent: MatTabChangeEvent): void {
    this.tabIndex = tabChangeEvent.index;
  }

  public getAllUserSchedulesByAnchorDate(): void {
    this.userSchedulesService.selectedDate.next(new Date(this.selectedDate.value));
    this.userSchedulesService.setAllUserSchedulesByAnchorDate(Utils.formatDateOnlyToStringUTC(this.selectedDate.value, true, true, true));
  }

  public syncData(userSchedules: ISchedule[]): void {
    this.filteredUserSchedulesMonth = userSchedules;
    this.monthSchedules = { weekSchedules: [] };
    var weekStarts = this.getWeekStarts(new Date(this.selectedDate.value));
    weekStarts = weekStarts.sort((a, b) => {
      return <any>new Date(a) - <any>new Date(b);
    });
    for (var i = 0; i < weekStarts.length; i++) {
      let weekSchedules: IWeekSchedules = {
        weekStart: weekStarts[i],
        day1Schedules: this.filteredUserSchedulesMonth.filter(x => Utils.formatDateOnlyToStringUTC(x.weekStart) === Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 1),
        day2Schedules: this.filteredUserSchedulesMonth.filter(x => Utils.formatDateOnlyToStringUTC(x.weekStart) === Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 2),
        day3Schedules: this.filteredUserSchedulesMonth.filter(x => Utils.formatDateOnlyToStringUTC(x.weekStart) === Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 3),
        day4Schedules: this.filteredUserSchedulesMonth.filter(x => Utils.formatDateOnlyToStringUTC(x.weekStart) === Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 4),
        day5Schedules: this.filteredUserSchedulesMonth.filter(x => Utils.formatDateOnlyToStringUTC(x.weekStart) === Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 5),
        day6Schedules: this.filteredUserSchedulesMonth.filter(x => Utils.formatDateOnlyToStringUTC(x.weekStart) === Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 6),
        day7Schedules: this.filteredUserSchedulesMonth.filter(x => Utils.formatDateOnlyToStringUTC(x.weekStart) === Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 7),
      };
      this.monthSchedules.weekSchedules.push(weekSchedules);
    }
    this.weekSchedules = null;
    for (var i = 0; i < this.monthSchedules.weekSchedules.length; i++) {
      if (Utils.formatDateOnlyToStringUTC(this.monthSchedules.weekSchedules[i].weekStart) === Utils.formatDateOnlyToStringUTC(this.selectedWeekStartAndEnd.weekStart)) {
        this.weekSchedules = this.monthSchedules.weekSchedules[i];
      }
    }

    this.filteredUserSchedulesWeek = this.filteredUserSchedulesMonth.filter(x => Utils.formatDateOnlyToStringUTC(x.weekStart) === Utils.formatDateOnlyToStringUTC(this.selectedWeekStartAndEnd.weekStart));

  }

  getWeekStarts(referenceDateTime: Date): Date[] {
    let referenceDate: Date = Utils.formatDateOnly(referenceDateTime) as Date;
    let weekStarts: Date[] = [];

    let firstOfMonth: Date = new Date(referenceDate.getFullYear(), referenceDate.getMonth(), 1);
    if (firstOfMonth.getDay() == 0) {

    }
    let mondayDifference: number = 1 - (firstOfMonth.getDay() == 0 ? 7 : firstOfMonth.getDay());
    let firstMonday: Date = new Date(referenceDate.getFullYear(), referenceDate.getMonth(), 1 + mondayDifference);
    let lastDayOfMonth: Date = new Date(referenceDate.getFullYear(), referenceDate.getMonth(), new Date(referenceDate.getFullYear(), referenceDate.getMonth() + 1, 0).getDate());
    mondayDifference = 1 - (lastDayOfMonth.getDay() == 0 ? 7 : lastDayOfMonth.getDay());
    let lastMonday: Date = new Date(lastDayOfMonth.getFullYear(), lastDayOfMonth.getMonth(), lastDayOfMonth.getDate() + mondayDifference);

    let weeksBetween: number = ((lastMonday.getTime() - firstMonday.getTime()) / (1000 * 60 * 60 * 24)) / 7;

    weekStarts.push(firstMonday);

    for (var i = 1; i < weeksBetween; i++) {
      let currentMonday: Date = new Date(firstMonday.getFullYear(), firstMonday.getMonth(), firstMonday.getDate() + (i * 7));
      weekStarts.push(currentMonday);
    }
    if (weekStarts.filter(x => Utils.formatDateOnlyToStringUTC(x) == Utils.formatDateOnlyToStringUTC(lastMonday)).length < 1)
      weekStarts.push(lastMonday);
    return weekStarts;
  }
}

