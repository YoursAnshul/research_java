import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import {
  ISchedule,
  IUserSchedule,
  IMonthSchedules,
  IWeekSchedules,
  IAuthenticatedUser,
  IWeekStartAndEnd,
  IProject,
} from '../../interfaces/interfaces';
import { UserSchedulesService } from '../../services/userSchedules/user-schedules.service';
import { FormControl, FormGroup } from '@angular/forms';
import { Utils } from '../../classes/utils';
import { LogsService } from '../../services/logs/logs.service';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { User } from '../../models/data/user';

@Component({
  selector: 'app-shift-calendar',
  templateUrl: './shift.calendar.component.html',
  styleUrls: ['./shift.calendar.component.css'],
})
export class ShifCalendarComponent implements OnInit {
  errorMessage!: string;
  selectedDate: FormControl = new FormControl(new Date());
  scheduleFetchStatus!: boolean;
  scheduleFetchMessage: string = '';
  tabIndex = 0;
  @Input() shiftSchedule: any[] = [];

  constructor(
    private userSchedulesService: UserSchedulesService,
    private logsService: LogsService
  ) {}

  ngOnInit(): void {    
    this.userSchedulesService.scheduleFetchStatus.subscribe(
      (scheduleFetchStatus) => {
        
        this.scheduleFetchStatus = scheduleFetchStatus;
        if (!scheduleFetchStatus) {
          this.scheduleFetchMessage =
            'last refresh: ' +
            Utils.formatDateToTimeString(new Date(), false, true);
        }
      },
      (error) => {
        this.errorMessage = <string>error.message;
        this.logsService.logError(this.errorMessage);
        console.log(this.errorMessage);
      }
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['shiftSchedule']) {
      console.log('Shift Schedule Updated-----------:', this.shiftSchedule);
    }
  }

  onTabChanged(tabChangeEvent: MatTabChangeEvent): void {
    this.tabIndex = tabChangeEvent.index;
  }

  public getAllUserSchedulesByAnchorDate(): void {
    this.userSchedulesService.selectedDate.next(
      new Date(this.selectedDate.value)
    );
    this.userSchedulesService.setAllUserSchedulesByAnchorDate(
      Utils.formatDateOnlyToStringUTC(this.selectedDate.value, true, true, true)
    );
  }
}
