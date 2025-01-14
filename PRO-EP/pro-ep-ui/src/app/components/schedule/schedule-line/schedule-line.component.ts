import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Utils } from '../../../classes/utils';
import { IActionButton, IAuthenticatedUser, IBlockOutDate, IProjectGroup, IProjectMin, ISchedule, IScheduleMin, ITimeCode, IUserMin, IUserSchedule } from '../../../interfaces/interfaces';
import { GlobalsService } from '../../../services/globals/globals.service';
import { LogsService } from '../../../services/logs/logs.service';
import { ProjectsService } from '../../../services/projects/projects.service';
import { UsersService } from '../../../services/users/users.service';
import { UserSchedulesService } from '../../../services/userSchedules/user-schedules.service';

@Component({
  selector: 'app-schedule-line',
  templateUrl: './schedule-line.component.html',
  styleUrls: ['./schedule-line.component.css']
})
export class ScheduleLineComponent implements OnInit {
  //inputs
  @Input() schedule!: ISchedule;
  @Input() allUsers!: IUserMin[];
  @Input() allProjects!: IProjectMin[];
  @Input() timeCodes!: ITimeCode[];
  @Input() authenticatedUser!: IAuthenticatedUser;
  @Input() readOnly!: boolean;
  @Input() unlockDate!: Date;
  @Input() blockOutDates!: IBlockOutDate[];
  @Input() schedulingLevel!: Number;

  //outputs
  @Output() saveEvent = new EventEmitter<ISchedule>();
  @Output() deleteEvent = new EventEmitter<ISchedule>();
  @Output() addEvent = new EventEmitter<ISchedule>();
  @Output() changeEvent = new EventEmitter<ISchedule>();

  trainedOnProjects!: IProjectMin[];
  trainedOnProjectGroups: IProjectGroup[] = [];
  scheduleMin!: IScheduleMin;
  errorMessage!: string;

  constructor(private userSchedulesService: UserSchedulesService,
    private projectsService: ProjectsService,
    private usersService: UsersService,
    private globalsService: GlobalsService,
    private logsService: LogsService) {
    if (this.allUsers && this.allProjects) {
      this.setTrainedOnProjects();
    }
  }

  ngOnInit(): void {
    this.schedule.invalidFields = [];

    if (this.schedule.addTab) {
      this.changeEvent.emit(this.schedule);
    }

  }

  ngOnChanges(): void {
    if (!this.trainedOnProjects && this.allUsers.length > 0 && this.allProjects.length > 0) {
      this.setTrainedOnProjects();
    }

  }

  //filter projects to user trained on projects
  setTrainedOnProjects(user: IUserMin | null = null): void {
    if (!user) {
      user = this.allUsers.find(x => x.dempoid == this.schedule.dempoid) as IUserMin;
    }

    this.trainedOnProjects = this.allProjects.filter(x => ((x.projectType == 'Administrative') || (Utils.pipeStringToArray((user ? user.trainedon : '')).includes(x.projectID.toString()) && x.active && (x.projectDisplayId.split('|').includes('2')))));

    let projectGroup: IProjectGroup = {
      name: 'Projects',
      projects: this.trainedOnProjects.filter(x => x.projectType !== 'Administrative')
    };

    let adminGroup: IProjectGroup = {
      name: 'Admin',
      projects: this.trainedOnProjects.filter(x => x.projectType == 'Administrative')
    };

    this.trainedOnProjectGroups = [projectGroup, adminGroup];

  }

  //----------------------------------
  //state change functions
  //----------------------------------
  setChanged(): void {
    this.schedule.changed = true;
    this.schedule.validationMessages = [];

    //update start and end datetime values
    if (this.schedule.startTime && this.schedule.startTime !== '0') {
      this.schedule.startdatetime = Utils.buildDateTime(this.schedule.startdatetime, this.schedule.startTime);
    }
    if (this.schedule.endTime && this.schedule.endTime !== '0') {
      this.schedule.enddatetime = Utils.buildDateTime(this.schedule.startdatetime, this.schedule.endTime);
    }

    //update the project id
    let projectIds: number[] = this.allProjects.filter(x => x.projectName == this.schedule.projectName).map(x => x.projectID);
    if (projectIds.length > 0) {
      this.schedule.projectid = projectIds[0];
    }

    //update scheduledHours
    if (this.schedule.startdatetime
      && this.schedule.enddatetime) {
      let startHours: number = (((this.schedule.startdatetime.getHours() * 60) + this.schedule.startdatetime.getMinutes()) / 60);
      let endHours: number = (((this.schedule.enddatetime.getHours() * 60) + this.schedule.enddatetime.getMinutes()) / 60);
      this.schedule.scheduledHours = endHours - startHours;
    }

    //update the display name and user id
    let user: IUserMin | undefined = this.allUsers.find(x => x.dempoid == this.schedule.dempoid);
    if (user) {
      this.schedule.fname = user.fname;
      this.schedule.lname = user.lname;
      this.schedule.displayName = user.displayName || '';
      this.schedule.userid = user.userid;
      this.setTrainedOnProjects(user);
    }

    //this.validateRequiredFields();
    //this.businessRulesValidation();

    this.changeEvent.emit(this.schedule);

  }

  setSaved(): void {
    this.schedule.saved = true;
  }

  setMarkedForDeletion(): void {
    this.schedule.markedForDeletion = true;
    this.changeEvent.emit(this.schedule);
  }

  undoMarkForDeletion(): void {
    this.schedule.markedForDeletion = false;
    this.changeEvent.emit(this.schedule);
  }

  addSchedule(sourceSchedule: ISchedule): void {
    this.addEvent.emit(sourceSchedule);
  }

  //----------------------------------
  //database actions
  //----------------------------------

