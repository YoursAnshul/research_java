import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import {
  ISchedule,
  IUserSchedule,
  IMonthSchedules,
  IWeekSchedules,
  IAuthenticatedUser,
  IWeekStartAndEnd,
  IProject,
  IFormField,
  IDropDownValue,
  IProjectMin,
} from '../../interfaces/interfaces';
import { UserSchedulesService } from '../../services/userSchedules/user-schedules.service';
import { FormControl, FormGroup } from '@angular/forms';
import { Utils } from '../../classes/utils';
import { LogsService } from '../../services/logs/logs.service';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { User } from '../../models/data/user';
import { UsersService } from '../../services/users/users.service';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { ProjectsService } from '../../services/projects/projects.service';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-shift-calendar',
  templateUrl: './shift.calendar.component.html',
  styleUrls: ['./shift.calendar.component.css'],
})
export class ShifCalendarComponent implements OnInit {
  @Input() authenticatedUser!: IAuthenticatedUser;
  @Input() contextUser!: User;
  user!: User;

  @Input() contextProject!: IProject;
  //SCHEDULES
  allUsers!: User[];
  //top level schedules - only changes when date changes
  userSchedulesMonth!: ISchedule[];
  //filter month and week versions
  filteredUserSchedulesMonth!: ISchedule[];
  filteredUserSchedulesWeek!: ISchedule[];
  //top level user + schedules - only changes when date changes
  userSchedulesDay!: IUserSchedule[];
  //filtered version of userSchedulesDay
  filteredUserSchedulesDay: IUserSchedule[] = [];

  //interfaces to encapuslate and organize month and week data
  monthSchedules!: IMonthSchedules;
  weekSchedules!: IWeekSchedules | null;

  //GENERAL
  errorMessage!: string;

  //2-WAY BOUND DATA
  selectedDate: FormControl = new FormControl(new Date());
  selectedWeekStartAndEnd: IWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(
    new Date(this.selectedDate.value)
  );
  selectedDateRange!: FormGroup;
  languageFieldInfo!: IFormField;
  languages!: IDropDownValue[];
  projects!: IProjectMin[];

  //FILTERS
  userFilter: FormControl = new FormControl('2');
  languageFilter: FormControl = new FormControl(['0']);
  projectFilter: FormControl = new FormControl(['0']);
  conditionalOperatorFilter: FormControl = new FormControl('1');
  trainedOnFilter: FormControl = new FormControl(['0']);
  notTrainedOnFilter: FormControl = new FormControl(['0']);

  anylanguageToggle: boolean = true;
  anyProjectToggle: boolean = true;
  anyTrainedOnToggle: boolean = true;
  anyNotTrainedOnToggle: boolean = true;

  scheduleFetchStatus!: boolean;
  scheduleFetchMessage: string = '';

  tabIndex = 0;
  userList: any[] = [];
  projectList: any[] = [];
  @Input() shiftSchedule: any[] = [];
  @Output() resetShiftSchedule = new EventEmitter<void>(); // Output event to parent component

  //constructor
  constructor(
    private userSchedulesService: UserSchedulesService,
    private usersService: UsersService,
    private configurationService: ConfigurationService,
    private projectsService: ProjectsService,
    private logsService: LogsService,
    private http: HttpClient
  ) {
    //subscribe to users
    this.usersService.allUsersMin.subscribe(
      (allUsers) => {
        this.allUsers = allUsers;

        this.getAllUserSchedulesByAnchorDate();
      },
      (error) => {
        this.errorMessage = <string>error.message;
        this.logsService.logError(this.errorMessage);
        console.log(this.errorMessage);
      }
    );

    //subscribe to user schedules subject
    this.userSchedulesService.userSchedules.subscribe(
      (userSchedules) => {
        // console.log(userSchedules)
        this.userSchedulesMonth = userSchedules.filter((x) => !x.addInitial);

        this.syncData(this.userSchedulesMonth);
        //set filtered lists

        //apply default filters
        this.applyFilters();
      },
      (error) => {
        this.errorMessage = <string>error.message;
        this.logsService.logError(this.errorMessage);
        console.log(this.errorMessage);
      }
    );

    //subscribe to languages
    this.configurationService.languages.subscribe(
      (languages) => {
        this.languageFieldInfo = languages.formField;
        this.languages = languages.dropDownValues || [];
      },
      (error) => {
        this.errorMessage = <string>error.message;
        this.logsService.logError(this.errorMessage);
        console.log(this.errorMessage);
      }
    );

    //subscribe to projects
    this.projectsService.allProjectsMin.subscribe(
      (projects) => {
        this.projects = projects.filter((x) => x.active);
      },
      (error) => {
        this.errorMessage = <string>error.message;
        this.logsService.logError(this.errorMessage);
        console.log(this.errorMessage);
      }
    );
  }

