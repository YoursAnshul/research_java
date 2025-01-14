import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { FormControl } from '@angular/forms';
import { scheduled } from 'rxjs';
import { Utils } from '../../../classes/utils';
import { ISchedule, IUserMin, IUserSchedule } from '../../../interfaces/interfaces';
import { GlobalsService } from '../../../services/globals/globals.service';

@Component({
  selector: 'app-day-view',
  templateUrl: './day-view.component.html',
  styleUrls: ['./day-view.component.css']
})
export class DayViewComponent implements OnInit {

  @Input() userSchedules!: IUserSchedule[];
  @Input() selectedDate!: FormControl;

  constructor(private globalsService: GlobalsService) { }

  ngOnInit(): void {
  }

  customScheduleCard(startTime: Date | null | undefined, totalHours: number) {
    var startTimeCode = 0;
    startTime = new Date(startTime || '');
    startTimeCode = startTime.getHours() * 2;
    if (startTime.getMinutes() == 30) {
      startTimeCode = startTimeCode + 1;
    }

    return {
      'left': ((((startTimeCode) - 15 + 1) * 2.35) + 14.1) + '%',
      'width': (totalHours * 4.7) + '%',
    };
  }

  customProjectSwatch(projectColor: string, scheduledHours?: number) {
    if (projectColor.length > 7) {
      projectColor = projectColor.substring(0, 7);
    }

    return {
      'background-color': projectColor,
    };

    //let marginRight: string = '10px';
    //if (scheduledHours <= 1) {
    //  marginRight = '0px';

    //  return {
    //    'width': '100%',
    //    'margin-right': marginRight,
    //    'background-color': projectColor,
    //  };
    //} else if (scheduledHours == 1.5) {
    //  marginRight = '5px';
    //}

    //return {
    //  'margin-right': marginRight,
    //  'background-color': projectColor,
    //};
  }

  customScheduleStyle(scheduledHours?: number) {
    if (scheduledHours == 1.5) {
      return {};
      //return {
      //  'font-size': '0.85em',
      //};
    } else {
      return {};
    }
  }

  getTime(dateToFormat: Date): string | null {
    return Utils.formatDateToTimeString(dateToFormat, true);
  }

  //open contextual popup for the clicked user
  openUserSchedule(netId: string, projectName: string | null = null): void {
    //tab index of 1 = Day tab
    this.globalsService.showContextualPopup(1, netId, null, (this.selectedDate.value ? Utils.formatDateOnly(this.selectedDate.value as Date) : null) as Date);
  }

  displayHoverMessage(event: any, schedule: ISchedule, us: IUserSchedule): void {
    let htmlMessage: string = '<p class="hover-message-title">' + us.user.displayName + ' (' + schedule.projectName + '): ' + schedule.startTime + ' – ' + schedule.endTime + '<p>';

    //comments
    if (schedule.comments) {
      htmlMessage = htmlMessage + '<p class="bold">Comments:</p><p>' + schedule.comments + '</p>';
    }

    let hoverMessage: HTMLElement | null = document.getElementById('hover-message');
    if (hoverMessage != null) {
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
