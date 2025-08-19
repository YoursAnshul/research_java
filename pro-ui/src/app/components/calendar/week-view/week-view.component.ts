import { Component, Input, OnInit } from '@angular/core';
import { Utils } from '../../../classes/utils';
import {
  ILegend,
  ISchedule,
  IWeekSchedules,
  IAuthenticatedUser,
} from '../../../interfaces/interfaces';
import { GlobalsService } from '../../../services/globals/globals.service';
import { HoverMessage } from '../../../models/presentation/hover-message';
import { ScheduleService } from '../../schedule/schedule.service';
import { MatDialog } from '@angular/material/dialog';
import { ShiftScheduleComponent } from '../../schedule/shift-schedule.component';
import { AuthenticationService } from '../../../services/authentication/authentication.service';

@Component({
  selector: 'app-week-view',
  templateUrl: './week-view.component.html',
  styleUrls: ['./week-view.component.css'],
})
export class WeekViewComponent implements OnInit {
  @Input() weekSchedules: IWeekSchedules | null = null;
  @Input() monthPart: boolean = false;
  authenticatedUser!: IAuthenticatedUser;
  hoverMessage: HoverMessage = new HoverMessage();

  constructor(
    private authenticationService: AuthenticationService,
    private globalsService: GlobalsService,
    private scheduleService: ScheduleService,
    private dialog: MatDialog
  ) {
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
      }
    );
  }

  ngOnInit(): void {}

  public GetDaysDate(weekStart: Date | undefined, dayOfWeek: number): string {
    let workingDate: Date = new Date(weekStart || '');
    workingDate.setDate(workingDate.getDate() + (dayOfWeek - 1));
    const options: Intl.DateTimeFormatOptions = {
      day: 'numeric',
      month: 'numeric',
    };

    return workingDate.toLocaleString('en-US', options);
  }

  public GetDaysDateAsDate(
    weekStart: Date | undefined,
    dayOfWeek: number
  ): Date {
    let workingDate: Date = new Date(weekStart || '');
    workingDate.setDate(workingDate.getDate() + (dayOfWeek - 1));

    return workingDate;
  }

  formatDateOnlyString(dateToFormat: Date | null | undefined): string | null {
    if (!dateToFormat) {
      return null;
    }

    return Utils.formatDateOnlyToStringUTC(dateToFormat);
  }

  //open contextual popup for the clicked user
  openUserSchedule(
    netId: string | null,
    projectName: string | null = null,
    contextDate: Date | null = null,
    scheduleTabIndex: number | null = null
  ): void {
    if (!scheduleTabIndex) {
      if (this.monthPart) {
        //tab index of 3 = Month tab
        scheduleTabIndex = 3;
      } else {
        //tab index of 2 = Week tab
        scheduleTabIndex = 2;
      }
    }

    this.globalsService.showContextualPopup(
      scheduleTabIndex,
      netId,
      null,
      contextDate as Date
    );
  }

  displayHoverMessage(event: any, schedule: ISchedule): void {
    let htmlMessage: string =
      '<p class="hover-message-title">' +
      schedule.displayName +
      ' (' +
      schedule.projectName +
      '): ' +
      schedule.startTime +
      ' – ' +
      schedule.endTime +
      ' – ' +
      this.formatDateOnlyString(schedule.startdatetime) +
      ' </p>';
    if (schedule.comments) {
      htmlMessage =
        htmlMessage +
        '<p class="bold">Comments:</p><p>' +
        schedule.comments +
        '</p>';
    }

    this.hoverMessage.setAndShow(event, htmlMessage);
  }

  hideHoverMessage(): void {
    this.hoverMessage.hide();
  }

  openScheduleData(schedule: any): void {
    if (
      this.authenticatedUser.interviewer &&
      this.authenticatedUser.netID != schedule.dempoid
    ) {
      return;
    }
    let date = schedule?.scheduledate ? new Date(schedule.scheduledate+ 'T00:00:00-04:00') : null;
    let currentDate = new Date();
    if (date) {
      date.setHours(0, 0, 0, 0);
    }
    currentDate.setHours(0, 0, 0, 0);
    if (
      this.authenticatedUser.interviewer &&
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
    console.log('Clicked Week schedule------->:', schedule);
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
