import { Component, Input, OnInit, Output, EventEmitter, Directive } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MAT_MOMENT_DATE_ADAPTER_OPTIONS, MomentDateAdapter } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepicker } from '@angular/material/datepicker';
import { Moment } from 'moment';
import { Utils } from '../../../classes/utils';
import { IAuthenticatedUser, IDropDownValue, IFormField, IFormFieldVariable, IProjectGroup, IProjectMin, IWeekStartAndEnd} from '../../../interfaces/interfaces';
import { AuthenticationService } from '../../../services/authentication/authentication.service';
import { LogsService } from '../../../services/logs/logs.service';
import { UserSchedulesService } from '../../../services/userSchedules/user-schedules.service';

@Component({
  selector: 'app-shift-calendar-controls',
  templateUrl: './shift-calendar-controls.component.html',
  styleUrls: ['./shift-calendar-controls.component.css'],
})

export class ShiftCalendarControlsComponent implements OnInit {
  //inputs
  @Input() authenticatedUser!: IAuthenticatedUser;
  @Input() _selectedDate!: FormControl;

  @Input() _selectedWeek!: FormControl;
  @Input() _languageFieldInfo!: IFormField;
  @Input() _calendarType!: string;

  @Input() _languages!: IDropDownValue[];
  @Input() _projects!: IProjectMin[];
  @Input() selectedDateRange!: FormGroup;

  //filters
  @Input() _userFilter!: FormControl;
  @Input() _languageFilter!: FormControl;
  @Input() _projectFilter!: FormControl;
  @Input() _conditionalOperatorFilter!: FormControl;
  @Input() _trainedOnFilter!: FormControl;
  @Input() _notTrainedOnFilter!: FormControl;



  //outputs
  @Output() selectedDateChange = new EventEmitter<FormControl>();

  //filters
  @Output() filterChange = new EventEmitter();

  //events
  @Output() resetDefaultFilters = new EventEmitter();


  projectGroups: IProjectGroup[] = [];
  startViewText!: string;
  scheduleFetchStatus!: boolean;
  todayPickerLabel: string = 'Today';
  scheduleFetchMessage: string = '';

  errorMessage!: string;

  constructor(private authenticationService: AuthenticationService,
    private userSchedulesService: UserSchedulesService,
    private logsService: LogsService) { }

  ngOnInit(): void {
     
    this.setStartView();
//subscribe to scheduleFetchStatus
    this.userSchedulesService.scheduleFetchStatus.subscribe(
      scheduleFetchStatus => {
        this.scheduleFetchStatus = scheduleFetchStatus;
        if (!scheduleFetchStatus) {
          this.scheduleFetchMessage = 'last refresh: ' + Utils.formatDateToTimeString(new Date(), false, true);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  ngOnChanges(): void {
    //group projects for dropdown
    let projectGroup: IProjectGroup = {
      name: 'Projects',
      projects: this._projects?.filter(x => x.projectType !== 'Administrative')
    };

    let adminGroup: IProjectGroup = {
      name: 'Admin',
      projects: this._projects?.filter(x => x.projectType == 'Administrative')
    };

    this.projectGroups = [projectGroup, adminGroup];

  }

  //set the start view text based on calendar type
  public setStartView(): void {
    let currDate: Date = new Date();
    let days: string[] = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    switch (this._calendarType.toUpperCase()) {
      case 'DAY': {
        this.startViewText = "month";
        //this.todayPickerLabel = 'Today';
        this.todayPickerLabel = 'Current Day: '  + days[currDate.getDay()] + ', '+ Utils.formatDateAsStringUTC(currDate);
        break;
      }
      case 'MONTH': {
        this.startViewText = "year";
        this.todayPickerLabel = 'Current Month: ' + Utils.formatDateMonthNameToString(currDate) + ', '+ currDate.getFullYear();
        break;

      }
      case 'WEEK': {
        this.startViewText = "month";
        let currentWeek: IWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(currDate);
        this.todayPickerLabel = 'Current Week: ' + Utils.formatDateOnlyToStringUTC(currentWeek.weekStart, false, true) + ' – ' + Utils.formatDateOnlyToStringUTC(currentWeek.weekEnd, false, true);
        break;
      }


      default:
        this.startViewText = "month";
        this.todayPickerLabel = 'Current Day: ' + days[currDate.getDay()] + ', ' + Utils.formatDateOnlyWithMonthNameToString(currDate);
        break;

    }
  }

  //route add/subtract functions based on the calendar type
  public addDateUnitsToSelectedDate(unit: number): void {
    switch (this._calendarType.toUpperCase()) {
      case 'DAY': {
        this.addDaysToSelectedDate(unit);
        break;
      }
      case 'MONTH':{
        this.addMonthsToSelectedDate(unit)
        break;
      }
      case 'WEEK': {
        this.addWeeksToSelectedDate(unit);
        break;
      }

      default: {
        this.addDaysToSelectedDate(unit);
        break;
      }
    }
  }

  //add (or subract with a negative number) a day to the anchor date
  public addDaysToSelectedDate(days: number): void {
    let selectedDt: Date = new Date(this._selectedDate.value);
    selectedDt.setDate(selectedDt.getDate() + days);
    this._selectedDate.setValue(selectedDt);

    //refresh the grid with the new date
    this.selectedDateChange.emit(this._selectedDate);
  }
  //add (or subract with a negative number) a week to the anchor date
  public addWeeksToSelectedDate(weeks: number): void {
    weeks = weeks * 7;
    let selectedDt: Date = new Date(this._selectedDate.value);
    selectedDt.setDate(selectedDt.getDate() + weeks);
    this._selectedDate.setValue(selectedDt);
    //set the week start so we can set the anchor date to it
    let selectedWeekStartAndEnd: IWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date(this._selectedDate.value));
    //set the anchor date to the week start
    this._selectedDate.setValue(selectedWeekStartAndEnd.weekStart);

    //refresh the grid with the new date
    this.selectedDateChange.emit(this._selectedDate);
  }




  //emit selected date
  emitSelectedDate(): void {
    let selectedDt: Date = new Date(this._selectedDate.value);
    this._selectedDate.setValue(selectedDt);
    this.selectedDateChange.emit(this._selectedDate);
  }

  public addMonthsToSelectedDate(months: number): void {
    let selectedDt: Date = new Date(this._selectedDate.value);
    selectedDt.setMonth(selectedDt.getMonth() + months);
    selectedDt.setDate(1);
    this._selectedDate.setValue(selectedDt);
    this.selectedDateChange.emit(this._selectedDate);
  }

  backToToday(): void {
    this._selectedDate.setValue(new Date());
    this.selectedDateChange.emit(this._selectedDate);
  }

}