  //save a single schedule
  saveSchedule(): void {
    Utils.validateStartEndDateTime(this.schedule);

    this.scheduleMin = {
      dempoid: this.schedule.dempoid,
      projectid: this.schedule.projectid,
      scheduledate: Utils.formatDateOnly(this.schedule.startdatetime || new Date()) || undefined,
      startdatetime: this.schedule.startdatetime,
      enddatetime: this.schedule.enddatetime,
      comments: this.schedule.comments,
      preschedulekey: this.schedule.preschedulekey,
      entryBy: (this.schedule.entryBy ? this.schedule.entryBy : this.authenticatedUser.netID),
      entryDt: (this.schedule.entryDt ? this.schedule.entryDt : new Date()),
      modBy: this.authenticatedUser.netID,
      modDt: new Date(),
      machineName: 'NA',
      status: 0,
    };

    //call delete schedule if committing a delete
    if (this.schedule.markedForDeletion) {

      let deleteFunction: Function = (): void => {
        this.deleteSchedule();
      }

      let confirmButton: IActionButton = {
        label: 'Delete Schedule',
        callbackFunction: deleteFunction
      }

      let cancelButton: IActionButton = {
        label: 'Cancel',
        callbackFunction: (): void => { }
      }

      this.globalsService.displayPopupMessage(Utils.generatePopupMessageWithCallbacks('Confirm Delete', '<p>Are you sure you want to delete this schedule?</p>', [confirmButton, cancelButton], true));

      return;
    }

    //alert and exit if trying to save an invalid schedule
    if (this.schedule.invalid) {
      this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Schedule failed validation and cannot be saved. Please review the fields in red.', ['OK']));
      return;
    }

    let schedules: IScheduleMin[] = [this.scheduleMin];
    this.userSchedulesService.saveSchedules(schedules).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          //set new preschedulekey if the schedule is new
          if (!this.schedule.preschedulekey || this.schedule.preschedulekey == 0) {
            this.schedule.justAdded = true;
            let subject: IScheduleMin[] = <IScheduleMin[]>response.Subject;
            if (subject) {
              this.schedule.preschedulekey = subject[0].preschedulekey;
            }
          }
          //set saved and changed flags
          this.schedule.saved = true;
          this.schedule.changed = false;
          this.schedule.addInitial = false;

          //emit the schedule for the save event to bubble up to the schedule component
          this.saveEvent.emit(this.schedule);
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to save schedule', ['OK']));
        }
      },
        error => {
          this.errorMessage = <string>(error.message);
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to save schedule:<br />' + this.errorMessage, ['OK']));
          this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }

  //save a single schedule
  deleteSchedule(): void {
    if (this.scheduleMin.preschedulekey == 0) {
      this.deleteEvent.emit(this.schedule);
      return;
    }

    let schedules: IScheduleMin[] = [this.scheduleMin];
    this.userSchedulesService.deleteSchedules(schedules).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          //set saved, changed, and marked for deletion flags
          this.schedule.saved = false;
          this.schedule.changed = false;
          this.schedule.markedForDeletion = false;
          //emit the schedule for the delete event to bubble up to the schedule component
          this.deleteEvent.emit(this.schedule);
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to delete schedule', ['OK']));
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to delete schedule:<br />' + this.errorMessage, ['OK']));
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }

  //----------------------------------
  //custom style functions
  //----------------------------------
  saveScheduleStyle() {
    if (this.schedule.changed || this.schedule.markedForDeletion || (this.schedule.addTab && !this.schedule.saved)) {
      return {
        'display': 'initial',
      };
    } else {
      return {
        'display': 'none',
      };
    }
  }

  savedCheckmarkStyle() {
    if (this.schedule.saved && (!this.schedule.markedForDeletion && !this.schedule.changed)) {
      return {
        'display': 'initial',
      };
    } else {
      return {
        'display': 'none',
      };
    }
  }

  noSaveOrCheckmarkStyle() {
    if (!this.schedule.changed && !this.schedule.saved && !this.schedule.markedForDeletion && !(this.schedule.addTab && !this.schedule.saved)) {
      return {
        'display': 'inline-block',
      };
    } else {
      return {
        'display': 'none',
      };
    }
  }

  deleteButtonStyle() {
    if (!this.schedule.markedForDeletion && !this.schedule.addInitial) {
      return {
        'display': 'initial',
      };
    } else {
      return {
        'display': 'none',
      };
    }
  }

  editSectionStyle() {
    if (!this.schedule.markedForDeletion) {
      return {
        'display': 'initial',
      };
    } else {
      return {
        'display': 'none',
      };
    }
  }

  readOnlySectionStyle() {
    if (this.schedule.markedForDeletion) {
      return {
        'display': 'initial',
      };
    } else {
      return {
        'display': 'none',
      };
    }
  }

  //check if the specified time code value is before 1pm and the schedule date is a weekday and if the schedule is for a level 1 interviewer - PROEP-1041
  isWeekdayMorning(timeCodeValue: number): boolean {
    if (!this.schedule.startdatetime)
      return false;

    let dayNumber: number = this.schedule.startdatetime.getDay();
    if (dayNumber > 0 && dayNumber < 6
      && this.schedulingLevel == 1 && timeCodeValue < 27)
      return true;
    else
      return false;
  }

  //proxy for the Utils date format function
  formatDate(dateToFormat: Date | undefined): string | null {
    if (!dateToFormat) {
      return null;
    }

    return Utils.formatDateOnlyToString(dateToFormat);
  }

  //returns the day of the week for the current schedule
  dayOfTheWeek(schedule: ISchedule): string {
    //let daysOfTheWeek: string[] = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    let daysOfTheWeek: string[] = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    if (!schedule.startdatetime)
      return '--';

    return daysOfTheWeek[schedule.startdatetime.getDay()];
  }

}
