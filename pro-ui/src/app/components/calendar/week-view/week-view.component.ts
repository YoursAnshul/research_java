import { Component, Input, OnInit, } from '@angular/core';
import { Utils } from '../../../classes/utils';
import {ILegend, ISchedule, IWeekSchedules} from '../../../interfaces/interfaces';
import { GlobalsService } from '../../../services/globals/globals.service';

@Component({
  selector: 'app-week-view',
  templateUrl: './week-view.component.html',
  styleUrls: ['./week-view.component.css']
})
export class WeekViewComponent implements OnInit {


  @Input() weekSchedules: IWeekSchedules | null = null;
  @Input() monthPart: boolean = false;

  constructor(private globalsService: GlobalsService) { }

  ngOnInit(): void {
  }

  public GetDaysDate(weekStart: Date | undefined, dayOfWeek: number): string {
    let workingDate: Date = new Date(weekStart || '');
    workingDate.setDate(workingDate.getDate() + (dayOfWeek - 1));
    const options: Intl.DateTimeFormatOptions = {
      day: 'numeric',
      month: 'numeric',
    };

    return workingDate.toLocaleString('en-US', options);
  }

  public GetDaysDateAsDate(weekStart: Date | undefined, dayOfWeek: number): Date {
    let workingDate: Date = new Date(weekStart || '');
    workingDate.setDate(workingDate.getDate() + (dayOfWeek - 1));

    return workingDate;
  }

  formatDateOnlyString(dateToFormat: Date | null | undefined): string | null {
    if (!dateToFormat) {
      return null;
    }

    return Utils.formatDateOnlyToString(dateToFormat);
  }

  //open contextual popup for the clicked user
  openUserSchedule(netId: string | null, projectName: string | null = null, contextDate: Date | null = null, scheduleTabIndex: number | null = null): void {
    if (!scheduleTabIndex) {
      if (this.monthPart) {
        //tab index of 3 = Month tab
        scheduleTabIndex = 3;
      } else {
        //tab index of 2 = Week tab
        scheduleTabIndex = 2
      }
    }

    this.globalsService.showContextualPopup(scheduleTabIndex, netId, null, contextDate as Date);
  }

  displayHoverMessage(event: any, schedule: ISchedule): void {
    let htmlMessage: string = '<p class="hover-message-title">' + schedule.displayName + ' (' + schedule.projectName + '): ' + schedule.startTime + ' – ' + schedule.endTime + ' – ' + this.formatDateOnlyString(schedule.startdatetime) + ' </p>';
    if (schedule.comments) {
      htmlMessage = htmlMessage + '<p class="bold">Comments:</p><p>' + schedule.comments + '</p>';
    }

    let hoverMessage: HTMLElement | null = document.getElementById('hover-message');

    if (hoverMessage) {
      hoverMessage.innerHTML = htmlMessage;

      this.globalsService.showHoverMessage.next(true);

      hoverMessage.style.top = (event.screenY) + 'px';
      hoverMessage.style.left = event.clientX + 'px';
    }

  }

  hideHoverMessage(): void {
    this.globalsService.showHoverMessage.next(false);
  }

}
