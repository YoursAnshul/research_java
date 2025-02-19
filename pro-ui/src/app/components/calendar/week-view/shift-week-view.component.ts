import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { Utils } from '../../../classes/utils';
import {
  ILegend,
  ISchedule,
  IWeekSchedules,
} from '../../../interfaces/interfaces';
import { GlobalsService } from '../../../services/globals/globals.service';
import { HoverMessage } from '../../../models/presentation/hover-message';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-shift-week-view',
  templateUrl: './shift-week-view.component.html',
  styleUrls: ['./shift-week-view.component.css'],
})
export class ShiftWeekViewComponent implements OnInit {
  @Input() weekSchedules: IWeekSchedules | null = null;
  @Input() monthPart: boolean = false;
  @Input() shiftSchedule: any[] = [];
  @Input() selectedDate!: FormControl;
  @Input() selectedDateRange!: FormGroup;
  @Output() resetShiftSchedule = new EventEmitter<void>();

  hoverMessage: HoverMessage = new HoverMessage();

  constructor(private globalsService: GlobalsService) {}

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

    const startOfWeek = new Date(this.selectedDateRange.value.start);
    const endOfWeek = new Date(this.selectedDateRange.value.end);

    this.weekSchedules = {
      weekStart: startOfWeek,
      day1Schedules: [],
      day2Schedules: [],
      day3Schedules: [],
      day4Schedules: [],
      day5Schedules: [],
      day6Schedules: [],
      day7Schedules: [],
    };

    this.shiftSchedule.forEach((shift) => {
      const shiftDate = new Date(shift.dayWiseDate);
      shiftDate.setHours(0, 0, 0, 0);
      startOfWeek.setHours(0, 0, 0, 0);
      endOfWeek.setHours(0, 0, 0, 0);
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
          month: shiftDate.toLocaleString('default', { month: 'long' }),
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
        (this.weekSchedules as any)[`day${adjustedDayIndex}Schedules`].push(
          schedule
        );
      }
    });
    console.log('Updated Week Schedules:', this.weekSchedules);
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
  addShift(): void {
    this.resetShiftSchedule.emit(); // Emit event to parent component
  }
}