  ngOnInit(): void {
    //subscribe to scheduleFetchStatus
    this.getAuthor();
    this.getProjectInfo();
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
    //set default date and filter values
    this.userSchedulesService.selectedDate.subscribe((selectedDate) => {
      this.selectedDate = new FormControl(selectedDate.toISOString());

      this.selectedWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(
        new Date(this.selectedDate.value)
      );

      this.selectedDateRange = new FormGroup({
        start: new FormControl(
          new Date(this.selectedWeekStartAndEnd.weekStart)
        ),
        end: new FormControl(new Date(this.selectedWeekStartAndEnd.weekEnd)),
      });
    });

    this.userSchedulesService.selectedDate.next(new Date());

    this.selectedWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date());

    this.selectedDateRange = new FormGroup({
      start: new FormControl(new Date(this.selectedWeekStartAndEnd.weekStart)),
      end: new FormControl(new Date(this.selectedWeekStartAndEnd.weekEnd)),
    });

    //filters
    this.setDefaultFilters(false);

    console.log('this.selectedDate ---', this.selectedDate);
  }

  ngOnChanges(): void {
    this.checkContext();
  }

  onTabChanged(tabChangeEvent: MatTabChangeEvent): void {
    // console.log('tabChangeEvent => ', tabChangeEvent);
    // console.log('index => ', tabChangeEvent.index);
    this.tabIndex = tabChangeEvent.index;
    // console.log("tabIndex" + this.tabIndex);
  }

  checkContext(applyFilters: boolean = true): void {
    //show only the context user, if specified
    if (this.contextUser) {
      if (this.contextUser.dempoid) {
        this.filteredUserSchedulesMonth = this.userSchedulesMonth.filter(
          (x) => x.dempoid == this.contextUser.dempoid
        );

        if (applyFilters) {
          this.applyFilters();
        }
      }
    }

    //show only the context project, if specified
    if (this.contextProject) {
      if (this.contextProject.projectID) {
        this.filteredUserSchedulesMonth = this.userSchedulesMonth.filter(
          (x) => x.projectid == this.contextProject.projectID
        );

        if (applyFilters) {
          this.applyFilters();
        }
      }
    }
  }

  //end ngOnInit

  //---------------------------------------------------
  // Get and sync data
  //---------------------------------------------------
  //get user schedules based on the date that is selected
  public getAllUserSchedulesByAnchorDate(): void {
    //----------------------------------------------------
    // get the first and last days of the selected week
    //----------------------------------------------------
    this.userSchedulesService.selectedDate.next(
      new Date(this.selectedDate.value)
    );

    //----------------------------------------------------
    // set user schedules
    //----------------------------------------------------
    this.userSchedulesService.setAllUserSchedulesByAnchorDate(
      Utils.formatDateOnlyToStringUTC(this.selectedDate.value, true, true, true)
    );
  }

  //sync data from the month down to the week and day (month becomes our master data set and is what we ultimately pull from the database when the anchor date changes)
  public syncData(userSchedules: ISchedule[]): void {
    if (!userSchedules || !this.allUsers) {
      return;
    }

    //-------------------------------
    // month view
    //-------------------------------
    //get user schedules for the selected day's month
    this.filteredUserSchedulesMonth = userSchedules;

    const uniqueValue = (value: any, index: number, self: any[]) => {
      return self.indexOf(value) === index;
    };

    this.monthSchedules = { weekSchedules: [] };
    var weekStarts = this.getWeekStarts(new Date(this.selectedDate.value));

    weekStarts = weekStarts.sort((a, b) => {
      return <any>new Date(a) - <any>new Date(b);
    });

    for (var i = 0; i < weekStarts.length; i++) {
      let weekSchedules: IWeekSchedules = {
        weekStart: weekStarts[i],
        day1Schedules: this.filteredUserSchedulesMonth.filter(
          (x) =>
            Utils.formatDateOnlyToStringUTC(x.weekStart) ===
              Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 1
        ),
        day2Schedules: this.filteredUserSchedulesMonth.filter(
          (x) =>
            Utils.formatDateOnlyToStringUTC(x.weekStart) ===
              Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 2
        ),
        day3Schedules: this.filteredUserSchedulesMonth.filter(
          (x) =>
            Utils.formatDateOnlyToStringUTC(x.weekStart) ===
              Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 3
        ),
        day4Schedules: this.filteredUserSchedulesMonth.filter(
          (x) =>
            Utils.formatDateOnlyToStringUTC(x.weekStart) ===
              Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 4
        ),
        day5Schedules: this.filteredUserSchedulesMonth.filter(
          (x) =>
            Utils.formatDateOnlyToStringUTC(x.weekStart) ===
              Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 5
        ),
        day6Schedules: this.filteredUserSchedulesMonth.filter(
          (x) =>
            Utils.formatDateOnlyToStringUTC(x.weekStart) ===
              Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 6
        ),
        day7Schedules: this.filteredUserSchedulesMonth.filter(
          (x) =>
            Utils.formatDateOnlyToStringUTC(x.weekStart) ===
              Utils.formatDateOnlyToStringUTC(weekStarts[i]) && x.dayOfWeek == 7
        ),
      };
      this.monthSchedules.weekSchedules.push(weekSchedules);
    }

    //-------------------------------
    // week view
    //-------------------------------
    //get user schedules for the selected day's week
    this.weekSchedules = null;
    for (var i = 0; i < this.monthSchedules.weekSchedules.length; i++) {
      if (
        Utils.formatDateOnlyToStringUTC(
          this.monthSchedules.weekSchedules[i].weekStart
        ) ===
        Utils.formatDateOnlyToStringUTC(this.selectedWeekStartAndEnd.weekStart)
      ) {
        this.weekSchedules = this.monthSchedules.weekSchedules[i];
      }
    }

    this.filteredUserSchedulesWeek = this.filteredUserSchedulesMonth.filter(
      (x) =>
        Utils.formatDateOnlyToStringUTC(x.weekStart) ===
        Utils.formatDateOnlyToStringUTC(this.selectedWeekStartAndEnd.weekStart)
    );

    //-------------------------------
    // day view
    //-------------------------------
    var tempUserSchedulesDay: IUserSchedule[] = [];

    var tempSchedulesToday: ISchedule[] =
      this.filteredUserSchedulesMonth.filter(
        (x) =>
          Utils.formatDateOnlyToStringUTC(x.startdatetime) ===
          Utils.formatDateOnlyToStringUTC(this.selectedDate.value)
      );

    //get user schedules for the selected day
    this.allUsers.forEach(function (user) {
      let userSchedule = <IUserSchedule>{
        user: user,
        schedules: <ISchedule[]>(
          tempSchedulesToday.filter((x) => x.userid === user.userid)
        ),
      };

      //sort user schedules by start time
      userSchedule.schedules.sort(function (a, b) {
        return (
          (Utils.formatDateOnly(a.startdatetime) || new Date()).getTime() -
          (Utils.formatDateOnly(b.startdatetime) || new Date()).getTime()
        );
      });

      tempUserSchedulesDay.push(userSchedule);
    });
    this.userSchedulesDay = tempUserSchedulesDay;

    //sort user schedules by user
    this.userSchedulesDay.sort((a, b) =>
      (a.user.displayName || '').localeCompare(b.user.displayName || '')
    );
  }

  //get the start of each week (Monday-based) in the given month
  getWeekStarts(referenceDateTime: Date): Date[] {
    let referenceDate: Date = Utils.formatDateOnly(referenceDateTime) as Date;
    let weekStarts: Date[] = [];

    //get the week start of the first of the month
    let firstOfMonth: Date = new Date(
      referenceDate.getFullYear(),
      referenceDate.getMonth(),
      1
    );
    if (firstOfMonth.getDay() == 0) {
    }
    let mondayDifference: number =
      1 - (firstOfMonth.getDay() == 0 ? 7 : firstOfMonth.getDay());
    let firstMonday: Date = new Date(
      referenceDate.getFullYear(),
      referenceDate.getMonth(),
      1 + mondayDifference
    );

    //get the week start of the last of the month
    let lastDayOfMonth: Date = new Date(
      referenceDate.getFullYear(),
      referenceDate.getMonth(),
      new Date(
        referenceDate.getFullYear(),
        referenceDate.getMonth() + 1,
        0
      ).getDate()
    );
    mondayDifference =
      1 - (lastDayOfMonth.getDay() == 0 ? 7 : lastDayOfMonth.getDay());
    let lastMonday: Date = new Date(
      lastDayOfMonth.getFullYear(),
      lastDayOfMonth.getMonth(),
      lastDayOfMonth.getDate() + mondayDifference
    );

    let weeksBetween: number =
      (lastMonday.getTime() - firstMonday.getTime()) /
      (1000 * 60 * 60 * 24) /
      7;

    //add the first monday
    weekStarts.push(firstMonday);

    //add each monday inbetween
    for (var i = 1; i < weeksBetween; i++) {
      let currentMonday: Date = new Date(
        firstMonday.getFullYear(),
        firstMonday.getMonth(),
        firstMonday.getDate() + i * 7
      );
      weekStarts.push(currentMonday);
    }

    //add the last monday
    if (
      weekStarts.filter(
        (x) =>
          Utils.formatDateOnlyToStringUTC(x) ==
          Utils.formatDateOnlyToStringUTC(lastMonday)
      ).length < 1
    )
      weekStarts.push(lastMonday);

    //console.log(weekStarts);

    return weekStarts;
  }

  //---------------------------------------------------
  // FILTERS
  //---------------------------------------------------
  //apply filters to the current calendar view
  public applyFilters(): void {
    if (this.anyNotTrainedOnToggle) {
      this.trainedOnFilter.disable();
    } else {
      this.trainedOnFilter.enable();
    }

    //reset filtered datasets to default and then refilter
    //this.setDefaultFilters(true);

    this.filteredUserSchedulesMonth = this.userSchedulesMonth;
    //check for context user or project and filter out to include only that user or project
    this.checkContext(false);

    //schedule filter - must be done first so if we filter out all project schedules for a user, that user gets removed, but stays if they have projects left
    if (!this.projectFilter.value.includes('0')) {
      this.filteredUserSchedulesMonth = this.filteredUserSchedulesMonth.filter(
        (x) =>
          this.projectFilter.value.includes(
            x.projectid ? x.projectid.toString() : ''
          )
      );
    }

    //use user-specific filters at the month level (may negate the "user schedule filter" section below, but that hasn't been tested, it works as-is)
    this.filteredUserSchedulesMonth = this.filteredUserSchedulesMonth.filter(
      (x) => this.filterScheduleByUser(x)
    );

    this.syncData(this.filteredUserSchedulesMonth);

    //user schedule filter
    if (this.userSchedulesDay) {
      this.filteredUserSchedulesDay = this.userSchedulesDay.filter((x) =>
        this.filterUser(x)
      );
    }
  }

  //filter logic per-user - day only
  public filterUser(user: IUserSchedule): boolean {
    let keepUser: boolean = false;

    //user filter
    if (this.userFilter.value == '1' && user.user.status == '1') {
      keepUser = true;
    } else if (
      this.userFilter.value == '2' &&
      user.user.status == '1' &&
      user.schedules.length > 0
    ) {
      keepUser = true;
    }

    //from here on out, we don't continue unless still true

    //language filter
    if (
      !(
        (this.languageFilter.value.includes('0') ||
          Utils.arrayIncludesAny(
            this.pipeStringToArray(user.user.language),
            this.languageFilter.value
          )) &&
        keepUser
      )
    ) {
      keepUser = false;
    }

    //trained on filter;
    var trainedOn = false;
    if (
      this.trainedOnFilter.value.includes('0') ||
      Utils.arrayIncludesAll(
        this.trainedOnFilter.value,
        this.pipeStringToArray(user.user.trainedon)
      )
    ) {
      trainedOn = true;
    }

    var trainedOnAny = false;
    if (this.trainedOnFilter.value.includes('0')) {
      trainedOnAny = true;
    }

    //not trained on filter;
    var notTrainedOn = false;
    if (
      this.notTrainedOnFilter.value.includes('0') ||
      !Utils.arrayIncludesAny(
        this.notTrainedOnFilter.value,
        this.pipeStringToArray(user.user.trainedon)
      )
    ) {
      notTrainedOn = true;
    }

    var notTrainedOnAny = false;
    if (this.notTrainedOnFilter.value.includes('0')) {
      notTrainedOnAny = true;
    }

    //conditional operator filter
    //AND
    if (this.conditionalOperatorFilter.value == '1') {
      if (!(trainedOn && notTrainedOn && keepUser)) {
        keepUser = false;
      }
    }
    //OR
    else if (this.conditionalOperatorFilter.value == '2') {
      if ((trainedOn && !trainedOnAny) || (notTrainedOn && !notTrainedOnAny)) {
      }
      if (
        (trainedOn && !trainedOnAny) ||
        (notTrainedOn && !notTrainedOnAny) ||
        keepUser
      ) {
        keepUser = true;
      }
    }

    return keepUser;
  }

  //filter logic per-user - day only
  public filterScheduleByUser(schedule: ISchedule): boolean {
    let keepUser: boolean = false;
    let user = this.allUsers.find((x) => x.dempoid == schedule.dempoid);

    if (!user) {
      return false;
    }

    ////user filter
    //if (this.userFilter.value == '1' && user.Active) {
    //  keepUser = true;
    //}

    //language filter
    if (
      !(
        this.languageFilter.value.includes('0') ||
        Utils.arrayIncludesAny(
          this.pipeStringToArray(user.language),
          this.languageFilter.value
        )
      )
    ) {
      keepUser = false;
    } else {
      keepUser = true;
    }

    //from here on out, we don't continue unless still true
    //trained on filter;
    var trainedOn = false;
    if (
      this.trainedOnFilter.value.includes('0') ||
      Utils.arrayIncludesAll(
        this.trainedOnFilter.value,
        this.pipeStringToArray(user.trainedon)
      )
    ) {
      trainedOn = true;
    }

    var trainedOnAny = false;
    if (this.trainedOnFilter.value.includes('0')) {
      trainedOnAny = true;
    }

    //not trained on filter;
    var notTrainedOn = false;
    if (
      this.notTrainedOnFilter.value.includes('0') ||
      !Utils.arrayIncludesAny(
        this.notTrainedOnFilter.value,
        this.pipeStringToArray(user.trainedon)
      )
    ) {
      notTrainedOn = true;
    }

    var notTrainedOnAny = false;
    if (this.notTrainedOnFilter.value.includes('0')) {
      notTrainedOnAny = true;
    }

    //conditional operator filter
    //AND
    if (this.conditionalOperatorFilter.value == '1') {
      if (!(trainedOn && notTrainedOn && keepUser)) {
        keepUser = false;
      }
    }
    //OR
    else if (this.conditionalOperatorFilter.value == '2') {
      if ((trainedOn && !trainedOnAny) || (notTrainedOn && !notTrainedOnAny)) {
      }
      if (
        (trainedOn && !trainedOnAny) ||
        (notTrainedOn && !notTrainedOnAny) ||
        keepUser
      ) {
        keepUser = true;
      }
    }

    return keepUser;
  }

  //handle select, deselect and auto-select of the "any" values in multi-selects

  //filter logic per-schedule - alt week/month

  //unset filter defaults or reset the filter default if selected --bug - can't get it to reset to default
  //public unsetResetFilterDefaults(filterArray: string[], defaultValue: string): string[] {
  //  if (filterArray.length > 1) {
  //    if (filterArray.includes(defaultValue)) {
  //      filterArray = filterArray.filter(x => x !== defaultValue);
  //    }
  //  }

  //  return filterArray;
  //}

  //set default filters
  public setDefaultFilters(applyFilters: boolean): void {
    this.userFilter = new FormControl('2');
    this.languageFilter = new FormControl(['0']);
    this.projectFilter = new FormControl(['0']);
    this.conditionalOperatorFilter = new FormControl('1');
    this.trainedOnFilter = new FormControl(['0']);
    this.notTrainedOnFilter = new FormControl(['0']);

    if (applyFilters) {
      this.applyFilters();
    }
  }

  //---------------------------------------------------
  // Array Handling
  //---------------------------------------------------
  //return a string array from a pipe-delimited string
  public pipeStringToArray(pipeString: string): Array<string> {
    let returnArray: string[] = [];
    if (pipeString) {
      returnArray = pipeString.split('|');
    }

    return returnArray;
  }

  //check if 2 arrays are equal
  public arraysEqual(firstArray: any, secondArray: any): boolean {
    if (firstArray.length !== secondArray.length) {
      return false;
    }

    for (let i = 0; i < firstArray.length; i++) {
      if (!secondArray.includes(firstArray[i])) {
        return false;
      }
    }

    return true;
  }
  onResetShiftSchedule(): void {
    console.log('Shift schedule and form reset.');
    this.resetShiftSchedule.emit(); 
  }
  getAuthor(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/authors`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.userList = Array.isArray(data) ? data : [];
      },
      error: (error) => console.error('Error fetching authors:', error),
    });
  }
  getProjectInfo(): void {
    const apiUrl = `${environment.DataAPIUrl}/manage-announement/projects`;
    this.http.get(apiUrl).subscribe({
      next: (data: any) => {
        this.projectList = Array.isArray(data) ? data : [];
      },
      error: (error) => console.error('Error fetching projects:', error),
    });
  }
}
