import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { Utils } from '../../../classes/utils';
import {
  IAuthenticatedUser,
  ILegend,
  ISchedule,
  IValidationMessage,
  IWeekSchedules,
} from '../../../interfaces/interfaces';
import { GlobalsService } from '../../../services/globals/globals.service';
import { HoverMessage } from '../../../models/presentation/hover-message';
import { FormControl, FormGroup } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AuthenticationService } from '../../../services/authentication/authentication.service';
import moment from 'moment-timezone';
import { ScheduleService } from '../../schedule/schedule.service';
import { UserSchedulesService } from '../../../services/userSchedules/user-schedules.service';

@Component({
  selector: 'app-shift-week-view-v2',
  templateUrl: './shift-week-view.componentV2.html',
  styleUrls: ['./shift-week-view.component.css'],
})
export class ShiftWeekViewComponentV2 implements OnInit {
  @Input() weekSchedules: IWeekSchedules | null = null;
  @Input() monthPart: boolean = false;
  @Input() shiftSchedule: any[] = [];
  @Input() selectedDate!: FormControl;
  @Input() selectedDateRange!: FormGroup;
  @Output() resetShiftSchedule = new EventEmitter<void>();
  @Input() selectedUser: any = null;
  @Input() selectedProject: any = null;
  authenticatedUser!: IAuthenticatedUser;
  hoverMessage: HoverMessage = new HoverMessage();
  tooltipMessage: SafeHtml = ''; // New property to store tooltip content
  showTooltip: boolean = false;
  tooltipPosition: { top: string; left: string } = { top: '0px', left: '0px' };
  private debounceTimer: any;
  inputHeight: string = '30px';
  totalDuration: number = 0;
  @Output() sendWeekDate = new EventEmitter<FormControl>();
  @Output() scheduleData = new EventEmitter<any>();
  @Input() isLoading!: boolean;
  isHomeRedirect: boolean = false;
  profileType: string = '';
  private previouslyEditedSchedule: ISchedule | null = null;
  clickTimer: any = null;
  clickDelay = 250;
  @Input() pId: number = 0;
  processedSchedules: ISchedule[] = [];
  @Input() isScheduleUpdate: boolean = false;
  validationMessages: IValidationMessage[] = [];
  invalidScheduleKeys: string[] = [];
  invalidWeeks: string[] = [];

  constructor(
    private globalsService: GlobalsService,
    private sanitizer: DomSanitizer,
    private authenticationService: AuthenticationService,
    private cdr: ChangeDetectorRef,
    private scheduleService: ScheduleService,
    private userScheduleService: UserSchedulesService
  ) {
    this.authenticationService.authenticatedUser.subscribe(
      (authenticatedUser) => {
        this.authenticatedUser = authenticatedUser;
      }
    );
  }

  ngOnInit(): void {
    //subscribe to validation messages
    this.userScheduleService.userValidationMessages.subscribe((messages) => {
      this.validationMessages = messages;
    });

    //subscribe to invalid schedules
    this.userScheduleService.invalidSchedulesKeys.subscribe((scheduleKeys) => {
      this.invalidScheduleKeys = scheduleKeys;
    });

    //subscribe to invalid weeks
    this.userScheduleService.invalidWeeks.subscribe((weeks) => {
      this.invalidWeeks = weeks;
    });
  }

  scheduleInvalid(scheduleKey: number): boolean {
    return this.invalidScheduleKeys.some((x) => scheduleKey.toString() == x);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.processShiftSchedules();
  }
  hasSchedules(): boolean {
    if (!this.weekSchedules) return false;

    const schedules = [
      ...(this.weekSchedules.day1Schedules || []),
      ...(this.weekSchedules.day2Schedules || []),
      ...(this.weekSchedules.day3Schedules || []),
      ...(this.weekSchedules.day4Schedules || []),
      ...(this.weekSchedules.day5Schedules || []),
      ...(this.weekSchedules.day6Schedules || []),
      ...(this.weekSchedules.day7Schedules || []),
    ];

    return schedules.length > 0;
  }

