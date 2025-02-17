import { Component, Input, OnInit, SimpleChanges, } from '@angular/core';
import { Utils } from '../../../classes/utils';
import {ILegend, ISchedule, IWeekSchedules} from '../../../interfaces/interfaces';
import { GlobalsService } from '../../../services/globals/globals.service';
import { HoverMessage } from '../../../models/presentation/hover-message';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-shift-week-view',
  templateUrl: './shift-week-view.component.html',
  styleUrls: ['./shift-week-view.component.css']
})
export class ShiftWeekViewComponent implements OnInit {


  @Input() weekSchedules: IWeekSchedules | null = null;
  @Input() monthPart: boolean = false;
  @Input() shiftSchedule: any[] = [];
  @Input() selectedDate!: FormControl;
  @Input() selectedDateRange!: FormGroup;

  hoverMessage: HoverMessage = new HoverMessage();

  constructor(private globalsService: GlobalsService) { }

  ngOnInit(): void {
  }
    ngOnChanges(changes: SimpleChanges): void {
      console.log("selectedDateRange---",this.selectedDateRange);
      
      this.populateWeekSchedules();
    }
    private populateWeekSchedules(): void {
      // Initialize empty week schedules
      const weekSchedules: IWeekSchedules = {
        weekStart: new Date(), // Set the starting date for the week
        day1Schedules: [],
        day2Schedules: [],
        day3Schedules: [],
        day4Schedules: [],
        day5Schedules: [],
        day6Schedules: [],
        day7Schedules: []
      };
  
      // Iterate over each schedule to assign it to the correct day of the week
      this.shiftSchedule.forEach((schedule: ISchedule) => {
        const scheduleDate = new Date(schedule.dayOfWeek);
        const dayOfWeek = scheduleDate.getDay(); // Get the day of the week (0 = Sunday, 6 = Saturday)
  
        switch (dayOfWeek) {
          case 0:
            weekSchedules.day1Schedules.push(schedule);
            break;
          case 1:
            weekSchedules.day2Schedules.push(schedule);
            break;
          case 2:
            weekSchedules.day3Schedules.push(schedule);
            break;
          case 3:
            weekSchedules.day4Schedules.push(schedule);
            break;
          case 4:
            weekSchedules.day5Schedules.push(schedule);
            break;
          case 5:
            weekSchedules.day6Schedules.push(schedule);
            break;
          case 6:
            weekSchedules.day7Schedules.push(schedule);
            break;
          default:
            break;
        }
      });
  
      this.weekSchedules = weekSchedules;
      // console.log("weekSchedules---",this.weekSchedules);
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

    return Utils.formatDateOnlyToStringUTC(dateToFormat);
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

    this.hoverMessage.setAndShow(event, htmlMessage);

  }

  hideHoverMessage(): void {
    this.hoverMessage.hide();
  }

}
