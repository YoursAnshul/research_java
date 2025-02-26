import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { Utils } from '../../../classes/utils';
import {
  ILegend,
  ISchedule,
  IUserSchedule,
} from '../../../interfaces/interfaces';
import { GlobalsService } from '../../../services/globals/globals.service';
import { HoverMessage } from '../../../models/presentation/hover-message';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-shift-day-view',
  templateUrl: './shift-day-view.component.html',
  styleUrls: ['./shift-day-view.component.css'],
})
export class ShiftDayViewComponent implements OnInit {
  @Input() userSchedules!: IUserSchedule[];
  @Input() selectedDate!: FormControl;
  @Input() shiftSchedule: any[] = [];
  @Input() selectedUser: any = null;
  @Input() selectedProject: any = null;

  tooltipMessage: SafeHtml = ''; // New property to store tooltip content
  showTooltip: boolean = false; // Control visibility
  tooltipPosition = { top: '0px', left: '0px' };

  hoverMessage: HoverMessage = new HoverMessage();
  filteredShiftSchedule: any[] = [];
  @Output() resetShiftSchedule = new EventEmitter<void>();

  constructor(
    private globalsService: GlobalsService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {}

  ngOnChanges(changes: SimpleChanges): void {
    const selectedDateValue = this.selectedDate?.value
      ? new Date(this.selectedDate.value)
      : null;

    const selectedUserId = this.selectedUser?.userId || 0;
    const selectedtProjectId = this.selectedProject?.projectId || 0;

    this.filteredShiftSchedule = this.shiftSchedule.filter((schedule) => {
      const scheduleDate = new Date(schedule.dayWiseDate).toLocaleDateString(
        'en-CA'
      );
      const isDateMatch = selectedDateValue
        ? scheduleDate === selectedDateValue.toLocaleDateString('en-CA')
        : true;
      const isUserMatch = selectedUserId
        ? schedule.user.userId === selectedUserId
        : true;
      const isProjectMatch = selectedtProjectId
        ? schedule.projects.projectId === selectedtProjectId
        : true;
      return isDateMatch && isUserMatch && isProjectMatch;
    });
  }

  customScheduleCard(startTime: string, endTime: string) {
    const startHour = this.convertTimeToSlot(startTime); // Convert start time to a slot index
    const endHour = this.convertTimeToSlot(endTime); // Convert end time to a slot index
    const duration = endHour - startHour; // Calculate duration

    return {
      left: `${(startHour - 8) * 5.25 + 10.1}%`, // Adjust left positioning based on the start time
      width: `${duration * 5.8}%`, // Width should match the hourly slot width dynamically
    };
  }

  convertTimeToSlot(time: string): number {
    const [hours, minutes] = time.split(/[: ]/);
    let hour = parseInt(hours);
    if (time.includes('PM') && hour !== 12) hour += 12;
    if (time.includes('AM') && hour === 12) hour = 0;
    const slot = hour + (minutes === '30' ? 0.5 : 0); // Adjust for half-hour slots
    return slot;
  }

  customProjectSwatch(projectColor: string, scheduledHours?: number) {
    if (projectColor.length > 7) {
      projectColor = projectColor.substring(0, 7);
    }

    return {
      'background-color': projectColor,
    };
  }

  customScheduleStyle(scheduledHours?: number) {
    if (scheduledHours == 1.5) {
      return {};
    } else {
      return {};
    }
  }

  getTime(dateToFormat: Date): string | null {
    return Utils.formatDateToTimeString(dateToFormat, true);
  }
  openUserSchedule(netId: string, projectName: string | null = null): void {
    this.globalsService.showContextualPopup(
      1,
      netId,
      null,
      (this.selectedDate.value
        ? Utils.formatDateOnly(this.selectedDate.value as Date)
        : null) as Date
    );
  }

  displayHoverMessage(
    event: MouseEvent,
    schedule: ISchedule,
    us: IUserSchedule
  ): void {
    console.log('us-------', us);

    this.tooltipMessage = this.sanitizer.bypassSecurityTrustHtml(`
      <p style="font-weight: bold;">
        ${us.user.userName}: ${schedule.startTime} – ${
      schedule.endTime
    } - ${Utils.formatDateOnlyToStringUTC(schedule.dayWiseDate)}
      </p>
      ${
        schedule.duration
          ? `<p><span style="font-weight: bold;">Hours:</span> ${schedule.duration}</p>`
          : ''
      }
  ${
    schedule.comments
      ? `<p><span style="font-weight: bold;">Comments:</span> ${schedule.comments}</p>`
      : ''
  }
    `);

    // Show and position tooltip
    this.showTooltip = true;
    this.tooltipPosition = {
      top: `${event.clientY + 10}px`,
      left: `${event.clientX + 10}px`,
    };
  }

  hideHoverMessage(): void {
    this.showTooltip = false;
  }
  addShift(): void {
    this.resetShiftSchedule.emit(); // Emit event to parent component
  }
}