  processShiftSchedules(): void {
    if (
      !this.selectedDateRange?.value?.start ||
      !this.selectedDateRange?.value?.end
    ) {
      return;
    }
    const startOfWeek = moment(this.selectedDateRange.value.start)
      .tz('America/New_York')
      .startOf('day')
      .toDate();
    const endOfWeek = moment(this.selectedDateRange.value.end)
      .tz('America/New_York')
      .endOf('day')
      .toDate();

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
    this.processedSchedules = [];
    this.shiftSchedule?.forEach((shift) => {
      const shiftDate = moment(shift.dayWiseDate)
        .tz('America/New_York')
        .startOf('day')
        .toDate();
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
          preschedulekey: shift?.preschedulekey || '',
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
          month: moment(shiftDate).format('MMMM'),
          requestDetails: '',
          requestCode: '',
          userid: shift.user?.userId || '',
          trainedon: '',
          language: null,
          entryBy: null,
          dempoid: shift.user?.dempoId,
          fname: null,
          lname: null,
          preferredfname: null,
          preferredlname: null,
          userName: null,
          expr1: null,
          isNew: shift.isNew === true || !shift.preschedulekey,
          projectId: shift.projects?.projectId,
          isEdit: shift.isEdit,
        };
        this.processedSchedules.push(schedule);
        (this.weekSchedules as any)[`day${adjustedDayIndex}Schedules`].push(
          schedule
        );
      }
    });
    const firstFilteredSchedule = this.pId
      ? this.processedSchedules.find(
          (schedule) => schedule.preschedulekey === this.pId
        )
      : null;
    if (
      firstFilteredSchedule &&
      (!this.previouslyEditedSchedule ||
        this.previouslyEditedSchedule.preschedulekey !==
          firstFilteredSchedule.preschedulekey)
    ) {
      this.openScheduleData(firstFilteredSchedule);
    }
    if (this.isScheduleUpdate) {
      this.processedSchedules.forEach((schedule) => {
        schedule.isEdit = false;
      });
    }
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
    const userName = schedule?.displayName ?? 'Unknown User';
    const startTime = schedule?.startTime ?? 'N/A';
    const endTime = schedule?.endTime ?? 'N/A';
    const projectName = schedule.projectName ?? 'N/A';
    const date =
      Utils.formatDateOnlyToStringUTC(schedule.scheduledate) ?? 'N/A';

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
  addShift(date: any): void {
    if (this.authenticatedUser?.interviewer) {
      if (!date) {
        console.warn('No date selected.');
        return;
      }

      const selectedDate = new Date(date);
      const currentDate = new Date();

      selectedDate.setHours(0, 0, 0, 0);
      currentDate.setHours(0, 0, 0, 0);

      if (selectedDate.getTime() < currentDate.getTime()) {
        console.warn('Past date selected, ignoring.');
        return;
      }
    }
    this.sendWeekDate.emit(date);
    this.resetShiftSchedule.emit();
  }
  onResetShiftSchedule(): void {
    console.log(' sdfsdfds:');
    this.resetShiftSchedule.emit();
  }
  calculateTotalDuration(): number {
    if (!this.weekSchedules) return 0;

    const days = [
      this.weekSchedules.day1Schedules,
      this.weekSchedules.day2Schedules,
      this.weekSchedules.day3Schedules,
      this.weekSchedules.day4Schedules,
      this.weekSchedules.day5Schedules,
      this.weekSchedules.day6Schedules,
      this.weekSchedules.day7Schedules,
    ];

    let totalUserCards = 0;
    this.totalDuration = days.reduce((total, schedules) => {
      if (schedules?.length) {
        totalUserCards += schedules.length; // Count user cards
        total += schedules.reduce(
          (sum, schedule) => sum + (schedule.duration || 0),
          0
        ); // Sum of durations
      }
      return total;
    }, 0);

    this.calculateInputHeight();
    return this.totalDuration;
  }

  calculateInputHeight(): void {
    const baseHeight = 30;
    const userCardHeight = 62;

    if (!this.weekSchedules) {
      this.inputHeight = `${baseHeight}px`;
      return;
    }

    const days = [
      this.weekSchedules.day1Schedules,
      this.weekSchedules.day2Schedules,
      this.weekSchedules.day3Schedules,
      this.weekSchedules.day4Schedules,
      this.weekSchedules.day5Schedules,
      this.weekSchedules.day6Schedules,
      this.weekSchedules.day7Schedules,
    ];

    let maxSchedulesPerDay = Math.max(
      ...days.map((schedules) => (schedules ? schedules.length : 0))
    );

    const newHeight = `${baseHeight + maxSchedulesPerDay * userCardHeight}px`;

    if (this.inputHeight !== newHeight) {
      this.inputHeight = newHeight;
      this.updateWeekCalendarHeight(newHeight);
      this.cdr.detectChanges();
    }
  }

  updateWeekCalendarHeight(height: string): void {
    const weekCalendar = document.getElementById('week-calendar1');
    if (weekCalendar) {
      weekCalendar.style.height = '500px';
      console.log(
        'weekCalendar.style.height----------- ',
        weekCalendar.style.height,
        this.monthPart
      );
    }
  }
  private getDayKeyForSchedule(
    schedule: ISchedule
  ): keyof IWeekSchedules | null {
    if (!this.weekSchedules) return null;

    for (const key of Object.keys(
      this.weekSchedules
    ) as (keyof IWeekSchedules)[]) {
      const schedules = this.weekSchedules[key];
      if (Array.isArray(schedules) && schedules.includes(schedule)) {
        return key;
      }
    }

    return null;
  }
  handleClick(schedule: ISchedule) {
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
    if (
      schedule.projectName == 'Sick' ||
      schedule.projectName == 'Absent' ||
      schedule.projectName == 'Arriving Late' ||
      schedule.projectName == 'Leaving Early'
    ) {
      return;
    }
    let date = schedule?.scheduledate ? new Date(schedule.scheduledate) : null;
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
    let tab = '';
    this.scheduleService.getSchedule().subscribe((data) => {
      if (data) {
        this.isHomeRedirect = data.isHomeRedirect;
        tab = data.tab;
      }
    });

    this.processedSchedules?.forEach((s) => (s.isEdit = false));
    this.scheduleService.getType().subscribe((type) => {
      if (type) {
        this.profileType = type;
      }
    });

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

    if (tab != 'Day' && this.isHomeRedirect) {
      schedule.tab = tab;
      schedule.isEdit = true;
    } else if (!this.isHomeRedirect) {
      schedule.tab = 'Week';
    }
    this.scheduleData.emit({ ...schedule });
  }
}
