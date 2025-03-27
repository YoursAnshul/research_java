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
  IWeekSchedules,
} from '../../../interfaces/interfaces';
import { GlobalsService } from '../../../services/globals/globals.service';
import { HoverMessage } from '../../../models/presentation/hover-message';
import { FormControl, FormGroup } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AuthenticationService } from '../../../services/authentication/authentication.service';

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
  authenticatedUser!: IAuthenticatedUser;
  hoverMessage: HoverMessage = new HoverMessage();
  tooltipMessage: SafeHtml = ''; // New property to store tooltip content
  showTooltip: boolean = false;
  tooltipPosition: { top: string; left: string } = { top: '0px', left: '0px' };
  isLoading: boolean = false;
  private debounceTimer: any;
  inputHeight: string = '30px';
  totalDuration: number = 0;
  @Output() sendWeekDate = new EventEmitter<FormControl>();

  constructor(
    private globalsService: GlobalsService,
    private sanitizer: DomSanitizer,
    private authenticationService: AuthenticationService,
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
    this.processShiftSchedules();
  }
  hasSchedules(): boolean {
    if (!this.weekSchedules) return false;
  
    const schedules = [
      ...this.weekSchedules.day1Schedules || [],
      ...this.weekSchedules.day2Schedules || [],
      ...this.weekSchedules.day3Schedules || [],
      ...this.weekSchedules.day4Schedules || [],
      ...this.weekSchedules.day5Schedules || [],
      ...this.weekSchedules.day6Schedules || [],
      ...this.weekSchedules.day7Schedules || []
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

    this.shiftSchedule?.forEach((shift) => {
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
    const formattedDate = new Date(schedule.scheduledate).toLocaleDateString(
      'en-US',
      {
        month: '2-digit',
        day: '2-digit',
        year: 'numeric',
      }
    );
    this.tooltipMessage = this.sanitizer.bypassSecurityTrustHtml(`
      <p style="font-weight: bold;">
         ${schedule.displayName} (${schedule.projectName}): ${
      schedule.startTime
    } – ${schedule.endTime} <br> ${formattedDate}
      </p>
     ${
       schedule.duration
         ? `<p><span style="font-weight: bold;">Hours:</span> ${schedule.duration}hr</p>`
         : ''
     }
  ${
    schedule.comments
      ? `<p><span style="font-weight: bold;">Comments:</span> ${schedule.comments}</p>`
      : ''
  }
    `);

    // Position tooltip above the cursor
    this.tooltipPosition = {
      top: `${event.clientY - 150}px`, // Adjust the value to move it above the cursor
      left: `${event.clientX + 10}px`,
    };
    this.showTooltip = true;
  }

  hideHoverMessage(): void {
    this.showTooltip = false;
  }
  addShift(date: any): void {    
    console.log('Date:--------->', date);
    
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
    const weekCalendar = document.getElementById('week-calendar');
    if (weekCalendar) {
      
      if(this.monthPart==false){
        weekCalendar.style.height = "500px";
      }else{
        if(weekCalendar.style.height=="500px"){

        }else{
          weekCalendar.style.height = height;
        }
        
      }
      console.log("weekCalendar.style.height----------- ",weekCalendar.style.height,this.monthPart);
      
    }
  }
}
