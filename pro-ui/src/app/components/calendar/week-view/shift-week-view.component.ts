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
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

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
  @Input() selectedUser: any = null;
  @Input() selectedProject: any = null;
  hoverMessage: HoverMessage = new HoverMessage();
  tooltipMessage: SafeHtml = ''; // New property to store tooltip content
  showTooltip: boolean = false;
  tooltipPosition: { top: string; left: string } = { top: '0px', left: '0px' };
  constructor(
    private globalsService: GlobalsService,
    private sanitizer: DomSanitizer
  ) {}

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

    startOfWeek.setHours(0, 0, 0, 0);
    endOfWeek.setHours(23, 59, 59, 999);

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

    const selectedUserId = this.selectedUser?.userId ?? null;
    const selectedProjectId = this.selectedProject?.projectId ?? null;

    this.shiftSchedule.forEach((shift) => {
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
        const adjustedDayIndex = dayIndex === 0 ? 7 : dayIndex;

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
          weekStart: startOfWeek,
          weekEnd: endOfWeek,
          month: shiftDate.toLocaleString('default', { month: 'long' }),
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

        (this.weekSchedules as any)[`day${adjustedDayIndex}Schedules`].push(
          schedule
        );
      }
    });

    console.log('Filtered Week Schedules:', this.weekSchedules);
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
  displayHoverMessage(event: MouseEvent, schedule: ISchedule): void {
    if (!schedule) return;
  
    this.tooltipMessage = this.sanitizer.bypassSecurityTrustHtml(`
      <div style="width: 300px; padding: 10px; background-color: #fff; border: 1px solid #ccc; border-radius: 8px; box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);">
        <p style="font-weight: bold; margin: 0;">
          ${schedule.displayName} (${schedule.projectName}): ${schedule.startTime} – ${schedule.endTime}
        </p>
        ${
          schedule.duration
            ? `<p style="margin: 5px 0;"><span style="font-weight: bold;">Hours:</span> ${schedule.duration}hr</p>`
            : ''
        }
        ${
          schedule.comments
            ? `<p style="margin: 5px 0;"><span style="font-weight: bold;">Comments:</span> ${schedule.comments}</p>`
            : ''
        }
      </div>
    `);
  
    // Position tooltip above the cursor
    this.tooltipPosition = {
      top: `${event.clientY - 150}px`,
      left: `${event.clientX + 10}px`,
    };
    this.showTooltip = true;
  }
  

  hideHoverMessage(): void {
    this.showTooltip = false;
  }
  addShift(): void {
    this.resetShiftSchedule.emit(); // Emit event to parent component
  }
  onResetShiftSchedule(): void {
    console.log('Shift schedule and form reset.');
    this.resetShiftSchedule.emit();
  }
}
