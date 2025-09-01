import { Component, Input, OnInit } from '@angular/core';
import { IMonthSchedules } from '../../../interfaces/interfaces';
import { ShiftScheduleComponent } from '../../schedule/shift-schedule.component';
import { UserRole } from '../../../models/presentation/enums';

@Component({
  selector: 'app-month-view',
  templateUrl: './month-view.component.html',
  styleUrls: ['./month-view.component.css'],
})
export class MonthViewComponent implements OnInit {
  @Input() monthSchedules!: IMonthSchedules;
  authenticatedUser: any;
  monthPart: any;
  scheduleService: any;
  dialog: any;

  constructor() {}

  ngOnInit(): void {}

  openScheduleData(schedule: any): void {
    let date = schedule?.scheduledate ? new Date(schedule.scheduledate) : null;
    let currentDate = new Date();
    if (
      this.authenticatedUser.role == UserRole.Interviewer &&
      date !== null &&
      date < currentDate
    ) {
      return;
    }
    if (this.monthPart) {
      schedule.tab = 'Month';
    } else {
      schedule.tab = 'Week';
    }
    schedule.isHomeRedirect = true;
    console.log('Clicked Week schedule--for month view---->:', schedule);
    this.scheduleService.setSchedule(schedule);
    const dialogRef = this.dialog.open(ShiftScheduleComponent, {
      width: '1900px',
      height: '900px',
      disableClose: true,
    });
    dialogRef.afterClosed().subscribe((result: any) => {
      console.log('Shift Schedule dialog was closed', result);
      this.scheduleService.clearSchedule();
    });
  }
}
