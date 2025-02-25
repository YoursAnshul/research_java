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

  tooltipMessage: string = ''; // New property to store tooltip content
  showTooltip: boolean = false; // Control visibility
  tooltipPosition = { top: '0px', left: '0px' };

  hoverMessage: HoverMessage = new HoverMessage();
  filteredShiftSchedule: any[] = [];
  @Output() resetShiftSchedule = new EventEmitter<void>();

  constructor(private globalsService: GlobalsService) {}

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
    const startHour = this.convertTimeToSlot(startTime);
    const endHour = this.convertTimeToSlot(endTime);
    const duration = endHour - startHour;

    return {
      left: `${(startHour - 8) * 6.25 + 10.1}%`,
      width: `${duration * 6.25}%`,
    };
  }
  convertTimeToSlot(time: string): number {
    const [hour, minute, period] = time.match(/(\d+):(\d+) (\w+)/)!.slice(1);
    let hours = parseInt(hour);
    if (period === 'PM' && hours !== 12) hours += 12;
    if (period === 'AM' && hours === 12) hours = 0;
    return hours;
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

    let htmlMessage: string = `
      <p class="hover-message-title">
        ${us.user.userName} : ${schedule.startTime} – ${
      schedule.endTime
    } - ${Utils.formatDateOnlyToStringUTC(schedule.dayWiseDate)}
      </p>`;

    if (schedule.duration) {
      htmlMessage += `<p class="bold">Hours:</p><p>${schedule.duration}</p>`;
    }
    if (schedule.comments) {
      htmlMessage += `<p class="bold">Comments:</p><p>${schedule.comments}</p>`;
    }

    // Set tooltip content
    this.tooltipMessage = htmlMessage;
    this.showTooltip = true;

    // Position tooltip near cursor
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
