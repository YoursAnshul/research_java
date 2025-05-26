import {
  ChangeDetectorRef,
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
import { ScheduleService } from '../../schedule/schedule.service';
import { MatDialog } from '@angular/material/dialog';
import { ShiftScheduleComponent } from '../../schedule/shift-schedule.component';

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
  @Output() selectedUserChange = new EventEmitter<any>();
  @Output() selectedProjectChange = new EventEmitter<any>();
  @Output() sendDate = new EventEmitter<FormControl>();
  @Output() scheduleData = new EventEmitter<any>();
  @Input() isLoading!: boolean;
  isHomeRedirect: boolean = false;
  profileType: string = '';
  private previouslyEditedSchedule: ISchedule | null = null;
  clickTimer: any = null;
  clickDelay = 250;
  @Input() pId: number = 0;
  @Input() isScheduleUpdate: boolean = false;

  constructor(
    private globalsService: GlobalsService,
    private sanitizer: DomSanitizer,
    private authenticationService: AuthenticationService,
    private scheduleService: ScheduleService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
      }
    );
  }

  ngOnInit(): void {}

  ngOnChanges(changes: SimpleChanges): void {
    if (this.selectedProject) {
      this.selectedProjectChange.emit(this.selectedProject);
    }
    const selectedDateValue = this.selectedDate?.value
      ? new Date(this.selectedDate.value)
      : null;

    const selectedUserId = this.selectedUser?.userId || 0;
    const selectedProjectId = this.selectedProject?.projectId || 0;

    this.filteredShiftSchedule = this.shiftSchedule
      ?.map((schedule) => {
        const scheduleDateUTC = new Date(schedule?.dayWiseDate);

        const scheduleDateET = new Date(
          scheduleDateUTC.toLocaleString('en-US', {
            timeZone: 'America/New_York',
          })
        );

        const formattedScheduleDate = scheduleDateET
          .toISOString()
          .split('T')[0];

        const selectedDateET = selectedDateValue
          ? new Date(
              selectedDateValue.toLocaleString('en-US', {
                timeZone: 'America/New_York',
              })
            )
              .toISOString()
              .split('T')[0]
          : null;

        const isDateMatch = selectedDateET
          ? formattedScheduleDate === selectedDateET
          : true;
        const isUserMatch = selectedUserId
          ? schedule.user.userId === selectedUserId
          : true;
        const isProjectMatch = selectedProjectId
          ? schedule.projects.projectId === selectedProjectId
          : true;

        schedule.duration = parseFloat(schedule.duration) || 0;

        if (schedule.dayWiseDate && schedule.startTime && schedule.endTime) {
          const utcStart = new Date(
            `${schedule.dayWiseDate} ${schedule.startTime} UTC`
          );
          const utcEnd = new Date(
            `${schedule.dayWiseDate} ${schedule.endTime} UTC`
          );

          const startET = new Date(
            utcStart.toLocaleString('en-US', { timeZone: 'America/New_York' })
          );
          const endET = new Date(
            utcEnd.toLocaleString('en-US', { timeZone: 'America/New_York' })
          );

          const timeOptions: Intl.DateTimeFormatOptions = {
            hour: 'numeric',
            minute: '2-digit',
            hour12: true,
            timeZone: 'America/New_York',
          };

          schedule.startTime = startET.toLocaleTimeString('en-US', timeOptions);
          schedule.endTime = endET.toLocaleTimeString('en-US', timeOptions);
        }

        return isDateMatch && isUserMatch && isProjectMatch ? schedule : null;
      })
      .filter((s) => s);
    if (this.pId > 0) {
      this.filteredShiftSchedule
        .filter((schedule) => schedule.preschedulekey === this.pId)
        .forEach((schedule) => (schedule.isEdit = true));

      const targetSchedule = this.filteredShiftSchedule.find(
        (item) => item.preschedulekey === this.pId
      );
      if (
        targetSchedule &&
        (!this.previouslyEditedSchedule ||
          this.previouslyEditedSchedule.preschedulekey !==
            targetSchedule.preschedulekey)
      ) {
        this.openScheduleData(targetSchedule);
      }
    }
    if (this.isScheduleUpdate) {
      this.filteredShiftSchedule.forEach((schedule) => {
        schedule.isEdit = false;
      });
    }
  }

  customScheduleCard(startTime: string, endTime: string) {
    const startHour = this.convertTimeToSlot(startTime); // Converts time to slot index (8 = 8 AM, 9 = 9 AM, etc.)
    const endHour = this.convertTimeToSlot(endTime); // Converts end time to slot index
    const duration = endHour - startHour; // Calculate event duration in hours
    let leftOffset = 0;
    let totalHours = 0;
    if (this.authenticatedUser?.interviewer) {
      leftOffset = 12.85;
      totalHours = 17;
    } else if (
      this.authenticatedUser?.admin &&
      this.selectedUser?.userId != 0
    ) {
      leftOffset = 12.85;
      totalHours = 17;
    } else if (
      this.authenticatedUser?.admin &&
      this.selectedUser?.userId == 0
    ) {
      leftOffset = 15.4;
      totalHours = 16;
    }
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
    const userName = us?.user?.userName ?? 'Unknown User';
    const startTime = schedule?.startTime ?? 'N/A';
    const endTime = schedule?.endTime ?? 'N/A';
    const projectName = schedule?.projects?.projectName ?? 'N/A';
    const date =
      Utils.formatDateOnlyToStringUTC(schedule?.dayWiseDate) ?? 'N/A';

    let htmlMessage: string =
      '<p class="hover-message-title">' +
      userName +
      ' (' +
      projectName +
      '): ' +
      ' - ' +
      startTime +
      ' – ' +
      endTime +
      ' - ' +
      date +
      '<p>';

    //comments
    if (schedule?.duration) {
      htmlMessage =
        htmlMessage +
        `<p style="margin: 2px 0;"><strong>Hours:</strong> ${schedule.duration} hr</p>`;
    }
    if (schedule?.comments) {
      htmlMessage =
        htmlMessage +
        `<p style="margin: 2px 0;"><strong>Comments:</strong> ${schedule.comments}</p>`;
    }

    this.hoverMessage.setAndShow(event, htmlMessage);
  }

  hideHoverMessage(): void {
    this.hoverMessage.hide();
  }
  addShift(): void {
    this.sendDate.emit(this.selectedDate);
    this.resetShiftSchedule.emit();
  }

  handleClick(schedule: any, event: MouseEvent) {
    if (this.clickTimer) {
      clearTimeout(this.clickTimer);
      this.clickTimer = null;
      return;
    }
    this.clickTimer = setTimeout(() => {
      this.openScheduleData(schedule);
      this.clickTimer = null;
    }, this.clickDelay);
  }
  openScheduleData(schedule: ISchedule): void {
    let tab = '';
    this.scheduleService.getSchedule().subscribe((data) => {
      if (data) {
        this.isHomeRedirect = data.isHomeRedirect;
        tab = data.tab;
      }
    });
    this.scheduleService.getType().subscribe((type) => {
      if (type) {
        this.profileType = type;
      }
    });
    this.filteredShiftSchedule?.forEach((s) => (s.isEdit = false));
    if (schedule.isEdit) {
      schedule.isEdit = false;
      this.previouslyEditedSchedule = null;
    } else {
      if (this.previouslyEditedSchedule) {
        this.previouslyEditedSchedule.isEdit = false;
      }
      schedule.isEdit = true;
      this.previouslyEditedSchedule = schedule;
    }

    if (this.isHomeRedirect) {
      schedule.tab = tab;
    } else {
      schedule.tab = 'Day';
    }
    this.scheduleData.emit({ ...schedule });
  }
}
