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
  IAuthenticatedUser,
  ILegend,
  ISchedule,
  IUserSchedule,
} from '../../../interfaces/interfaces';
import { GlobalsService } from '../../../services/globals/globals.service';
import { HoverMessage } from '../../../models/presentation/hover-message';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AuthenticationService } from '../../../services/authentication/authentication.service';

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
  authenticatedUser!: IAuthenticatedUser;

  constructor(
    private globalsService: GlobalsService,
    private sanitizer: DomSanitizer,
    private authenticationService: AuthenticationService
  ) {
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
      }
    );
  }

  ngOnInit(): void {
  }

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
      schedule.duration = parseFloat(schedule.duration) || 0;
      return isDateMatch && isUserMatch && isProjectMatch;
    });
  }

  customScheduleCard(startTime: string, endTime: string) {
    const startHour = this.convertTimeToSlot(startTime); // Converts time to slot index (8 = 8 AM, 9 = 9 AM, etc.)
    const endHour = this.convertTimeToSlot(endTime); // Converts end time to slot index
    const duration = endHour - startHour; // Calculate event duration in hours

    const leftOffset = this.authenticatedUser?.interviewer ? 12.85 : 15.4; // Adjust offset based on role
    const totalHours = this.authenticatedUser?.interviewer ? 17 : 16; // From 08:00 AM to 11:00 PM = 16 hours
    const slotWidth = (100 - leftOffset) / totalHours; // Remaining width for time slots

    return {
      left: `${leftOffset + (startHour - 8) * slotWidth}%`, // Calculate dynamic left position
      width: `${duration * slotWidth}%`, // Calculate dynamic width based on duration
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
    console.log('User Schedule:', us);

    const userName = us?.user?.userName ?? 'Unknown User';
    const startTime = schedule?.startTime ?? 'N/A';
    const endTime = schedule?.endTime ?? 'N/A';
    const date =
      Utils.formatDateOnlyToStringUTC(schedule?.dayWiseDate) ?? 'N/A';
    const duration = schedule?.duration
      ? `<p><strong>Hours:</strong> ${schedule.duration} hr</p>`
      : '';
    const comments = schedule?.comments
      ? `<p><strong>Comments:</strong> ${schedule.comments}</p>`
      : '';

    this.tooltipMessage = this.sanitizer.bypassSecurityTrustHtml(`
      <div style="
        padding: 20px;
        min-width: 250px;
        min-height: 80px;
        font-size: 1rem;
        line-height: 1.5;
      ">
        <p style="font-weight: bold;">
          ${userName}: ${startTime} – ${endTime} - ${date}
        </p>
        ${duration}
        ${comments}
      </div>
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
