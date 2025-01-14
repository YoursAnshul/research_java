import { Component, HostListener, OnInit } from '@angular/core';
import { Utils } from '../../classes/utils';
import { IAuthenticatedUser, ICoreHours, IForecastHours, IProjectForecastHours, IProjectMin, IUserCoreHours, IUserMin } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { GlobalsService } from '../../services/globals/globals.service';
import { LogsService } from '../../services/logs/logs.service';
import { ProjectsService } from '../../services/projects/projects.service';
import { UsersService } from '../../services/users/users.service';

@Component({
  selector: 'app-forecasting',
  templateUrl: './forecasting.component.html',
  styleUrls: ['./forecasting.component.css']
})
export class ForecastingComponent implements OnInit {
  @HostListener('window:beforeunload') onBeforeUnload(e: any) {

    if (this.changed) {
      e.preventDefault();
      e.returnValue = '';
    }

  }

  authenticatedUser!: IAuthenticatedUser;

  //forecasting
  forecastHours: IForecastHours[] = [];
  projectForecastHours: IProjectForecastHours[] = [];
  activeProjects!: IProjectMin[];
  forecastMonths: string[] = [];
  forecastHourTotals: number[] = [];
  changed: boolean = false;

  //core hours
  coreHours: ICoreHours[] = [];
  userCoreHours: IUserCoreHours[] = [];
  allUsers!: IUserMin[];
  coreHoursMonths: string[] = [];
  coreHourTotals: number[] = [];
  coreHoursChanged: boolean = false;
  errorMessage!: string;

