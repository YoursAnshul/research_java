import { Component, OnInit } from '@angular/core';
// import jsPDF from 'jspdf'
import 'jspdf-autotable'
// import domtoimage from 'dom-to-image';
import { GlobalsService } from '../../services/globals/globals.service';
import { IAuthenticatedUser, IProjectTotals, IProjectTotalsReport, IProjectTotalsReportFlat, IProjectTotalsSummed, ISchedule, IUserMin, IWeekStartAndEnd } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { UsersService } from '../../services/users/users.service';
import { FormControl, FormGroup } from '@angular/forms';
import { Utils } from '../../classes/utils';
import { UserSchedulesService } from '../../services/userSchedules/user-schedules.service';
import * as html2pdf from 'html2pdf.js';
import { ProjectsService } from '../../services/projects/projects.service';
import { LogsService } from '../../services/logs/logs.service';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  errorMessage!: string;
  authenticatedUser!: IAuthenticatedUser;
  selectedReport: string = 'Project Totals';
  selectedUser: string = '';
  selectedDateRange!: FormGroup;
  activeUsers!: IUserMin[];
  showReportResults: boolean = false;
  userSchedulesReport!: ISchedule[];
  displayedColumns: string[] = ['DisplayName', 'Project', 'Day', 'Date', 'StartTime', 'EndTime', 'Hours', 'Comments'];
  ptDisplayedColumns: string[] = ['ProjectColumn', 'Column1', 'Column2', 'Column3', 'Column4', 'Column5', 'Column6', 'Column7', 'TotalColumn'];
  projectTotalsProjects: string[] = [];
  projectTotalsReport: IProjectTotalsReport = {} as IProjectTotalsReport;
  projectTotalsReportFlat: IProjectTotalsReportFlat[] = [];
  projectTotalsWeekStartAndEnd: IWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date());
  projectTotalsDaysOfWeek: Date[] = [];
  projectTotalsDate: FormControl = new FormControl(new Date());
  projectTotalsDateRange: FormGroup;

  constructor(private globalsService: GlobalsService,
    private authenticationService: AuthenticationService,
    private usersService: UsersService,
    private userSchedulesService: UserSchedulesService,
    private projectsService: ProjectsService,
    private logsService: LogsService) {

    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;

        if (authenticatedUser?.netID) {
          if (!authenticatedUser.resourceGroup && !authenticatedUser.admin) {
            this.selectedReport = 'User Schedules';
          }
        }

        //set authenticated user as the default selected user
        if (this.authenticatedUser) {
          this.selectedUser = this.authenticatedUser.netID;
        }

      }
    );

    //get active users
    this.usersService.allUsersMin.subscribe(
      allUsers => {
        this.activeUsers = allUsers.filter(x => x.active);
      }
    );

    //project totals
    this.projectTotalsDateRange = new FormGroup({
      start: new FormControl(new Date(this.projectTotalsWeekStartAndEnd.weekStart)),
      end: new FormControl(new Date(this.projectTotalsWeekStartAndEnd.weekEnd)),
    });

    this.setProjectTotalsDaysOfWeek();
    this.generateProjectTotalsReport();
}

  ngOnInit(): void {
    //set current page for navigation menu to track
    this.globalsService.selectedPage.next('reports');

    //set default selected date range
    let monthStart: Date = new Date();
    monthStart.setDate(1);
    let monthEnd: Date = new Date();
    monthEnd.setMonth(monthEnd.getMonth() + 1);
    monthEnd.setDate(0);

    this.selectedDateRange = new FormGroup({
      start: new FormControl(monthStart),
      end: new FormControl(monthEnd),
    });

  }

  generatePdf(elementId: string, fileName: string): void {
    //scroll to the top or we have weird gaps/cutoffs at top of pdf
    window.scrollTo(0, 0);

    let pageElement: HTMLElement | null = document.getElementById(elementId);

    if (pageElement) {
      //setup options
      var options = {
        pagebreak: { avoid: ['.user-card', '.schedule-card', '.week-calendar-time-header', '.mat-row'] },
        margin: 0.25,
        filename: fileName + '.pdf',
        html2canvas: { scale: 2 },
        jsPDF: { unit: 'in', format: 'letter', orientation: 'landscape' }
      };

      //create pdf
      //html2pdf().set(options).from(pageElement).save();
      html2pdf.default(pageElement, options).then(function () {
        //user schedules
        Utils.hideShow('user-schedules-inputs', false);
        Utils.hideShow('daily-schedules-inputs', false);

        //daily schedules
        Utils.hideShow('day-calendar-controls', false);
        Utils.hideShow('week-calendar-controls', false);
        Utils.hideShow('month-calendar-controls', false);
        Utils.hideShow('daily-schedules-report', false, 'mat-tab-header');

        //project totals
        Utils.hideShow('project-totals-inputs', false);
      });
    }
  }

  generateUserSchedulesPdf(): void {
    let fileName: string = 'UserSchedulesReport_' + this.selectedUser + '_' + Utils.formatDateOnlyToString(this.selectedDateRange.value.start, true) + '_' + Utils.formatDateOnlyToString(this.selectedDateRange.value.end, true);

    Utils.hideShow('user-schedules-inputs');
    //generate pdf
    this.generatePdf('user-schedules-report', fileName);
  }

  generateProjectTotalsPdf(): void {
    let fileName: string = 'ProjectTotalsReport_' + Utils.formatDateOnlyToString(this.projectTotalsDateRange.value.start, true) + '_' + Utils.formatDateOnlyToString(this.projectTotalsDateRange.value.end, true);

    Utils.hideShow('project-totals-inputs');
    //generate pdf
    this.generatePdf('project-totals-report', fileName);

    //setTimeout(function () {
    //  Utils.hideShow('project-totals-inputs', false);
    //}, 1000);
  }

  generateDailySchedulesPdf(): void {
    let fileName: string = 'DailySchedulesReport_' + this.selectedUser;

    //hide before generating pdf
    Utils.hideShow('daily-schedules-inputs');
    Utils.hideShow('day-calendar-controls');
    Utils.hideShow('week-calendar-controls');
    Utils.hideShow('month-calendar-controls');
    Utils.hideShow('daily-schedules-report', true, 'mat-tab-header');
    Utils.hideShow('daily-schedules-report', true, 'mat-mdc-tab-labels');
    
    //generate pdf
    this.generatePdf('daily-schedules-report', fileName);

  }

  showReportControls(): void {
    //user schedules
    Utils.hideShow('user-schedules-inputs', false);

    //daily schedules
    Utils.hideShow('daily-schedules-report', false, 'mat-tab-header');
    Utils.hideShow('calendar-date-picker', false);
    Utils.hideShow('refresh-calendar-container', false);
    Utils.hideShow('calendar-filters-outer', false);
  }

  reportChange(selectedReport: any): void {
    this.showReportResults = false;
  }

  //------------------------------------
  // User Schedules
  //------------------------------------
  generateUserSchedulesReport(): void {
    if (!this.selectedDateRange.value.end || this.selectedUser.length < 1) {
      return;
    }

    this.showReportResults = true;

    let startDate: string = Utils.formatDateOnlyToString(this.selectedDateRange.value.start, true,true,true) || '';
    let endDate: string = Utils.formatDateOnlyToString(this.selectedDateRange.value.end, true,true,true) || '';

    this.userSchedulesService.getAllUserSchedulesByRange(startDate, endDate).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let userSchedulesReport: ISchedule[] = <ISchedule[]>response.Subject;

          this.userSchedulesReport = userSchedulesReport.filter(x => x.dempoid == this.selectedUser);

          if (this.userSchedulesReport.length < 1) {
            this.userSchedulesReport = [{ displayName: 'No schedules for this date range...' } as ISchedule];
          } else {
            //use new Date() on each schedule to convert from the stored UTC time to the local browser time
            //also, set the string formatted start/end times for display purposes (this avoids having to format the time everywhere it's displayed)
            for (var i = 0; i < this.userSchedulesReport.length; i++) {
              //update the display name and user id
              let user: IUserMin | undefined = this.activeUsers.find(x => x.dempoid == this.userSchedulesReport[i].dempoid);
              if (user) {
                this.userSchedulesReport[i].fname = user.fname;
                this.userSchedulesReport[i].lname = user.lname;
                this.userSchedulesReport[i].displayName = user.displayName;
                this.userSchedulesReport[i].userid = user.userid;
              }
              this.userSchedulesReport[i].dayOfWeekText = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'][new Date(this.userSchedulesReport[i].startdatetime || '').getDay()];
              this.userSchedulesReport[i].startdatetime = new Date(this.userSchedulesReport[i].startdatetime || '');
              this.userSchedulesReport[i].enddatetime = new Date(this.userSchedulesReport[i].enddatetime || '');
              this.userSchedulesReport[i].startTime = Utils.formatDateToTimeString(this.userSchedulesReport[i].startdatetime, true) || '';
              this.userSchedulesReport[i].endTime = Utils.formatDateToTimeString(this.userSchedulesReport[i].enddatetime, true) || '';
            }
          }
        }

      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }

  //------------------------------------
  // Daily Schedules
  //------------------------------------
  generateDailySchedulesReport(): void {

  }

  formatDateOnlyToString(dateToFormat: Date): string {
    if (!dateToFormat) {
      return '';
    }

    return Utils.formatDateOnlyToString(dateToFormat) || '';
  }

  //------------------------------------
  // Project Totals
  //------------------------------------
  generateProjectTotalsReport(): void {
    this.projectsService.getProjectTotalsReport(this.projectTotalsWeekStartAndEnd.weekStart, this.projectTotalsWeekStartAndEnd.weekEnd).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {

          this.projectTotalsReport = <IProjectTotalsReport>(response.Subject);

          this.projectTotalsReportFlat = [];

          if (this.projectTotalsReport) {
            if (this.projectTotalsReport.projectTotals.length < 1) {
              this.projectTotalsReportFlat = [{ projectColumn: 'No projects scheduled for this week...' } as IProjectTotalsReportFlat];
            } else {
              //map unique projects
              const uniqueValue = (value: any, index: any, self: any) => {
                return self.indexOf(value) === index
              }
              this.projectTotalsProjects = this.projectTotalsReport.projectTotals.map(x => x.projectName).filter(uniqueValue);

              //-----------------
              // flatten
              //-----------------
              //for each project
              for (var x = 0; x < this.projectTotalsProjects.length; x++) {
                let pt: IProjectTotals[] = this.projectTotalsReport.projectTotals.filter(z => z.projectName == this.projectTotalsProjects[x]);
                let weekTotal: number = 0;

                if (pt) {
                  let projectTotalsFlat: IProjectTotalsReportFlat = {} as IProjectTotalsReportFlat;

                  projectTotalsFlat.projectColumn = this.projectTotalsProjects[x];

                  let ptf: any = projectTotalsFlat;
                  for (var i = 1; i < 8; i++) {
                    let ptForDay: IProjectTotals | undefined = pt.find(x => x.dayNumber == (i));
                    if (ptForDay) {
                      ptf['Column' + (i)] = ptForDay.totalHours;
                    } else {
                      ptf['Column' + (i)] = 0;
                    }

                    weekTotal = weekTotal + ptf['Column' + (i)];

                  }

                  projectTotalsFlat = ptf;

                  projectTotalsFlat['totalColumn'] = weekTotal;

                  this.projectTotalsReportFlat.push(projectTotalsFlat);
                }

              }

              //totals at bottom
              let projectTotalsFlat: IProjectTotalsReportFlat = {} as IProjectTotalsReportFlat;
              projectTotalsFlat.projectColumn = 'TOTAL';
              let weekTotal: number = 0;

              let ptf: any = projectTotalsFlat;
              for (var i = 1; i < 8; i++) {
                let ptForDay: IProjectTotalsSummed | undefined = this.projectTotalsReport.projectTotalsSummed.find(x => x.daynumber == (i));
                if (ptForDay) {
                  ptf['Column' + (i)] = ptForDay.total;
                } else {
                  ptf['Column' + (i)] = 0;
                }

                weekTotal = weekTotal + ptf['Column' + (i)];

              }

              projectTotalsFlat = ptf;

              projectTotalsFlat['totalColumn'] = weekTotal;

              this.projectTotalsReportFlat.push(projectTotalsFlat);

              //-----------------
              // END flatten
              //-----------------
            }

          }
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  projectTotalsDateChange(): void {
    this.projectTotalsWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date(this.projectTotalsDate.value));

    this.setProjectTotalsDaysOfWeek();
    this.generateProjectTotalsReport();

  }

  setProjectTotalsDaysOfWeek(): void {
    this.projectTotalsDaysOfWeek = [];

    for (var x = 0; x < 7; x++) {
      let xDate: Date = new Date(this.projectTotalsWeekStartAndEnd.weekStart);
      xDate.setDate(xDate.getDate() + x);
      this.projectTotalsDaysOfWeek[x] = xDate;
    }
  }

  addWeeksToProjectTotals(numWeeks: number): void {
    this.projectTotalsDate.setValue(this.addWeeksToSelectedDate(new Date(this.projectTotalsDate.value), numWeeks));
    this.projectTotalsWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date(this.projectTotalsDate.value));
    this.projectTotalsDateRange = new FormGroup({
      start: new FormControl(new Date(this.projectTotalsWeekStartAndEnd.weekStart)),
      end: new FormControl(new Date(this.projectTotalsWeekStartAndEnd.weekEnd)),
    });

    this.projectTotalsDateChange();

  }

  //add (or subract with a negative number) a week to the anchor date
  public addWeeksToSelectedDate(dateToAddTo: Date, weeks: number): Date {
    weeks = weeks * 7;
    let selectedDt: Date = new Date(dateToAddTo);
    selectedDt.setDate(selectedDt.getDate() + weeks);
    return selectedDt;
  }

}