  constructor(private globalsService: GlobalsService,
    private projectsService: ProjectsService,
    private usersService: UsersService,
    private authenticationService: AuthenticationService,
    private logsService: LogsService) {

    //get active projects
    this.projectsService.allProjectsMin.subscribe(
      activeProjects => {
        this.activeProjects = activeProjects.filter(x => (x.active && x.projectType !== 'Administrative' && (x.projectDisplayId.split('|').includes('3'))));

        //get forecasting hours
        this.projectsService.getAllForecasting().subscribe(
          response => {
            if ((response.Status || '').toUpperCase() == 'SUCCESS') {
              this.forecastHours = <IForecastHours[]>response.Subject;

              if (this.forecastHours.length > 0) {
                this.projectForecastHours = [];
                //setup forecasting hours with project objects
                for (var i = 0; i < this.forecastHours.length; i++) {
                  let projectMinInstance: IProjectMin | undefined = this.activeProjects.find(x => x.projectID == this.forecastHours[i].projectId);
                  if (!projectMinInstance) {
                    continue;
                  }

                  let projectForecastInstance: IProjectForecastHours = {
                    forecastHours: this.forecastHours[i],
                    project: projectMinInstance
                  };

                  this.projectForecastHours.push(projectForecastInstance);
                }

                //sort forecast hours by project name
                this.projectForecastHours.sort((x, y) => {
                  return ((x.project.projectName > y.project.projectName) ? 1 : -1);
                });

                //set forecast months array
                let currentDate: Date = new Date();
                this.forecastMonths[0] = Utils.formatDateToMonthYear(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1), true, true);
                this.forecastMonths[1] = Utils.formatDateToMonthYear(new Date(currentDate.getFullYear(), currentDate.getMonth(), 1), true, true);
                for (var i = 1; i < 13; i++) {
                  this.forecastMonths[i + 1] = Utils.formatDateToMonthYear(new Date(currentDate.getFullYear(), currentDate.getMonth() + i, 1), true, true);
                }

                //set forecast hour totals
                this.setForecastHourTotals();

              }
            }
          }
        );


        //get all users
        this.usersService.allUsersMin.subscribe(
          allUsers => {
            this.allUsers = allUsers;


            //get core hours
            this.usersService.getAllUserCoreHours().subscribe(
              response => {
                if ((response.Status || '').toUpperCase() == 'SUCCESS') {
                  this.coreHours = <ICoreHours[]>response.Subject;

                  if (this.coreHours.length > 0) {
                    this.userCoreHours = [];
                    //setup forecasting hours with project objects
                    for (var i = 0; i < this.coreHours.length; i++) {
                      let userMinInstance: IUserMin | undefined = this.allUsers.find(x => x.dempoid == this.coreHours[i].dempoid);
                      if (!userMinInstance) {
                        continue;
                      }

                      let userCoreHoursInstance: IUserCoreHours = {
                        coreHours: this.coreHours[i],
                        user: userMinInstance
                      };

                      this.userCoreHours.push(userCoreHoursInstance);
                    }

                    //sort core hours by user
                    this.userCoreHours.sort((x, y) => {
                      return ((x.user.dempoid > y.user.dempoid) ? 1 : -1);
                    });

                    //set core hours months array
                    let currentDate: Date = new Date();
                    this.coreHoursMonths[0] = Utils.formatDateToMonthYear(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1), true, true);
                    this.coreHoursMonths[1] = Utils.formatDateToMonthYear(new Date(currentDate.getFullYear(), currentDate.getMonth(), 1), true, true);
                    for (var i = 1; i < 13; i++) {
                      this.coreHoursMonths[i + 1] = Utils.formatDateToMonthYear(new Date(currentDate.getFullYear(), currentDate.getMonth() + i, 1), true, true);
                    }

                    //set core hour totals
                    this.setCoreHoursTotals();

                  }

                  //sort by display name
                  this.userCoreHours.sort((a, b) => (a.user.displayName || '').localeCompare((b.user.displayName || '')));

                }
              }
            );


          }
        );


      }
    );

  }

  ngOnInit(): void {
    //set current page for navigation menu to track
    this.globalsService.selectedPage.next('forecasting');

    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;
      }
    );

  }

  setChanged(): void {
    this.changed = true;
    this.globalsService.currentChanges.next(true);
    this.setForecastHourTotals();
  }

  setChangedCoreHours(): void {
    this.coreHoursChanged = true;
    this.globalsService.currentChanges.next(true);
    this.setCoreHoursTotals();
  }

  nvlHours(item: any, index: number): void {
    let newValue: any = item['Forecasthours' + index];
    if (newValue) {
      if (newValue.replace(/\s/g, '') == '') {
        item['Forecasthours' + index] = null;
      }
    } else {
        item['Forecasthours' + index] = null;
    }
  }

  //function to return list of numbers from 0 to n-1
  numSequence(n: number): Array<number> {
    return Array(n);
  }

  setForecastHourTotals(): void {

    this.forecastHourTotals = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];

    for (var i = 0; i < this.projectForecastHours.length; i++) {
      for (var j = 0; j < 14; j++) {
        let fh: any = this.projectForecastHours[i].forecastHours;
        let hoursInstance: number = parseInt(fh['forecastHours' + (j + 1)]);
        if (!hoursInstance) {
          hoursInstance = 0;
        }
        this.forecastHourTotals[j] = (this.forecastHourTotals[j] ? (hoursInstance + this.forecastHourTotals[j]) : hoursInstance);
      }
    }

  }

  saveForecasting(): void {
    this.changed = false;
    this.globalsService.currentChanges.next(false);

    this.projectsService.saveForecasting(this.forecastHours).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Forecasting saved successfully', ['OK']));
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save forecasting', ['OK']));
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save forecasting:<br />' + this.errorMessage, ['OK']));
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }

    );
  }

  setCoreHoursTotals(): void {

    this.coreHourTotals = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];

    for (var i = 0; i < this.userCoreHours.length; i++) {
      for (var j = 0; j < 14; j++) {
        let ch: any = this.userCoreHours[i].coreHours;
        let hoursInstance: number = parseInt(ch['coreHours' + (j + 1)]);
        if (!hoursInstance) {
          hoursInstance = 0;
        }
        this.coreHourTotals[j] = (this.coreHourTotals[j] ? (hoursInstance + this.coreHourTotals[j]) : hoursInstance);
      }
    }

  }

  saveCoreHours(): void {
    this.changed = false;
    this.globalsService.currentChanges.next(false);

    this.usersService.saveUserCoreHours(this.coreHours).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Core hours saved successfully', ['OK']));
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save core hours', ['OK']));
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save core hours:<br />' + this.errorMessage, ['OK']));
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }

    );
  }

  numberRestrict(event: any) {
    var charCode = (event.which) ? event.which : event.keyCode;
    // Only Numbers 0-9
    if ((charCode < 48 || charCode > 57)) {
      event.preventDefault();
      return false;
    } else {
      return true;
    }
  }

  formatDateToShortMonthYear(dateToFormat: Date): string {

    if (!dateToFormat) {
      return '';
    }

    return Utils.formatDateToMonthYear(dateToFormat, true, true);

  }

}
