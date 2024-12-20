import { Component, HostListener, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { scheduled } from 'rxjs';
import { Utils } from '../../classes/utils';
import { IActionButton, IAdminOptionsVariable, IAnyFilter, IAuthenticatedUser, IBlockOutDate, IDateRange, IDropDownValue, IFormFieldVariable, IProjectMin, IRequest, ISchedule, IScheduleMin, ITimeCode, IUserSchedule, IUserTrainedOn, IValidationMessage, IWeekStartAndEnd, IWeekStartAndEndStrings} from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { GlobalsService } from '../../services/globals/globals.service';
import { LogsService } from '../../services/logs/logs.service';
import { RequestsService } from '../../services/requests/requests.service';
import { ProjectsService } from '../../services/projects/projects.service';
import { UsersService } from '../../services/users/users.service';
import { UserSchedulesService } from '../../services/userSchedules/user-schedules.service';
import { User } from '../../models/data/user';

@Component({
  selector: 'app-schedule',
  templateUrl: './schedule.component.html',
  styleUrls: ['./schedule.component.css']
})
export class ScheduleComponent implements OnInit {
  @HostListener('window:beforeunload') onBeforeUnload(e: any) {

    let changedSchedules: ISchedule[] = this.userSchedulesMonth.filter(x => (x.changed) || (x.markedForDeletion));

    if (changedSchedules.length > 0) {
      if(e){
        e.preventDefault();
        e.returnValue = '';
      }
    }
  }
  showPopupMessage: boolean = false;
  showBlackFilter: boolean = false;
  showScheduleBlackFilter: boolean = false;
  showHoverMessage: boolean = false;
  errorMessage!: string;
  selectedWeekStartAndEnd!: IWeekStartAndEnd;
  userSchedulesMonth: ISchedule[] = [];
  scheduleCache: ISchedule[] = [];
  filteredUserSchedulesMonth: ISchedule[] = [];
  filteredUserSchedulesDay: ISchedule[] = [];
  filteredUserSchedulesWeek: ISchedule[] = [];
  filteredUserSchedulesCustom: ISchedule[] = [];
  addedSchedules: ISchedule[] = [];
  initialSchedule: ISchedule = {} as ISchedule;
  allUsers: User[] = [];
  allProjects: IProjectMin[] = [];
  filteredUsers: User[] = [];
  filteredProjects: IProjectMin[] = [];
  availableUsers: User[] = [];
  availableProjects: IProjectMin[] = [];
  languages!: IDropDownValue[];
  selectedDate: Date;
  previousSelectedDate!: Date;
  selectedDateFC: FormControl;
  selectedDateRange!: FormGroup;
  selectedCustomRange!: FormGroup;
  timeCodes!: ITimeCode[];
  authenticatedUser!: IAuthenticatedUser;
  contextNetId!: string;
  contextProjectName!: string;
  scheduleTabIndex: number = 0;
  blankScheduleAdded: boolean = false;
  validationMessages: IValidationMessage[] = [];
  validationMessagesExpanded: boolean = false;
  validationMessagesChecked: boolean = false;
  blockOutDates: IBlockOutDate[] = [];
  lockDate!: Date;
  unlockDate!: Date;
  currentUser: User = {} as User;

  //validation
  addTabInvalid: boolean = false;
  dayTabInvalid: boolean = false;
  weekTabInvalid: boolean = false;
  monthTabInvalid: boolean = false;
  customTabInvalid: boolean = false;

  //schedule pagination
  pageSizeOptions = [5, 10, 20, 50, 100];
  //added
  pageIndexAdded = 0;
  pageSizeAdded = 10;
  pagedUsersAddedLength = 0;
  pagedUserSchedulesAdded: ISchedule[] = [];
  //day
  pageIndexDay = 0;
  pageSizeDay = 10;
  pagedUsersDayLength = 0;
  pagedUserSchedulesDay: ISchedule[] = [];
  //week
  pageIndexWeek = 0;
  pageSizeWeek = 10;
  pagedUsersWeekLength = 0;
  pagedUserSchedulesWeek: ISchedule[] = [];
  //month
  pageIndexMonth = 0;
  pageSizeMonth = 10;
  pagedUsersMonthLength = 0;
  pagedUserSchedulesMonth: ISchedule[] = [];
  //custom
  pageIndexCustom = 0;
  pageSizeCustom = 10;
  pagedUsersCustomLength = 0;
  pagedUserSchedulesCustom: ISchedule[] = [];


  currentTab: string = 'Add';

  //filters
  userFilterFC: FormControl = new FormControl(['0']);
  projectFilterFC: FormControl = new FormControl(['0']);
  anyUserToggle: boolean = true;
  anyProjectToggle: boolean = true;

  constructor(private userSchedulesService: UserSchedulesService,
              private usersService: UsersService,
              private projectsService: ProjectsService,
              private globalsService: GlobalsService,
              private authenticationService: AuthenticationService,
              private requestsService: RequestsService,
              private configurationService: ConfigurationService,
              private logsService: LogsService) {

    //set date and week defaults
    this.selectedDate = new Date();

    //this.userSchedulesService.selectedDate.next(this.selectedDate);
    this.selectedDateFC = new FormControl(this.selectedDate.toISOString());
    this.setSelectedWeek();

    //subscribe to context net Id
    this.globalsService.contextNetId.subscribe(
      contextNetId => {
        this.contextNetId = contextNetId || '';
        this.setInitialScheduleDefaults();
      }
    );

    //subscribe to context project name
    this.globalsService.contextProjectName.subscribe(
      contextProjectName => {
        this.contextProjectName = contextProjectName || '';
        this.setInitialScheduleDefaults();
      }
    );

    this.globalsService.showPopupMessage.subscribe(
      showPopupMessage => {
        this.showPopupMessage = showPopupMessage;
      }
    );

    this.globalsService.showHoverMessage.subscribe(
      showHoverMessage => {
        this.showHoverMessage = showHoverMessage;
      }
    );

;

    //subscribe to schedule tab index
    this.globalsService.scheduleTabIndex.subscribe(
      scheduleTabIndex => {
        this.scheduleTabIndex = scheduleTabIndex;

        //set the current tab, based on the schedule tab index (to make sure calendar controls are loaded)
        switch (this.scheduleTabIndex) {
          case 1:
            this.currentTab = 'Day';
            break;
          case 2:
            this.currentTab = 'Week';
            break;
          case 3:
            this.currentTab = 'Month';
            break;
          case 4:
            this.currentTab = 'Custom';
            break;
        }

      }
    );

    //subscribe to show scheduler boolean
    this.globalsService.showScheduler.subscribe(
      showScheduler => {
        if (showScheduler) {
          let blankSchedule: ISchedule | undefined = this.addedSchedules.find(x => x.addInitial);

          if (this.contextNetId) {
            this.userFilterFC.setValue([this.contextNetId]);
            if (blankSchedule) {
              blankSchedule.dempoid = this.contextNetId;
            }
          } else {
            this.userFilterFC.setValue(['0']);
          }

          if (this.contextProjectName) {
            this.projectFilterFC.setValue([this.contextProjectName]);
            if (blankSchedule) {
              blankSchedule.projectName = this.contextProjectName;
            }
          } else {
            this.projectFilterFC.setValue(['0']);
          }

          this.applyFilters();

        }
      }
    );

    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {

        this.authenticatedUser = authenticatedUser;

        //try to set current user
        if (this.allUsers.length > 0) {
          this.currentUser = this.allUsers.find(x => x.dempoid == this.authenticatedUser.netID) as User;
        }

        //restrict schedules shown if user is interviewer-only
        if (this.allUsers) {
          if (this.authenticatedUser.interviewer && !(this.authenticatedUser.admin || this.authenticatedUser.resourceGroup)) {
            this.allUsers = this.allUsers.filter(x => x.dempoid == this.authenticatedUser.netID);
            if (this.userSchedulesMonth.length > 0) {
              this.filteredUserSchedulesMonth = this.filteredUserSchedulesMonth.filter(x => x.dempoid == this.authenticatedUser.netID);
            }
          }
        }

        this.filterProjects();

        this.setInitialScheduleDefaults();

        //this.getValidationMessages();

      }
    );

    //subscribe to users
    this.usersService.allUsersMin.subscribe(
      allUsers => {

        this.allUsers = allUsers.filter(x => x.active);

        //try to set current user
        if (this.authenticatedUser) {
          this.currentUser = this.allUsers.find(x => x.dempoid == this.authenticatedUser.netID) as User;
        }

        //restrict schedules shown if user is interviewer-only
        if (this.authenticatedUser) {
          if (this.authenticatedUser.interviewer && !(this.authenticatedUser.admin || this.authenticatedUser.resourceGroup)) {
            this.allUsers = this.allUsers.filter(x => x.dempoid == this.authenticatedUser.netID);
            if (this.userSchedulesMonth.length > 0) {
              this.filteredUserSchedulesMonth = this.filteredUserSchedulesMonth.filter(x => x.dempoid == this.authenticatedUser.netID);
            }
          }
        }

        //filter available users
        if (this.userSchedulesMonth.length > 0) {
          this.filterUsers();
        }

        this.setInitialScheduleDefaults();
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

    //subscribe to schedule cache
    this.userSchedulesService.scheduleCache.subscribe(
      scheduleCache => {
        this.scheduleCache = scheduleCache;
      }
    );
    //subscribe to user schedules subject
    this.userSchedulesService.userSchedules.subscribe(
      userSchedules => {
        this.userSchedulesMonth = userSchedules;

        //initial schedule
        if (this.userSchedulesMonth.filter(x => x.addInitial).length < 1) {
          this.initialSchedule = {} as ISchedule;
          this.initialSchedule.addInitial = true;
          this.initialSchedule.addTab = true;
          this.userSchedulesMonth = [this.initialSchedule, ...this.userSchedulesMonth];
        }

        this.applyFilters();

        //filter available users and projects based on filtered schedules
        //users
        if (this.allUsers.length > 0) {
          this.filterUsers();
        }

        //projects
        if (this.allProjects.length > 0) {
          this.filterProjects();
        }

        this.setInitialScheduleDefaults();
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

    //subscribe to projects
    this.projectsService.allProjectsMin.subscribe(
      allProjects => {
        this.allProjects = allProjects.filter(x => x.active);

        //validation schedules
        if (this.validationMessages) {
          for (var x = 0; x < this.validationMessages.length; x++) {
            for (var i = 0; i < (this.validationMessages[x].schedules || []).length; i++) {
              let projectId: number = (this.validationMessages[x].schedules || [])[i].projectid as number;
              let scheduleProject: IProjectMin | undefined = this.allProjects.find(x => x.projectID == projectId);
              if (scheduleProject) {
                (this.validationMessages[x].schedules || [])[i].projectName = scheduleProject.projectName;
              }
            }
          }
        }


        //filter available projects
        if (this.userSchedulesMonth.length > 0) {
          this.filterProjects();
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

    //subscribe to the selected date
    this.userSchedulesService.selectedDate.subscribe(
      selectedDate => {

        this.selectedDate = selectedDate;
        this.setInitialScheduleDefaults();

        this.selectedDateFC = new FormControl(selectedDate.toISOString());

        this.setSelectedWeek();

        let previousDateIsSame: boolean = false;

        if (Utils.formatDateOnlyToString(this.selectedDate) == Utils.formatDateOnlyToString(this.previousSelectedDate)) {
          previousDateIsSame = true;
        }

        this.previousSelectedDate = new Date(this.selectedDate);

        if (!previousDateIsSame) {
          //call validate schedules
          this.userSchedulesService.validateSchedules([{ dempoId: this.authenticatedUser.netID, inMonth: new Date(this.selectedDate) } as IValidationMessage]).subscribe(
            response => {
              if ((response.Status || '').toUpperCase() == 'SUCCESS') {
                try {
                  this.getValidationMessages();
                  //this.validationMessages = <IValidationMessage[]>(response.Subject).filter(x => x.DempoId == this.authenticatedUser.NetID);
                } catch (ex) {
                  console.log(ex);
                }
              }
            },
            error => {
              this.errorMessage = <string>(error.message);
              this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
            }
          )
        }

        //set month start and end
        if (this.selectedCustomRange) {
          if (!this.selectedCustomRange.value.start) {
            let monthStart: Date = new Date(this.selectedDate);
            monthStart.setDate(1);
            let monthEnd: Date = new Date(this.selectedDate);
            monthEnd.setMonth(monthEnd.getMonth() + 1);
            monthEnd.setDate(0);

            this.selectedCustomRange = new FormGroup({
              start: new FormControl(monthStart),
              end: new FormControl(monthEnd),
            });

            this.setInitialScheduleDefaults();
          }
        }

        this.applyFilters();
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

    //subscribe to timecodes
    this.userSchedulesService.timeCodes.subscribe(
      timeCodes => {
        timeCodes = timeCodes.filter(x => (x.timeCodeValue > 14 && x.timeCodeValue !== 49));
        this.timeCodes = timeCodes;
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

    //get lock date
    this.configurationService.getLockDate().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let lockDate: IAdminOptionsVariable = <IAdminOptionsVariable>response.Subject;

          if (lockDate) {
            let lockDateFormFieldVariable: IAdminOptionsVariable = lockDate;
            let today: Date = new Date();
            //getting 0 day of current month returns last day of current month in JavaScript (getMonth() is zero-based, so + 1 is current month)
            let lastDayOfCurrentMonth: number = new Date(today.getFullYear(), today.getMonth() + 1, 0).getDate();
            let lockDateNumber: number = parseInt(lockDate.optionValue);
            if (lockDateNumber > lastDayOfCurrentMonth) {
              lockDateNumber = lastDayOfCurrentMonth;
            }

            this.lockDate = new Date(today.getFullYear(), today.getMonth(), lockDateNumber);

            //calculate unlock date
            //unlock modifier = 2 indicates that we can't unlock until after next month
            //unlock modifier = 1 indicates that we unlock starting the first of the next month
            let unlockModifier = 2;
            if (Utils.formatDateOnly(new Date()) || new Date() <= this.lockDate)
              unlockModifier = 1;
            this.unlockDate = new Date(this.lockDate.getFullYear(), this.lockDate.getMonth() + unlockModifier, 1);

            this.setInitialScheduleDefaults();
          }
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

    //get block-out dates (current day and future)
    this.configurationService.getBlockOutDates().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.blockOutDates = <IBlockOutDate[]>response.Subject;
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  ngOnInit(): void {

    //set date and week defaults
    if (!this.selectedDate)
      this.selectedDate = new Date();

    //this.userSchedulesService.selectedDate.next(this.selectedDate);
    if (!this.selectedDateFC)
      this.selectedDateFC = new FormControl(this.selectedDate.toISOString());

    if (!this.selectedWeekStartAndEnd)
      this.setSelectedWeek();

    this.applyFilters();

    if (!this.selectedDateRange.value)
      this.selectedDateRange = new FormGroup({
        start: new FormControl(new Date()),
        end: new FormControl(new Date()),
      });

    this.selectedCustomRange = new FormGroup({
      start: new FormControl(),
      end: new FormControl(),
    });

    //dynamic popup resizing
    window.addEventListener('resize', function (event) {

      Utils.schedulerPopupDynamicSize();
    });
  }

  ngAfterViewInit(): void {
    //dynamic popup resizing
    Utils.schedulerPopupDynamicSize(this.validationMessagesExpanded);
  }

  //-----------------------
  //button actions
  //-----------------------

  //close the scheduler popup
  closeScheduler(): void {
    let leaveFunction: Function = (): void => {
      this.globalsService.closeSchedulePopup();
      this.userSchedulesService.setAllUserSchedulesByAnchorDate(Utils.formatDateOnlyToString(this.selectedDate, true, true, true) || '');
    }

    let changedSchedules: ISchedule[] = this.userSchedulesMonth.filter(x => (x.changed) || (x.markedForDeletion));
    if (changedSchedules.length > 0) {
      let leaveButton: IActionButton = {
        label: 'Leave',
        callbackFunction: leaveFunction
      }

      let stayButton: IActionButton = {
        label: 'Stay',
        callbackFunction: (): void => {
          this.globalsService.displayBlackFilter();
        }
      }

      this.globalsService.displayPopupMessage(Utils.generatePopupMessageWithCallbacks('Confirm', 'You have unsaved changes to your schedules<br />Are you sure you want to close the schedule window?', [leaveButton, stayButton], true));

    } else {
      leaveFunction();
    }
  };

  saveConfirmation(): void {
    //BEGIN confirm deletion
    let deleteCustomSchedules: ISchedule[] = this.filteredUserSchedulesCustom.filter(x => x.markedForDeletion);
    let markedForDeletionSchedules: ISchedule[] = this.userSchedulesMonth.filter(x => x.markedForDeletion);

    if (deleteCustomSchedules.length > 0) {
      markedForDeletionSchedules = [...markedForDeletionSchedules, ...deleteCustomSchedules];
    }

    if (markedForDeletionSchedules.length > 0) {
      let deleteTitle: string = '';
      let deleteMessage: string = '';
      let uniqueInterviewers = [...new Set(markedForDeletionSchedules.map(x => x.userid))];
      if (uniqueInterviewers.length > 1) {
        deleteTitle = 'Confirm Batch Delete';
        deleteMessage = '<p><b>Warning:</b> schedules for more than one interviewer will be deleted by this action.</p>';
      } else {
        let preferredName: string = `${markedForDeletionSchedules[0].preferredfname || markedForDeletionSchedules[0].fname} ${markedForDeletionSchedules[0].preferredlname || markedForDeletionSchedules[0].lname}`;
        deleteTitle = 'Confirm Delete';
        deleteMessage = `<p>One or more schedules will be deleted for interviewer "${preferredName}".</p>`;
      }

      let deleteFunction: Function = (): void => {
        this.saveSchedules();
      }

      let cancelFunction: Function = (): void => {
        return;
      }

      let confirmButton: IActionButton = {
        label: 'Delete Schedule(s)',
        callbackFunction: deleteFunction
      }

      let cancelButton: IActionButton = {
        label: 'Cancel',
        callbackFunction: cancelFunction
      }

      this.globalsService.displayPopupMessage(Utils.generatePopupMessageWithCallbacks(deleteTitle, deleteMessage, [confirmButton, cancelButton], true));
    } else {
      this.saveSchedules();
    }
    //END confirm deletion
  }

  //save all schedule changes
  saveSchedules(): void {
    //remove any added, but marked for deletion rows first
    let newSchedulesDeleted: boolean = false;
    if (this.userSchedulesMonth.filter(x => (x.preschedulekey == 0 && x.markedForDeletion && !x.addTab)).length > 0 || this.filteredUserSchedulesCustom.filter(x => (x.preschedulekey == 0 && x.markedForDeletion && !x.addTab)).length > 0) {
      newSchedulesDeleted = true;
      this.userSchedulesMonth = this.userSchedulesMonth.filter(x => !(x.preschedulekey == 0 && x.markedForDeletion && !x.addTab));
      this.filteredUserSchedulesCustom = this.filteredUserSchedulesCustom.filter(x => !(x.preschedulekey == 0 && x.markedForDeletion && !x.addTab));
      this.applyFilters();
    }

    let changedSchedules: ISchedule[] = this.userSchedulesMonth.filter(x => (x.changed && !x.invalid) || (x.markedForDeletion));
    let changedCustomSchedules: ISchedule[] = this.filteredUserSchedulesCustom.filter(x => (x.changed && !x.invalid));

    if (changedCustomSchedules.length > 0) {
      let uniqueChangedCustomSchedules: ISchedule[] = changedCustomSchedules.filter(x => !changedSchedules.map(y => y.projectid).includes(x.projectid));
      changedSchedules = [...changedSchedules, ...uniqueChangedCustomSchedules];
    }

    let markedForDeletionSchedules: ISchedule[] = this.userSchedulesMonth.filter(x => x.markedForDeletion);
    let deleteCustomSchedules: ISchedule[] = this.filteredUserSchedulesCustom.filter(x => x.markedForDeletion);

    if (deleteCustomSchedules.length > 0) {
      markedForDeletionSchedules = [...markedForDeletionSchedules, ...deleteCustomSchedules];
    }

    let toDeleteSchedulesMin: IScheduleMin[] = [];

    if (markedForDeletionSchedules.length > 0) {
      this.userSchedulesMonth = this.userSchedulesMonth.filter(x => !(x.markedForDeletion && (!x.preschedulekey || x.preschedulekey < 1)));
      markedForDeletionSchedules = markedForDeletionSchedules.filter(x => !(x.markedForDeletion && (!x.preschedulekey || x.preschedulekey < 1)));
      if (markedForDeletionSchedules.length > 0) {
        toDeleteSchedulesMin = this.getSchedulesMin(markedForDeletionSchedules);
      }
    }

    //remove schedules marked for deletion from changed schedules, if any
    changedSchedules = changedSchedules.filter(x => !(x.markedForDeletion));

    if (changedSchedules.length < 1 && markedForDeletionSchedules.length < 1 && this.userSchedulesMonth.filter(x => (x.invalid && x.changed)).length < 1 && this.filteredUserSchedulesCustom.filter(x => (x.invalid && x.changed)).length < 1 && !newSchedulesDeleted) {
      this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Info', 'No changes to save', ['OK']));
      return;
    }

    //save changed schedules
    if (changedSchedules.length > 0) {
      //validate start/end date time values
      for (var i = 0; i < changedSchedules.length; i++) {
        Utils.validateStartEndDateTime(changedSchedules[i]);
      }

      let changedSchedulesMin: IScheduleMin[] = this.getSchedulesMin(changedSchedules);
      this.userSchedulesService.saveSchedules(changedSchedulesMin).subscribe(
        response => {
          if ((response.Status || '').toUpperCase() == 'SUCCESS') {
            try {

              //execute schedule validations at the server
              this.validateSchedules(changedSchedules);

              //if we have new schedules, we don't want to juggle connecting their new preschedulekeys auto-incremented from the database, so just refresh data
              if (this.userSchedulesMonth.filter(x => x.preschedulekey == 0).length > 0) {
                this.userSchedulesService.setAllUserSchedulesByAnchorDate(Utils.formatDateOnlyToString(this.selectedDate, true, true, true) || '');
              }
              ////if we don't have new schedules to save, just push out changes and correct the flags
              //else {

              this.requestAutomation(changedSchedules);

              //set saved, changed, and marked for deletion flags
              for (var ii = 0; ii < changedSchedules.length; ii++) {
                if (changedSchedules[ii].preschedulekey > 0) {

                  let scheduleIndex: number = this.userSchedulesMonth.findIndex(x => x.preschedulekey == changedSchedules[ii].preschedulekey);
                  //if not for the custom tab, all changed schedules should be present in month schedules array, but because of the custom tab, we may have changes outside this range
                  if (scheduleIndex > -1) {
                    this.userSchedulesMonth[scheduleIndex].saved = true;
                    this.userSchedulesMonth[scheduleIndex].changed = false;
                    this.userSchedulesMonth[scheduleIndex].markedForDeletion = false;
                  }
                  //custom syncing
                  let scheduleIndexCustom: number = this.filteredUserSchedulesCustom.findIndex(x => x.preschedulekey == changedSchedules[ii].preschedulekey);
                  if (scheduleIndexCustom > -1) {
                    //sync changes from month to schedule, but only if the month has been changes (month gets precedence over any concurrent changes in custom)
                    //if (scheduleIndex > -1) {
                    //  if (this.userSchedulesMonth[scheduleIndex].changed && !this.userSchedulesMonth[scheduleIndex].addTab) {
                    //    this.filteredUserSchedulesCustom[scheduleIndexCustom] = this.userSchedulesMonth[scheduleIndex];
                    //  }
                    //}
                    //set custom schedule flags
                    this.filteredUserSchedulesCustom[scheduleIndexCustom].saved = true;
                    this.filteredUserSchedulesCustom[scheduleIndexCustom].changed = false;
                    this.filteredUserSchedulesCustom[scheduleIndexCustom].markedForDeletion = false;
                  }
                } else if (changedSchedules[ii].addTab) {
                  changedSchedules[ii].addInitial = false;
                  changedSchedules[ii].addTab = false;
                  changedSchedules[ii].saved = true;
                  changedSchedules[ii].changed = false;
                  changedSchedules[ii].markedForDeletion = false;

                  //  //initial schedule
                  //  if (this.userSchedulesMonth.filter(x => x.addInitial).length < 1) {
                  //    this.initialSchedule = {} as ISchedule;
                  //    this.initialSchedule.addInitial = true;
                  //    this.initialSchedule.addTab = true;
                  //    this.userSchedulesMonth.push(this.initialSchedule);
                  //  }

                }

                //}

                //push all changes out to other components
                this.userSchedulesMonth = [...this.userSchedulesMonth];
                this.userSchedulesService.userSchedules.next(this.userSchedulesMonth);

              }

            } catch (ex) {
              this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Schedules have been saved successfully but there was an issue refreshing the page...You may need to refresh your page manually to reflect changes.', ['OK']));
              console.log(ex);
            }

            //if we also have schedules to delete, delete them here and then display a message
            if (markedForDeletionSchedules.length > 0) {
              this.deleteSchedules(toDeleteSchedulesMin, true);
            } else {
              if (this.userSchedulesMonth.filter(x => (x.invalid && x.changed)).length > 0) {
                this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Save successful!<br /><br />Some schedules failed validation and were not saved. Please review the fields in red.', ['OK']));
              } else {
                this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Save successful', ['OK']));
              }
            }

          } else {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to save schedules', ['OK']));
          }
        },
        error => {
          this.errorMessage = <string>(error.message);
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to save schedules:<br />' + this.errorMessage, ['OK']));
          this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
        }
      );
    } else if (newSchedulesDeleted) {
      this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Schedule(s) deleted', ['OK']));
    } else if (this.userSchedulesMonth.filter(x => (x.invalid && x.changed)).length > 0 || this.filteredUserSchedulesCustom.filter(x => (x.invalid && x.changed)).length > 0) {
      this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Partial Success', 'One or more schedules failed validation and cannot be saved. Please review the fields in red.', ['OK']));
    }

    //delete schedules marked for deletion and display a success message if there are no other changes to save
    if (markedForDeletionSchedules.length > 0 && changedSchedules.length < 1) {
      this.deleteSchedules(toDeleteSchedulesMin, true);
    }
  }

  //send user/months to controller to validate schedules based on level
  validateSchedules(sourceSchedules: ISchedule[]): void {
    //use validation message object to contain and send user netids and the months for the schedules
    let userMonths: IValidationMessage[] = [];

    for (var i = 0; i < sourceSchedules.length; i++) {
      let schedFirstOf: Date = new Date(sourceSchedules[i].startdatetime || '');
      schedFirstOf.setDate(1);
      let match: IValidationMessage[] = userMonths.filter(x => (x.dempoId == sourceSchedules[i].dempoid
        && Utils.formatDateOnlyToString(x.inMonth) == Utils.formatDateOnlyToString(schedFirstOf)));

      if (match.length < 1) {
        let userMonth: IValidationMessage = {
          dempoId: sourceSchedules[i].dempoid,
          inMonth: Utils.formatDateOnly(schedFirstOf) || new Date(),
          messageId: 0,
          validationMessagesId: 0,
          scheduleKeys: null,
          messageText: null,
          details: null
        };
        userMonths.push(userMonth);
      }

    }

    //call validate schedules
    this.userSchedulesService.validateSchedules(userMonths).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          try {
            this.getValidationMessages();
            //this.validationMessages = <IValidationMessage[]>(response.Subject).filter(x => x.DempoId == this.authenticatedUser.NetID);
          } catch (ex) {
            console.log(ex);
          }
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    )
  }

  //function to handle deleting schedules
  deleteSchedules(schedulesToDelete: IScheduleMin[], successMessage: boolean = false): void {
    this.userSchedulesService.deleteSchedules(schedulesToDelete).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          try {
            //remove deleted schedules from schedules array
            this.userSchedulesMonth = this.userSchedulesMonth.filter(x => !schedulesToDelete.map(x => x.preschedulekey).includes(x.preschedulekey));
            this.userSchedulesMonth = this.userSchedulesMonth.filter(x => x.preschedulekey !== 0);
            this.filteredUserSchedulesCustom = this.filteredUserSchedulesCustom.filter(x => !schedulesToDelete.map(x => x.preschedulekey).includes(x.preschedulekey));

            //push all changes out to other components
            this.userSchedulesService.userSchedules.next(this.userSchedulesMonth);

            if (successMessage) {
              this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Save successful!', ['OK']));
            }

            //check all conflicting schedules, if a conflicting schedule never changes, it won't get unflagged otherwise if it is no longer in conflict due to another ocnflict changing
            let conflictedSchedules: ISchedule[] = this.userSchedulesMonth.filter(x => x.scheduleConflict);
            if (conflictedSchedules.length > 0) {
              for (var i = 0; i < conflictedSchedules.length; i++) {
                this.conflictValidation(conflictedSchedules[i]);
              }
            }

          } catch (ex) {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Schedules have been deleted successfully but there was an issue refreshing the page... You may need to refresh your page manually to reflect changes.', ['OK']));
            console.log(ex);
          }
        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to delete schedules', ['OK']));
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Unable to delete schedules:<br />' + this.errorMessage, ['OK']));
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }

  //handle schedule changes
  changeEvent(schedule: ISchedule): void {

    //add changed, not new, schedule to schedule cache for real-time overlap validation
    if (schedule.preschedulekey > 0) {
      if (this.scheduleCache.filter(x => x.preschedulekey == schedule.preschedulekey).length > 0) {
        let cacheIndex: number = this.scheduleCache.findIndex(x => x.preschedulekey == schedule.preschedulekey);
        this.scheduleCache[cacheIndex] = schedule;
      }
    }

    //check for conflicts with the changed schedule - changed schedule is always patient 0 and it marks other schedules that conflict with it
    this.conflictValidation(schedule);

    //check all conflicting schedules, if a conflicting schedule never changes, it won't get unflagged otherwise if it is no longer in conflict due to another ocnflict changing
    let conflictedSchedules: ISchedule[] = this.userSchedulesMonth.filter(x => x.scheduleConflict);
    if (conflictedSchedules.length > 0) {
      for (var i = 0; i < conflictedSchedules.length; i++) {
        this.conflictValidation(conflictedSchedules[i]);
      }
    }

    this.validateRequiredFields(schedule);
    this.businessRulesValidation(schedule);

    //tab validation
/*    this.addTabInvalid = this.addedSchedules.filter(x => (x.changed && x.invalid)).length > 0;
    this.dayTabInvalid = this.filteredUserSchedulesDay.filter(x => x.invalid).length > 0;
    this.weekTabInvalid = this.filteredUserSchedulesWeek.filter(x => x.invalid).length > 0;
    this.monthTabInvalid = this.filteredUserSchedulesMonth.filter(x => x.invalid).length > 0;
    this.customTabInvalid = this.filteredUserSchedulesCustom.filter(x => x.invalid).length > 0;*/
    console.log("invalid :-"+this.addTabInvalid,this.dayTabInvalid,this.weekTabInvalid,this.monthTabInvalid,this.customTabInvalid);

  }

  conflictValidation(schedule: ISchedule): void {
    schedule.hoverMessage = undefined;

    let scheduleCacheTemp: ISchedule[] = [...this.addedSchedules, ...this.scheduleCache, ...this.userSchedulesMonth.filter(x => x.preschedulekey == 0)];

    //start/end time overlap validation
    if (schedule.startdatetime) {
      let startTimeAsDate = new Date(Utils.formatDateOnlyToString(schedule.startdatetime) + ' ' + schedule.startTime);
      let endTimeAsDate = new Date(Utils.formatDateOnlyToString(schedule.enddatetime) + ' ' + schedule.endTime);

      if (Utils.isValidDate(startTimeAsDate)
        && Utils.isValidDate(endTimeAsDate)) {
        let startTimeConflicts: ISchedule[] = scheduleCacheTemp.filter(x => (x.dempoid == schedule.dempoid && (x.startdatetime && x.enddatetime))
          && (
            (
              Utils.formatDateOnlyToString(x.startdatetime) == Utils.formatDateOnlyToString(schedule.startdatetime)
              && ((x.enddatetime.getHours() * 60) + x.enddatetime.getMinutes()) > ((startTimeAsDate.getHours() * 60) + startTimeAsDate.getMinutes())
              && ((x.startdatetime.getHours() * 60) + x.startdatetime.getMinutes()) < ((startTimeAsDate.getHours() * 60) + startTimeAsDate.getMinutes())
            )
            || (Utils.formatDateOnlyToString(x.startdatetime) == Utils.formatDateOnlyToString(schedule.startdatetime) && x.startTime == schedule.startTime)
            //|| (x.StartTime == schedule.StartTime)
            || (
              Utils.formatDateOnlyToString(x.startdatetime) == Utils.formatDateOnlyToString(schedule.startdatetime)
              && ((x.startdatetime.getHours() * 60) + x.startdatetime.getMinutes() >= ((startTimeAsDate.getHours() * 60) + startTimeAsDate.getMinutes()))
              && (((x.enddatetime.getHours() * 60) + x.enddatetime.getMinutes()) <= ((endTimeAsDate.getHours() * 60) + endTimeAsDate.getMinutes()))
            )
          )
        );

        let endTimeConflicts: ISchedule[] = scheduleCacheTemp.filter(x => (x.dempoid == schedule.dempoid && (x.startdatetime && x.enddatetime))
          && (
            (
              Utils.formatDateOnlyToString(x.startdatetime) == Utils.formatDateOnlyToString(schedule.startdatetime)
              && ((x.startdatetime.getHours() * 60) + x.startdatetime.getMinutes()) < ((endTimeAsDate.getHours() * 60) + endTimeAsDate.getMinutes())
              && ((x.enddatetime.getHours() * 60) + x.enddatetime.getMinutes()) > ((endTimeAsDate.getHours() * 60) + endTimeAsDate.getMinutes())
            )
            || (Utils.formatDateOnlyToString(x.enddatetime) == Utils.formatDateOnlyToString(schedule.enddatetime) && x.startTime == schedule.startTime)
            //|| (Utils.formatDateOnlyToString(x.Enddatetime) == Utils.formatDateOnlyToString(schedule.Enddatetime))
          )
        );

        //we should always have 1 match of each, because this schedule isn't easily excluded from the check
        //(if we weren't validating new schedules, we could use the schedule key, but the primary use case is new schedules...)
        if (startTimeConflicts.length > 1 || endTimeConflicts.length > 1) {
          const uniqueValue = (value: any, index: any, self: any) => {
            return self.indexOf(value) === index
          }

          let htmlMessage: string = '';
          let allConflicts: ISchedule[] = [...startTimeConflicts, ...endTimeConflicts];
          allConflicts = allConflicts.filter(uniqueValue);

          if (allConflicts.length > 0) {
            htmlMessage = htmlMessage + '<p class="hover-message-title">Conflicting schedules found:</p>'
          }

          for (var i = 0; i < allConflicts.length; i++) {
            if (!allConflicts[i].projectName) {
              const project = this.allProjects.find(x => x.projectID == allConflicts[i].projectid);
              if (project) {
                allConflicts[i].projectName = project.projectName;
              }
            }

            htmlMessage = htmlMessage + '<p><span class="bold italic">' + allConflicts[i].projectName + '</span> &nbsp;&nbsp;<span style="color: darkmagenta">' + Utils.formatDateOnlyWithMonthNameToString(allConflicts[i].startdatetime) + ',</span> &nbsp;&nbsp;<span class="italic">' + allConflicts[i].startTime + ' – ' + allConflicts[i].endTime + '</span></p>';
            //console.log('MFD: ' + Utils.formatDateOnlyToString(allConflicts[i].Startdatetime) + ' / ' + allConflicts[i].StartTime + ' - ' + allConflicts[i].EndTime);
          }

          for (var i = 0; i < allConflicts.length; i++) {
            if (allConflicts[i].markedForDeletion) {
              //console.log('MFD: ' + Utils.formatDateOnlyToString(allConflicts[i].Startdatetime) + ' / ' + allConflicts[i].StartTime + ' - ' + allConflicts[i].EndTime);
              allConflicts[i].invalid = false;
              allConflicts[i].scheduleConflict = false;
              allConflicts[i].hoverMessage = undefined;

              this.validateRequiredFields(allConflicts[i]);
              this.businessRulesValidation(allConflicts[i]);
              continue;
            } else {

              allConflicts[i] = this.setConflictProperties(allConflicts[i], htmlMessage);

              //set the invalidFields and scheduleConflict on the month schedules if they match the conflict preschedule key
              if (allConflicts[i].preschedulekey > 0) {
                if (this.userSchedulesMonth.filter(x => x.preschedulekey == allConflicts[i].preschedulekey).length > 0) {
                  let cacheIndex: number = this.userSchedulesMonth.findIndex(x => x.preschedulekey == allConflicts[i].preschedulekey);
                  this.userSchedulesMonth[cacheIndex] = this.setConflictProperties(this.userSchedulesMonth[cacheIndex], htmlMessage);
                  this.validateRequiredFields(this.userSchedulesMonth[cacheIndex]);
                  this.businessRulesValidation(this.userSchedulesMonth[cacheIndex]);
                }
              }

              this.validateRequiredFields(allConflicts[i]);
              this.businessRulesValidation(allConflicts[i]);
            }
          }

          if (schedule.markedForDeletion || allConflicts.filter(x => !x.markedForDeletion).length < 2) {
            //console.log('MFD Original: ' + Utils.formatDateOnlyToString(schedule.Startdatetime) + ' / ' + schedule.StartTime + ' - ' + schedule.EndTime);
            schedule.hoverMessage = undefined;
            schedule.invalid = false;
            schedule.scheduleConflict = false;
          }

        } else {
          schedule.invalid = false;
          schedule.scheduleConflict = false;
        }

      }
    }

    this.validateRequiredFields(schedule);
    this.businessRulesValidation(schedule);
  }

  //set conflict properties on a schedule - helper function for code reuse
  setConflictProperties(schedule: ISchedule, hoverMessage: string): ISchedule {

    if (hoverMessage.length > 0) {
      schedule.hoverMessage = hoverMessage;
    }

    schedule.invalid = true;

    if (!schedule.invalidFields)
      schedule.invalidFields = [];

    if (!schedule.invalidFields.includes('StartTime')) {
      schedule.invalidFields.push('StartTime');
    }
    if (!schedule.invalidFields.includes('EndTime')) {
      schedule.invalidFields.push('EndTime');
    }
    schedule.scheduleConflict = true;

    return schedule;
  }

  //validation of required fields
  validateRequiredFields(schedule: ISchedule): void {


    // Define a function that abstracts the complex date comparison.
    function isDateValid(date: Date, unlockDate: Date, lockDate: Date, authenticatedUser: any, currentUser: any ): boolean {

      //admin role , resourceGroup role and canEdit flag should be able to edit any schedule at any time
/*      if (authenticatedUser.admin || currentUser.canedit || authenticatedUser.resourceGroup) {
        return true;
      }


      const scheduleDate =  (date !== null && date !== undefined) ? new Date(date.getFullYear(), date.getMonth(), date.getDate()) : null;
      scheduleDate?.setHours(0, 0, 0, 0 );

      const unlockDateTwo = (unlockDate !== null && unlockDate !== undefined) ? new Date(unlockDate.getFullYear(), unlockDate.getMonth() + 1, unlockDate.getDate()) : null;
      unlockDateTwo?.setHours(0, 0, 0, 0 );
      const currentDate = new Date();
      currentDate?.setHours(0, 0, 0, 0 );

      //currentDate On or before lockDate, interviewers will be able to add/modify schedules for UnlockDate and later.
      if (scheduleDate != null  && authenticatedUser.interviewer && currentDate <= lockDate) {
        return  scheduleDate >= unlockDate ;
      }

      //currentDate After lockDate, interviewers will only be able to add/modify schedules for unlockDateTwo and later.
      if ( scheduleDate != null && unlockDate != null && unlockDateTwo != null  && authenticatedUser.interviewer && currentDate > lockDate ) {
        return scheduleDate >= unlockDateTwo;
      }

      return false;*/
      return date instanceof Date && !(authenticatedUser === null || authenticatedUser === undefined);
    }


    if (!schedule.invalidFields) {
      schedule.invalidFields = [];
    }

    //user
    if (!schedule.dempoid) {
      schedule.invalid = true;
      schedule.invalidFields.push('User');
    } else if (schedule.dempoid.replace(/\s/g, '') == '' || schedule.dempoid == '0') {
      schedule.invalid = true;
      schedule.invalidFields.push('User');
    } else {
      schedule.invalidFields = schedule.invalidFields.filter(x => x !== 'User');
    }

    //project
    if (!schedule.projectName) {
      schedule.invalid = true;
      schedule.invalidFields.push('Project');
    } else if (schedule.projectName.replace(/\s/g, '') == '' || schedule.projectName == '0') {
      schedule.invalid = true;
      schedule.invalidFields.push('Project');
    } else {
      schedule.invalidFields = schedule.invalidFields.filter(x => x !== 'Project');
    }

    //date
    let blockOutDateStrings: string[] = this.blockOutDates.map(x => Utils.formatDateAsStringUTC(x.blockOutDay)) as string[];

    if (!schedule.startdatetime) {
      schedule.invalid = true;
      schedule.invalidFields.push('Date');
    } else if (schedule.startdatetime.toString() == 'Invalid Date') {
      schedule.invalid = true;
      schedule.invalidFields.push('Date');

      //lock date validation against current date and schedule date
    } else if (!isDateValid(schedule.startdatetime, this.unlockDate, this.lockDate, this.authenticatedUser, this.currentUser)) {
      schedule.invalid = true;
      schedule.invalidFields.push('Date');
      if (!schedule.validationMessages) {
        schedule.validationMessages = [];
      }
      const ult = (this.unlockDate !== null && this.unlockDate !== undefined) ? new Date(this.unlockDate.getFullYear(), this.unlockDate.getMonth() + 1, this.unlockDate.getDate()) : null;
      if ( ult ) {
        let unlockMessage: string = schedule.startdatetime < this.unlockDate ? Utils.formatDateOnlyToString(schedule.startdatetime) + ' is before the end of the current lock period.<br />Please choose a date on or after ' + Utils.formatDateOnlyToString(this.unlockDate)
          : Utils.formatDateOnlyToString(schedule.startdatetime) + ' is before the end of the current lock period.<br />Please choose a date on or after ' + Utils.formatDateOnlyToString(ult);
        if (!(schedule.validationMessages || []).includes(unlockMessage)) {
          schedule.validationMessages.push(unlockMessage);
        }
      }


      //blockout date validations
    } else if (blockOutDateStrings.includes(Utils.formatDateOnlyToString(schedule.startdatetime) || '')
      && this.authenticatedUser.interviewer
      && !this.authenticatedUser.resourceGroup
      && !this.authenticatedUser.admin) {

      if (!schedule.validationMessages) {
        schedule.validationMessages = [];
      }

      let blockOutDate: IBlockOutDate = this.blockOutDates.find(x => Utils.formatDateAsStringUTC(x.blockOutDay) == Utils.formatDateAsStringUTC(schedule.startdatetime)) as IBlockOutDate;
      let blockOutStart: Date = Utils.buildDateTime(blockOutDate.blockOutDay, blockOutDate.startTime) || new Date();
      let blockOutEnd: Date = Utils.buildDateTime(blockOutDate.blockOutDay, blockOutDate.endTime) || new Date();

      if (blockOutDate?.allDay) {
        schedule.invalid = true;
        schedule.invalidFields.push('Date');
        let blockOutMessage: string = Utils.formatDateOnlyToString(schedule.startdatetime) + ' is blocked out and scheduling is not allowed.<br />Please choose another date.';
        //make sure we don't double-add the same blockout message
        if (!schedule.validationMessages.includes(blockOutMessage))
          schedule.validationMessages.push(blockOutMessage);
      } else if (!blockOutDate?.allDay
        && ((schedule.enddatetime !== null && schedule.startdatetime !== null && schedule.enddatetime !== undefined && schedule.startdatetime !== undefined)
          && ((schedule.startdatetime >= blockOutStart) || (schedule.enddatetime > blockOutStart )))) {
        schedule.invalid = true;
        schedule.invalidFields.push('Date');
        const msg_undefined_time = 'Start Date Time or End Date Time is not defined .Please choose time between ' + blockOutDate.startTime + ' - ' + blockOutDate.endTime;
        const msg_validation = Utils.formatDateOnlyToString(schedule.startdatetime) + ' ' + schedule.startTime + ' - ' + schedule.endTime + ' is blocked out for this time on this date and scheduling is not allowed.<br />The date/time blocked out is ' + Utils.formatDateOnlyToString(blockOutDate.blockOutDay) + ' ' + blockOutDate.startTime + ' - ' + blockOutDate.endTime + '<br />Please choose another date.';
        if (schedule.startdatetime === undefined || schedule.enddatetime === undefined) {
          if (!schedule.validationMessages.includes(msg_undefined_time)) {
            schedule.validationMessages.push(msg_undefined_time);
          }
        } else {
          if (!schedule.validationMessages.includes(msg_validation)) {
            schedule.validationMessages.push(msg_validation);
          }
        }
      } else {
        schedule.invalidFields = schedule.invalidFields.filter(x => x !== 'Date');
      }

    } else {
      schedule.invalidFields = schedule.invalidFields.filter(x => x !== 'Date');
    }

    //start time
    if (!schedule.startTime) {
      schedule.invalid = true;
      schedule.invalidFields.push('StartTime');
    } else if (schedule.startTime.replace(/\s/g, '') == '' || schedule.startTime == '0') {
      schedule.invalid = true;
      schedule.invalidFields.push('StartTime');
    } else if (!schedule.scheduleConflict) {
      schedule.invalidFields = schedule.invalidFields.filter(x => x !== 'StartTime');
    }

    //end time
    if (!schedule.endTime) {
      schedule.invalid = true;
      schedule.invalidFields.push('EndTime');
    } else if (schedule.endTime.replace(/\s/g, '') == '' || schedule.endTime == '0') {
      schedule.invalid = true;
      schedule.invalidFields.push('EndTime');
    } else if (!schedule.scheduleConflict) {
      schedule.invalidFields = schedule.invalidFields.filter(x => x !== 'EndTime');
    }

    if (schedule.invalidFields.length < 1) {
      schedule.invalid = false;
    }

  }

  //validation based on business rules - that can be done within the scope of a single schedule
  businessRulesValidation(schedule: ISchedule): void {
    //end date must be greater than start date
    let startTimeAsDate = new Date(Utils.formatDateOnlyToString(new Date()) + ' ' + schedule.startTime);
    let endTimeAsDate = new Date(Utils.formatDateOnlyToString(new Date()) + ' ' + schedule.endTime);

    if (Utils.isValidDate(startTimeAsDate)
      && Utils.isValidDate(endTimeAsDate)) {
      if (((endTimeAsDate.getHours() * 60) + endTimeAsDate.getMinutes()) - ((startTimeAsDate.getHours() * 60) + startTimeAsDate.getMinutes()) < 1) {
        schedule.invalid = true;
        if (!schedule.invalidFields) {
          schedule.invalidFields = [];
        }
        schedule.invalidFields.push('StartTime');
        schedule.invalidFields.push('EndTime');
      }
    }

  }
  //create list of schedule min from schedules
  getSchedulesMin(schedules: ISchedule[]): IScheduleMin[] {
    let schedulesMin: IScheduleMin[] = [];
    //create list of schedule min for changed schedules
    for (var i = 0; i < schedules.length; i++) {
      let scheduleMin = {
        dempoid: schedules[i].dempoid,
        projectid: schedules[i].projectid,
        scheduledate: schedules[i].scheduledate,
        startdatetime: schedules[i].startdatetime,
        enddatetime: schedules[i].enddatetime,
        comments: schedules[i].comments,
        preschedulekey: schedules[i].preschedulekey,
        entryBy: schedules[i].entryBy,
        entryDt: schedules[i].entryDt,
        modBy: this.authenticatedUser.netID,
        modDt: new Date(),
        machineName: 'NA',
        status: 0,
      };

      schedulesMin.push(scheduleMin as IScheduleMin);
    }

    return schedulesMin;
  }

  //add a new schedule line
  addSchedule(sourceSchedule: ISchedule, addTab: boolean = false, netId: string | null = null, addInitial: boolean = false): void {
    let scheduleIndex: number = this.userSchedulesMonth.findIndex(x => x.preschedulekey == sourceSchedule.preschedulekey) + 1;
    let scheduleIndexCustom: number = this.filteredUserSchedulesCustom.findIndex(x => x.preschedulekey == sourceSchedule.preschedulekey) + 1;
    let customTab: boolean = false;
    if (this.currentTab == 'Custom') {
      customTab = true;
    }

    let newSchedule: ISchedule = JSON.parse(JSON.stringify(sourceSchedule));
    newSchedule.dempoid = (netId ? (this.contextNetId ? this.contextNetId : netId) : sourceSchedule.dempoid);
    newSchedule.preschedulekey = 0;
    newSchedule.comments = '';
    //newSchedule.Startdatetime = Utils.formatDateOnly((addTab ? new Date() : newSchedule.Startdatetime));
    newSchedule.startdatetime = Utils.formatDateOnly(newSchedule.startdatetime);
    newSchedule.enddatetime = null;
    newSchedule.startTime = '0';
    newSchedule.endTime = '0';
    newSchedule.changed = !addInitial;
    newSchedule.addTab = addTab;
    newSchedule.addInitial = addInitial;
    newSchedule.markedForDeletion = false;
    newSchedule.invalid = true;
    newSchedule.invalidFields = [];
    newSchedule.fname = null;
    newSchedule.lname = null;
    newSchedule.displayName = null;
    newSchedule.userid = null;
    newSchedule.initialProjectid = null;

    if (!newSchedule.dempoid) {
      newSchedule.invalidFields.push('User');
    }
    if (!newSchedule.projectName) {
      newSchedule.invalidFields.push('Project');
    }

    if (netId) {
      //update the display name and user id
      let user: User | undefined = this.availableUsers.find(x => x.dempoid == netId);
      if (user) {
        newSchedule.fname = user.fname;
        newSchedule.lname = user.lname;
        newSchedule.displayName = user.displayName;
        newSchedule.userid = user.userid;
      }
    }

    //remove project info if schedule is in the add tab
    if (addTab) {
      scheduleIndex = this.addedSchedules.length + 1;
      //newSchedule.Projectid = null;
      //newSchedule.ProjectName = null;
    }

    if (scheduleIndexCustom > -1 && customTab) {
      newSchedule.changed = true;
      newSchedule.invalid = true;
      this.filteredUserSchedulesCustom.splice(scheduleIndexCustom, 0, newSchedule);
      this.filteredUserSchedulesCustom = [...this.filteredUserSchedulesCustom];
    } else if (scheduleIndex > -1) {
      this.userSchedulesMonth.splice(scheduleIndex, 0, newSchedule);
      this.userSchedulesMonth = [...this.userSchedulesMonth];
    }

    //if (addInitial) {
    //  this.initialSchedule = newSchedule;
    //}

    this.applyFilters();
  }

  //handle schedule save event bubbling up from the schedule line component
  scheduleSaved(schedule: ISchedule): void {
    try {
      this.requestAutomation([schedule]);

      if (schedule.justAdded) {
        schedule.justAdded = false;
      }

      //initial schedule
      if (schedule.addTab) {
        schedule.addInitial = false;
        schedule.addTab = false;
        schedule.saved = true;
        schedule.changed = false;
        schedule.markedForDeletion = false;
        //if (this.userSchedulesMonth.filter(x => x.addInitial).length < 1) {
        //  this.initialSchedule = {} as ISchedule;
        //  console.log(this.initialSchedule);
        //  //this.initialSchedule.addInitial = true;
        //  //this.initialSchedule.addTab = true;
        //  //this.userSchedulesMonth.push(this.initialSchedule);
        //}
      }

      //execute schedule validations at the server
      this.validateSchedules([schedule]);

      let scheduleIndex: number = this.userSchedulesMonth.findIndex(x => x.preschedulekey == schedule.preschedulekey);

      if (scheduleIndex > -1) {
        this.userSchedulesMonth[scheduleIndex] = schedule;
      }

      //custom syncing
      let scheduleIndexCustom: number = this.filteredUserSchedulesCustom.findIndex(x => x.preschedulekey == schedule.preschedulekey);
      if (scheduleIndexCustom > -1) {
        //sync changes from month to schedule, but only if the month has been changes (month gets precedence over any concurrent changes in custom)
        if (scheduleIndex > -1) {
          if (this.userSchedulesMonth[scheduleIndex].changed && !this.userSchedulesMonth[scheduleIndex].addTab) {
            this.filteredUserSchedulesCustom[scheduleIndexCustom] = this.userSchedulesMonth[scheduleIndex];
          }
        }
        //set custom schedule flags
        this.filteredUserSchedulesCustom[scheduleIndexCustom].saved = true;
        this.filteredUserSchedulesCustom[scheduleIndexCustom].changed = false;
        this.filteredUserSchedulesCustom[scheduleIndexCustom].markedForDeletion = false;
      }

      this.userSchedulesMonth = [...this.userSchedulesMonth];
      this.userSchedulesService.userSchedules.next(this.userSchedulesMonth);
      this.applyFilters();
    } catch (ex) {
      this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Error updating calendar screen... Please refresh page to see your changes in the calendar', ['OK']));
      console.log(ex);
    }
  }

  //handle schedule save event bubbling up from the schedule line component
  scheduleDeleted(schedule: ISchedule): void {
    try {
      if (schedule.preschedulekey == 0) {
        schedule.saved = true;

        this.userSchedulesMonth = this.userSchedulesMonth.filter(x => !(x.preschedulekey == 0 && x.saved && x.markedForDeletion) || x.addTab);
        this.filteredUserSchedulesCustom = this.filteredUserSchedulesCustom.filter(x => !(x.preschedulekey == 0 && x.saved && x.markedForDeletion) || x.addTab);
        this.applyFilters();
      }
      this.userSchedulesMonth = this.userSchedulesMonth.filter(x => x.preschedulekey !== schedule.preschedulekey);
      this.filteredUserSchedulesCustom = this.filteredUserSchedulesCustom.filter(x => x.preschedulekey !== schedule.preschedulekey);
      this.userSchedulesService.userSchedules.next(this.userSchedulesMonth);

      //check all conflicting schedules, if a conflicting schedule never changes, it won't get unflagged otherwise if it is no longer in conflict due to another ocnflict changing
      let conflictedSchedules: ISchedule[] = this.userSchedulesMonth.filter(x => x.scheduleConflict);
      if (conflictedSchedules.length > 0) {
        for (var i = 0; i < conflictedSchedules.length; i++) {
          this.conflictValidation(conflictedSchedules[i]);
        }
      }

    } catch (ex) {
      this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Error updating calendar screen... Please refresh page to see your changes in the calendar', ['OK']));
      console.log(ex);
    }
  }

  //get schedules for the custom view
  getCustomSchedules(): void {
    if (!this.selectedCustomRange.value.end) {
      return;
    }

    let startDate: string = Utils.formatDateOnlyToString(this.selectedCustomRange.value.start, true, true, true) || '';
    let endDate: string = Utils.formatDateOnlyToString(this.selectedCustomRange.value.end, true, true, true) || '';

    this.userSchedulesService.getAllUserSchedulesByRange(startDate, endDate).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.filteredUserSchedulesCustom = <ISchedule[]>response.Subject;

          //paged custom schedules
          this.setPagedCustomSchedules();

          //use new Date() on each schedule to convert from the stored UTC time to the local browser time
          //also, set the string formatted start/end times for display purposes (this avoids having to format the time everywhere it's displayed)
          for (var i = 0; i < this.filteredUserSchedulesCustom.length; i++) {
            this.filteredUserSchedulesCustom[i].startdatetime = new Date(this.filteredUserSchedulesCustom[i].startdatetime || '');
            this.filteredUserSchedulesCustom[i].enddatetime = new Date(this.filteredUserSchedulesCustom[i].enddatetime || '');
            this.filteredUserSchedulesCustom[i].startTime = Utils.formatDateToTimeString(this.filteredUserSchedulesCustom[i].startdatetime, true) || '';
            this.filteredUserSchedulesCustom[i].endTime = Utils.formatDateToTimeString(this.filteredUserSchedulesCustom[i].enddatetime, true) || '';
          }
        }

      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }

  //-----------------------
  //active filters
  //-----------------------

  //apply filters to the current schedule and calendar views
  public applyFilters(filterChanged: string | null = null): void {
    //unset defaults if other options are selected
    //users
    let anyUserFilter: IAnyFilter = this.anyFilter(this.userFilterFC, this.anyUserToggle);
    this.userFilterFC.setValue(anyUserFilter.filterControl.value);
    this.anyUserToggle = anyUserFilter.anyToggle;

    //projects
    let anyProjectFilter: IAnyFilter = this.anyFilter(this.projectFilterFC, this.anyProjectToggle);
    this.projectFilterFC.setValue(anyProjectFilter.filterControl.value);
    this.anyProjectToggle = anyProjectFilter.anyToggle;

    this.filteredUserSchedulesMonth = this.userSchedulesMonth;
    if (this.filteredUserSchedulesCustom.length < 1) {
      this.filteredUserSchedulesCustom = this.userSchedulesMonth;
    }

    //special filtering based on filter change
    this.filterUsers();
    this.filterProjects();

    if (filterChanged == 'user') {
      let trainedOn: string = this.filteredUsers.filter(x => this.userFilterFC.value.includes(x.dempoid)).map(x => x.trainedon).join('|');

      this.filteredProjects = this.filteredProjects.filter(x => (x.projectType == 'Administrative' || Utils.pipeStringToArray(trainedOn).includes(x.projectID.toString())));
    }

    if (filterChanged == 'project') {
      let selectedProjects: string[] = this.filteredProjects.filter(x => this.projectFilterFC.value.includes(x.projectName)).map(y => y.projectID.toString());
      this.filteredUsers = this.filteredUsers.filter(x => selectedProjects.some((val) => x.trainedOnArray.indexOf(val) !== -1));
    }

    //perform selected filters
    //filter by user
    if (!this.userFilterFC.value.includes('0')) {
      this.filteredUserSchedulesMonth = this.filteredUserSchedulesMonth.filter(x => this.userFilterFC.value.includes(x.dempoid));
      this.filteredUserSchedulesCustom = this.filteredUserSchedulesCustom.filter(x => this.userFilterFC.value.includes(x.dempoid));
    }

    //filter by project
    if (!this.projectFilterFC.value.includes('0')) {
      this.filteredUserSchedulesMonth = this.filteredUserSchedulesMonth.filter(x => this.projectFilterFC.value.includes(x.projectName));
      this.filteredUserSchedulesCustom = this.filteredUserSchedulesCustom.filter(x => this.projectFilterFC.value.includes(x.projectName));
    }

    this.filteredUserSchedulesCustom = this.filteredUserSchedulesCustom.filter(x => !x.addTab);

    this.syncData();

  }

  //set default filters
  public setDefaultFilters(applyFilters: boolean): void {
    this.userFilterFC = new FormControl(['0']);
    this.projectFilterFC = new FormControl(['0']);

    if (applyFilters) {
      this.applyFilters();
    }
  }

  //handle select, deselect and auto-select of the "any" values in multi-selects
  public anyFilter(filter: FormControl, anyToggle: boolean, anyValue: string = '0'): IAnyFilter {
    //if we have any values selected
    if (filter.value.length > 0) {
      //check if the any toggle is set and we have the any value (default of "0") selected. If so, set the toggle to false and filter to only (default of "0") selected
      if (anyToggle && (filter.value.includes(anyValue))) {
        anyToggle = false;
        filter.setValue(filter.value.filter((x: string) => x == anyValue));
        //otherwise check if the any toggle is false and set the toggle to true and filter to where all selctions but the any value (default of "0") is selected
        //also make sure that there isn't only 1 value or we can end up clearing all values if the toggle is false but we do have any set (those toggles are tricksy)
      } else if (!anyToggle && filter.value.length > 1) {
        //but this isn't necessary unless we have the any value (default of "0") selected
        if (filter.value.includes(anyValue)) {
          anyToggle = true;
          filter.setValue(filter.value.filter((x: string) => x !== anyValue));
        }
      }
      //if no values selected, set to default of any value (default of "0")
    } else {
      anyToggle = false;
      filter.setValue([anyValue]);
    }

    //return an interface with both the form control and the any toggle
    return <IAnyFilter>{
      filterControl: filter,
      anyToggle: anyToggle
    };
  }

  //-----------------------
  //passive filters
  //-----------------------
  syncData(): void {

    //restrict schedules shown if user is interviewer-only
    if (this.authenticatedUser) {
      if (this.authenticatedUser.interviewer && !(this.authenticatedUser.admin || this.authenticatedUser.resourceGroup)) {
        this.filteredUserSchedulesMonth = this.filteredUserSchedulesMonth.filter(x => x.dempoid == this.authenticatedUser.netID);
        this.filteredUserSchedulesCustom = this.filteredUserSchedulesCustom.filter(x => x.dempoid == this.authenticatedUser.netID);
        this.allUsers = this.allUsers.filter(x => x.dempoid == this.authenticatedUser.netID);
      }
    }

    //added schedules
    this.addedSchedules = this.filteredUserSchedulesMonth.filter(x => x.addTab);
    if (this.addedSchedules.length < 1)
      this.addedSchedules = [this.initialSchedule];

    this.setPagedAddedSchedules();

    //filter added schedules out of month before filtering day and week
    this.filteredUserSchedulesMonth = this.filteredUserSchedulesMonth.filter(x => !x.addTab);
    this.setPagedMonthSchedules();

    //filter day
    this.filteredUserSchedulesDay = this.filteredUserSchedulesMonth.filter(x => Utils.formatDateOnlyToString(x.startdatetime) == Utils.formatDateOnlyToString(new Date(this.selectedDateFC.value)));
    this.setPagedDaySchedules();

    //filter week
    this.filteredUserSchedulesWeek = this.filteredUserSchedulesMonth.filter(x => Utils.formatDateOnlyToString(x.weekStart) === Utils.formatDateOnlyToString(this.selectedWeekStartAndEnd.weekStart));
    this.setPagedWeekSchedules();

    //paged custom schedules
    this.setPagedCustomSchedules();

    //make sure we didn't filter out the initial "add" schedule
    if (this.filteredUserSchedulesMonth.filter(x => x.addTab).length < 1) {
      this.filteredUserSchedulesMonth.push(this.initialSchedule);
    }

  }

  //filter users
  filterUsers(): void {
    //exit if we don't have all users yet
    if (this.allUsers.length < 1 || !this.authenticatedUser) {
      return;
    }

    //only display current user if interviewer-only
    if (this.authenticatedUser.interviewer
      && !this.authenticatedUser.resourceGroup
      && !this.authenticatedUser.admin) {
      this.filteredUsers = this.allUsers.filter(x => x.dempoid == this.authenticatedUser.netID.toLowerCase());
      this.availableUsers = this.allUsers.filter(x => x.dempoid == this.authenticatedUser.netID.toLowerCase());
      this.filteredUserSchedulesMonth = this.filteredUserSchedulesMonth.filter(x => x.dempoid == this.authenticatedUser.netID.toLowerCase());
    }
    //if not interviewer-only, then ony display users in the current user schedules for the selected month
    else {
      const uniqueValue = (value: any, index: any, self: any) => {
        return self.indexOf(value) === index
      }
      //get unique users from user schedules
      var availableUserDempoIds = this.userSchedulesMonth.map(x => x.dempoid).filter(uniqueValue);
      availableUserDempoIds = availableUserDempoIds.sort((a, b) => {
        return <any>new Date(a || '') - <any>new Date(b || '');
      });

      //filter available users
      this.filteredUsers = this.allUsers.filter(x => availableUserDempoIds.includes(x.dempoid));
      this.availableUsers = this.allUsers;
    }

    //unset context project name as selection in project filter form control if it isn't in the filtered list
    if (!this.filteredUsers.map(x => x.dempoid).includes(this.contextNetId)) {
      if (this.userFilterFC.value.length == 1) {
        if (this.userFilterFC.value.includes(this.contextNetId)) {
          this.setDefaultFilters(true);
        }
      }
    }

  }

  //filter projects
  filterProjects(): void {
    //exit if we don't have all projects yet
    if (this.allProjects.length < 1) {
      return;
    }

    this.filteredProjects = this.allProjects;
    this.availableProjects = this.allProjects;

    //only display current user if interviewer-only
    if (this.authenticatedUser) {
      if (this.authenticatedUser.interviewer
        && !this.authenticatedUser.resourceGroup
        && !this.authenticatedUser.admin) {
        let user: User = this.allUsers.find(x => x.dempoid == this.authenticatedUser.netID) as User;
        this.availableProjects = this.availableProjects.filter(x => (x.projectType == 'Administrative' || Utils.pipeStringToArray(user.trainedon).includes(x.projectID.toString())));
      }
    }

    //filter to only available  projects, in addition to trained on or regardless of interviewer role
    const uniqueValue = (value: any, index: any, self: any) => {
      return self.indexOf(value) === index
    }
    //get unique projects from user schedules
    var availableProjectNames = this.userSchedulesMonth.map(x => x.projectName).filter(uniqueValue);
    availableProjectNames = availableProjectNames.sort((a, b) => {
      return <any>new Date(a || '') - <any>new Date(b || '');
    });

    //filter available projects
    this.filteredProjects = this.allProjects.filter(x => (x.projectType == 'Administrative' || availableProjectNames.includes(x.projectName)));

    //unset context project name as selection in project filter form control if it isn't in the filtered list
    if (!this.filteredProjects.map(x => x.projectName).includes(this.contextProjectName)) {
      if (this.projectFilterFC.value.length == 1) {
        if (this.projectFilterFC.value.includes(this.contextProjectName)) {
          this.setDefaultFilters(true);
        }
      }
    }

  }

  //-----------------------
  //other stuff
  //-----------------------
  //set current tab name
  setTab(tabEvent: MatTabChangeEvent): void {
    this.currentTab = tabEvent.tab.textLabel;

    //dynamic popup resizing
    Utils.schedulerPopupDynamicSize(this.validationMessagesExpanded);

  }

  setInitialScheduleDefaults(netId: string | null = null, projectName: string | null = null, scheduleDate: Date | null = null): void {
    if (!(this.authenticatedUser
      && this.selectedDate
      && this.allUsers.length > 0)) {
      return;
    }

    if (!netId) {
      netId = this.authenticatedUser.netID;
    }

    //context can override other options
    if (this.contextNetId) {
      netId = this.contextNetId;
    }

    let user: User = this.allUsers.find(x => x.dempoid == netId) as User;
    let project: IProjectMin | null = null;
    if (projectName) {
      project = this.allProjects.find(x => x.projectName.toUpperCase() == projectName.toUpperCase()) as IProjectMin;
    }

    //context can override other options
    if (this.contextProjectName) {
      project = this.allProjects.find(x => x.projectName.toUpperCase() == this.contextProjectName.toUpperCase()) as IProjectMin;
    }

    let startDate: Date = new Date();
    startDate?.setHours(0, 0, 0, 0 );

    if (scheduleDate) {
      startDate = scheduleDate;
    }

    if (this.unlockDate
      && this.authenticatedUser?.interviewer
      && !this.authenticatedUser?.resourceGroup
      && !this.authenticatedUser?.admin) {

      const unlockDateTwo = new Date(this.unlockDate.getFullYear(), this.unlockDate.getMonth() + 1, this.unlockDate.getDate()) ;
      unlockDateTwo?.setHours(0, 0, 0, 0 );

      if (startDate <= this.lockDate) {
        startDate = this.unlockDate;
      } else if (startDate > this.lockDate) {
        startDate = unlockDateTwo;
      }
    }

    this.initialSchedule.dempoid = (user ? user.dempoid : this.initialSchedule.dempoid);
    this.initialSchedule.fname = (user ? user.fname : this.initialSchedule.fname);
    this.initialSchedule.lname = (user ? user.lname : this.initialSchedule.lname);
    this.initialSchedule.displayName = (user ? user.displayName : this.initialSchedule.displayName);
    this.initialSchedule.preferredfname = (user ? user.fname : this.initialSchedule.preferredfname);
    this.initialSchedule.preferredlname = (user ? user.lname : this.initialSchedule.preferredlname);
    this.initialSchedule.userName = (user ? user.fname + ' ' + user.lname : this.initialSchedule.userName);
    this.initialSchedule.projectid = (project ? project.projectID : this.initialSchedule.projectid);
    this.initialSchedule.projectName = (project ? project.projectName : this.initialSchedule.projectName);
    this.initialSchedule.scheduledate = startDate;
    this.initialSchedule.startdatetime = startDate;
    this.initialSchedule.entryBy = (user ? user.dempoid : this.initialSchedule.entryBy);
    this.initialSchedule.entryDt = new Date(),

      //Utils.validateStartEndDateTime(this.initialSchedule);

      this.changeEvent(this.initialSchedule);
  }

  //create a blank ISchedule
  getBlankSchedule(netId: string | null = null, projectName: string | null = null, scheduleDate: Date | null = null): ISchedule {
    if (!netId) {
      netId = this.authenticatedUser.netID;
    }

    //context can override other options
    if (this.contextNetId) {
      netId = this.contextNetId;
    }

    let user: User = this.allUsers.find(x => x.dempoid == netId) as User;
    let project: IProjectMin | null = null;
    if (projectName) {
      project = this.allProjects.find(x => x.projectName.toUpperCase() == projectName.toUpperCase()) as IProjectMin;
    }

    //context can override other options
    if (this.contextProjectName) {
      project = this.allProjects.find(x => x.projectName.toUpperCase() == this.contextProjectName.toUpperCase()) as IProjectMin;
    }

    return {
      preschedulekey: 0,
      dempoid: (user ? user.dempoid : null),
      fname: (user ? user.fname : null),
      lname: (user ? user.lname : null),
      displayName: (user ? user.displayName : null),
      preferredfname: (user ? user.fname : null),
      preferredlname: (user ? user.lname : null),
      userName: (user ? user.fname + ' ' + user.lname : null),
      projectid: (project ? project.projectID : null),
      projectName: (project ? project.projectName : null),
      projectColor: '',
      scheduledate: (scheduleDate ? scheduleDate : this.selectedDate),
      comments: null,
      startdatetime: (scheduleDate ? scheduleDate : this.selectedDate),
      enddatetime: null,
      startTime: '',
      endTime: '',
      dayNumber: null,
      weekNumber: null,
      weekStart: null,
      weekEnd: null,
      dayOfWeek: 0,
      monthNumber: null,
      month: null,
      scheduleyear: null,
      scheduledHours: null,
      expr1: null,
      requestId: null,
      requestDetails: null,
      requestCodeId: null,
      requestCode: null,
      userid: null,
      trainedon: null,
      language: null,
      changed: true,
      saved: null,
      markedForDeletion: null,
      addTab: true,
      addInitial: true,
      invalid: true,
      entryBy: (user ? user.dempoid : null),
      entryDt: new Date(),
    };
  }

  //route add/subtract functions based on the calendar type
  public addDateUnitsToSelectedDate(unit: number): void {
    switch (this.currentTab) {
      case 'Day': {
        this.addDaysToSelectedDate(unit);
        break;
      }
      case 'Week': {
        this.addWeeksToSelectedDate(unit);
        break;
      }
      case 'Month': {
        this.addMonthsToSelectedDate(unit);
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
    let selectedDt: Date = new Date(this.selectedDate);
    selectedDt.setDate(selectedDt.getDate() + days);
    this.selectedDate = selectedDt;
    this.selectedDateFC = new FormControl(selectedDt.toISOString());

    //refresh the grid with the new date
    this.userSchedulesService.selectedDate.next(this.selectedDate);
    this.userSchedulesService.setAllUserSchedulesByAnchorDate(Utils.formatDateOnlyToString(this.selectedDate, true, true, true));
  }

  //add (or subract with a negative number) a week to the anchor date
  public addWeeksToSelectedDate(weeks: number): void {
    weeks = weeks * 7;
    let selectedDt: Date = new Date(this.selectedDate);
    selectedDt.setDate(selectedDt.getDate() + weeks);
    this.selectedDate = selectedDt;
    //set the week start so we can set the anchor date to it
    let selectedWeekStartAndEnd: IWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(this.selectedDate);
    //set the anchor date to the week start
    this.selectedDate = selectedWeekStartAndEnd.weekStart;
    this.selectedDateFC = new FormControl(this.selectedDate.toISOString());

    //refresh the grid with the new date
    this.setSelectedWeek();
    this.userSchedulesService.selectedDate.next(this.selectedDate);
    this.userSchedulesService.setAllUserSchedulesByAnchorDate(Utils.formatDateOnlyToString(this.selectedDate, true, true, true));
  }

  //add (or subract with a negative number) a month to the anchor date
  public addMonthsToSelectedDate(months: number): void {
    let selectedDt: Date = new Date(this.selectedDate);
    selectedDt.setMonth(selectedDt.getMonth() + months);
    selectedDt.setDate(1);
    this.selectedDate = selectedDt;
    this.selectedDateFC = new FormControl(this.selectedDate.toISOString());

    //refresh the grid with the new date
    this.userSchedulesService.selectedDate.next(this.selectedDate);
    this.userSchedulesService.setAllUserSchedulesByAnchorDate(Utils.formatDateOnlyToString(this.selectedDate, true, true, true));
  }

  setSelectedWeek(): void {
    this.selectedWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(this.selectedDate);

    this.selectedDateRange = new FormGroup({
      start: new FormControl(new Date(this.selectedWeekStartAndEnd.weekStart)),
      end: new FormControl(new Date(this.selectedWeekStartAndEnd.weekEnd)),
    });

  }

  fetchNewSchedules(selectedDate: FormControl): void {
    this.selectedDate = new Date(selectedDate.value);

    this.userSchedulesService.selectedDate.next(this.selectedDate);
    this.setSelectedWeek();
    this.userSchedulesService.setAllUserSchedulesByAnchorDate(Utils.formatDateOnlyToString(this.selectedDate, true, true, true));
  }

  //pagination
  //day
  handleDayPage(e: PageEvent): void {
    this.pageIndexDay = e.pageIndex;
    this.pageSizeDay = e.pageSize;
    this.setPagedDaySchedules();
  }

  setPagedDaySchedules(): void {
    this.pagedUsersDayLength = this.filteredUserSchedulesDay.length;
    const start = this.pageIndexDay * this.pageSizeDay;
    const end = (this.pageIndexDay + 1) * this.pageSizeDay;
    this.pagedUserSchedulesDay = this.filteredUserSchedulesDay.slice(start, end);
  }

  //week
  handleWeekPage(e: PageEvent): void {
    this.pageIndexWeek = e.pageIndex;
    this.pageSizeWeek = e.pageSize;
    this.setPagedWeekSchedules();
  }

  setPagedWeekSchedules(): void {
    this.pagedUsersWeekLength = this.filteredUserSchedulesWeek.length;
    const start = this.pageIndexWeek * this.pageSizeWeek;
    const end = (this.pageIndexWeek + 1) * this.pageSizeWeek;
    this.pagedUserSchedulesWeek = this.filteredUserSchedulesWeek.slice(start, end);
  }

  //month
  handleMonthPage(e: PageEvent): void {
    this.pageIndexMonth = e.pageIndex;
    this.pageSizeMonth = e.pageSize;
    this.setPagedMonthSchedules();
  }

  setPagedMonthSchedules(): void {
    this.pagedUsersMonthLength = this.filteredUserSchedulesMonth.length;
    const start = this.pageIndexMonth * this.pageSizeMonth;
    const end = (this.pageIndexMonth + 1) * this.pageSizeMonth;
    this.pagedUserSchedulesMonth = this.filteredUserSchedulesMonth.slice(start, end);
  }

  //custom
  handleCustomPage(e: PageEvent): void {
    this.pageIndexCustom = e.pageIndex;
    this.pageSizeCustom = e.pageSize;
    this.setPagedCustomSchedules();
  }

  setPagedCustomSchedules(): void {
    this.pagedUsersCustomLength = this.filteredUserSchedulesCustom.length;
    const start = this.pageIndexCustom * this.pageSizeCustom;
    const end = (this.pageIndexCustom + 1) * this.pageSizeCustom;
    this.pagedUserSchedulesCustom = this.filteredUserSchedulesCustom.slice(start, end);
  }

  //added
  handleAddedPage(e: PageEvent): void {
    this.pageIndexAdded = e.pageIndex;
    this.pageSizeAdded = e.pageSize;
    this.setPagedAddedSchedules();
  }

  //set paging variables
  setPagedAddedSchedules(): void {
    this.pagedUsersAddedLength = this.addedSchedules.length;
    const start = this.pageIndexAdded * this.pageSizeAdded;
    const end = (this.pageIndexAdded + 1) * this.pageSizeAdded;
    this.pagedUserSchedulesAdded = this.addedSchedules.slice(start, end);
  }

  getValidationMessages(): void {
    if (!this.selectedDate || !this.authenticatedUser)
      return;

    //get validation messages
    this.validationMessagesChecked = false;
    this.userSchedulesService.getUserValidationMessages(new Date(this.selectedDate), this.authenticatedUser.netID).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.validationMessages = <IValidationMessage[]>response.Subject;

          for (var x = 0; x < this.validationMessages.length; x++) {

            if (this.validationMessages[x].schedules) {
              for (var i = 0; i < (this.validationMessages[x].schedules || []).length; i++) {

                this.validationMessages[x].validationMessagesId = 0;
                (this.validationMessages[x].schedules || [])[i].startdatetime = new Date((this.validationMessages[x].schedules || [])[i].startdatetime || '');
                (this.validationMessages[x].schedules || [])[i].enddatetime = new Date((this.validationMessages[x].schedules || [])[i].enddatetime || '');
                (this.validationMessages[x].schedules || [])[i].startTime = Utils.formatDateToTimeString((this.validationMessages[x].schedules || [])[i].startdatetime, true) || '';
                (this.validationMessages[x].schedules || [])[i].endTime = Utils.formatDateToTimeString((this.validationMessages[x].schedules || [])[i].enddatetime, true) || '';

                let schedule: ISchedule = (this.validationMessages[x].schedules || [])[i];

                let startHours: number = ((((schedule.startdatetime || new Date()).getHours() * 60) + (schedule.startdatetime || new Date()).getMinutes()) / 60);
                let endHours: number = ((((schedule.enddatetime || new Date()).getHours() * 60) + (schedule.enddatetime || new Date()).getMinutes()) / 60);
                (this.validationMessages[x].schedules || [])[i].scheduledHours = endHours - startHours;

                if (this.allProjects) {
                  let projectId: number = (this.validationMessages[x].schedules || [])[i].projectid as number;
                  let scheduleProject: IProjectMin | undefined = this.allProjects.find(x => x.projectID == projectId);
                  if (scheduleProject) {
                    (this.validationMessages[x].schedules || [])[i].projectName = scheduleProject.projectName;
                  }
                }

              }

              const uniqueValue = (value: any, index: any, self: any) => {
                return self.indexOf(value) === index
              }

              this.validationMessages = this.validationMessages.filter(uniqueValue);

            }

          }

          this.validationMessagesChecked = true;
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  formatDateOnlyWithMonthNameToString(dateToFormat: Date | null): string | null {
    return Utils.formatDateOnlyWithMonthNameToString(dateToFormat);
  }

  formatDateMonthNameToString(dateToFormat: Date | null): string | null {
    return Utils.formatDateMonthNameToString(dateToFormat);
  }

  expandValidationMessages(validationMessagesExpanded: boolean): void {
    this.validationMessagesExpanded = validationMessagesExpanded;
    Utils.schedulerPopupDynamicSize(validationMessagesExpanded);
  }

  requestAutomation(schedules: ISchedule[]) {
    let requests: IRequest[] = [];

    for (var i = 0; i < schedules.length; i++) {
      //57 = absent
      //58 = sick
      //86 = Leaving Early
      //89 = Arriving Late
      let automatableProjects: number[] = [57, 58, 86, 89];

      //only whitelisted projects can result in automated requests
      //checking the project id against the initial project id allows us to skip schedules that did not have a change in the project so that we don't create duplicate requests
      if (!(automatableProjects.includes(schedules[i].projectid as number)
        && schedules[i].projectid !== schedules[i].initialProjectid)) {
        continue;
      }

      //set request code and type
      //4 = Unexcused Absence - Other
      //3 = Unexcused Absence - Sick
      //8 = Tardy - Leaving Early
      //7 = Tardy - arriving Late
      let requestCodeId: number = -999;
      let requestType: string = '';

      switch (schedules[i].projectid) {
        case 57:
          requestCodeId = 4;
          requestType = 'Absent';
          break;
        case 58:
          requestCodeId = 3;
          requestType = 'Sick';
          break;
        case 86:
          requestCodeId = 8;
          requestType = 'Leaving Early';
          break;
        case 89:
          requestCodeId = 7;
          requestType = 'Arriving Late';
          break;
      }

      let request: IRequest = {
        invalidFields: [],
        decisionId: 1,//schedule updated
        requestCodeId: requestCodeId,
        interviewerEmpId: schedules[i].dempoid,
        resourceTeamMemberId: this.authenticatedUser.netID,
        resourceTeamMemberName: this.authenticatedUser.displayName,
        requestId: 0,
        requestDate: new Date(),
        requestDetails: requestType + ': ' + Utils.formatDateOnlyWithMonthNameToString(schedules[i].startdatetime) + ' ' + schedules[i].startTime + ' - ' + schedules[i].endTime,
        notes: schedules[i].comments,
        modBy: this.authenticatedUser.netID,
        modDt: new Date(),
        entryBy: this.authenticatedUser.netID,
        entryDt: new Date()
      };

      requests.push(request);
    }

    if (requests.length > 0) {
      this.requestsService.saveRequests(requests).subscribe(
        response => {
          if (response.Status == 'Success') {
            //this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Request(s) saved successfully', ['OK']));
            let savedRequests: IRequest[] = <IRequest[]>response.Subject;
            this.requestsService.setAllRequests();
          } else {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save automated request(s)', ['OK']));
            this.errorMessage = response.Message;
            this.logsService.logError(this.errorMessage);
            this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
          }
        },
        error => {
          this.errorMessage = <string>(error.message);
          this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save automated request(s):<br />' + this.errorMessage, ['OK']));
        }
      );
    }

  }

  displayHoverMessage(event: any, schedule: ISchedule): void {

    if (schedule.hoverMessage
      || (schedule.validationMessages || []).length > 0) {
      let htmlMessage: string = '';

      if (schedule.hoverMessage) {
        htmlMessage = htmlMessage + schedule.hoverMessage;
      }

      //validaiton messages
      if (schedule.validationMessages) {
        if (schedule.validationMessages.length > 0) {
          htmlMessage = htmlMessage + '<p class="bold">Validation Messages:</p>';
          for (var i = 0; i < schedule.validationMessages.length; i++) {
            htmlMessage = htmlMessage + '<p style="margin-left: 5px;">' + schedule.validationMessages[i] + '</p>';

          }
        }
      }

      let hoverMessage: HTMLElement = <HTMLElement>document.getElementById('hover-message');
      hoverMessage.innerHTML = htmlMessage;

      this.globalsService.showHoverMessage.next(true);

      hoverMessage.style.top = (event.screenY) + 'px';
      hoverMessage.style.left = (event.screenX) + 'px';
    }

  }

  hideHoverMessage(): void {
    this.globalsService.showHoverMessage.next(false);
  }

  displaySchedulingLevelInfo(event: any): void {
    let htmlMessage: string = '';

    if (!this.currentUser) {
      this.currentUser = {} as User;
      htmlMessage = 'No scheduling level assigned.';
    }

    if (this.currentUser.schedulinglevel == 1) {
      htmlMessage = htmlMessage + '<p class="bold">Scheduling Level 1</p>';
      htmlMessage = htmlMessage + "<p><ul>";
      htmlMessage = htmlMessage + "<li>A shift schedule should be at least 4 hours in length.</li>";
      htmlMessage = htmlMessage + "<li>A shift schedule should be no more than 7 hours in length.</li>";
      htmlMessage = htmlMessage + "<li>A shift schedule for a weekday, Monday thru Friday, should begin at or after 1 PM.</li>";
      htmlMessage = htmlMessage + "<li>A shift schedule for Saturday should begin at or after 9 AM.</li>";
      htmlMessage = htmlMessage + "<li>A shift schedule for Sunday should begin at or after 12 noon.</li>";
      htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should at a minimum match their core hours total.</li>";
      htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should not exceed 20 hours total.</li>";
      htmlMessage = htmlMessage + "<li>An Interviewer's schedule should include 1 night shift, until at or after 9 PM, every other week.</li>";
      htmlMessage = htmlMessage + "<li>An Interviewer's schedule should include 1 weekend shift every other week.</li>";
      htmlMessage = htmlMessage + "<ul><li>A Friday night shift schedule with majority of hours after 5 PM, can only have 1 Friday night per month.</li>";
      htmlMessage = htmlMessage + "<li>A Saturday and/or Sunday shift schedule should be 6 hours minimum.</li></ul>";
      htmlMessage = htmlMessage + "</ul></p>";
    }

    if (this.currentUser.schedulinglevel == 2) {
      htmlMessage = htmlMessage + '<p class="bold">Scheduling Level 2</p>';
      htmlMessage = htmlMessage + "<p><ul>";
      htmlMessage = htmlMessage + "<li>A shift schedule should be at least 4 hours in length.</li>";
      htmlMessage = htmlMessage + "<li>A shift schedule cannot be exactly 8 hours in length.</li>";
      htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should at a minimum match their core hours total.</li>";
      htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should not exceed 40 hours total.</li>";
      htmlMessage = htmlMessage + "<li>An Interviewer's schedule should include 1 night shift, until at or after 9 PM, every other week.</li>";
      htmlMessage = htmlMessage + "<li>An Interviewer's schedule should include 1 weekend shift every other week.</li>";
      htmlMessage = htmlMessage + "<ul><li>A Friday night shift schedule with majority of hours after 5 PM, can only have 1 Friday night per month.</li>";
      htmlMessage = htmlMessage + "<li>A Saturday and/or Sunday shift schedule should be 6 hours minimum.</li></ul>";
      htmlMessage = htmlMessage + "</ul></p>";
    }

    if (this.currentUser.schedulinglevel == 3) {
      htmlMessage = htmlMessage + '<p class="bold">Scheduling Level 3</p>';
      htmlMessage = htmlMessage + "<p><ul>";
      htmlMessage = htmlMessage + "<li>A shift schedule cannot be exactly 8 hours in length.</li>";
      htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should at a minimum match their core hours total.</li>";
      htmlMessage = htmlMessage + "<li>An Interviewer's weekly schedule should not exceed 40 hours total.</li>";
      htmlMessage = htmlMessage + "</ul></p>";
    }

    let hoverMessage: HTMLElement = <HTMLElement>document.getElementById('hover-message');
    hoverMessage.innerHTML = htmlMessage;

    this.globalsService.showHoverMessage.next(true);

    hoverMessage.style.top = (event.screenY) + 'px';
    hoverMessage.style.left = event.clientX + 'px';
  }

  hideSchedulingLevelInfo(): void {
    this.globalsService.showHoverMessage.next(false);
  }

  checkReadOnly(schedule: ISchedule) {
    //if we can't get your user info, you can't edit schedules
    if (!this.authenticatedUser) {
      return true;
    }

    //before unlock date
    if ((Utils.formatDateOnly(schedule.startdatetime) || new Date()) < this.unlockDate
      && this.authenticatedUser.interviewer
      && !this.authenticatedUser.resourceGroup
      && !this.authenticatedUser.admin
      && !this.currentUser.canEdit) {
      return true;
    }

    return false;
  }

  blackFilterStyle() {
    var body = document.body,
      html = document.documentElement;

    var height = Math.max(body.scrollHeight, body.offsetHeight,
      html.clientHeight, html.scrollHeight, html.offsetHeight);

    return {
      'height': height + 'px',
    }
  }

  closeModalPopup(): void {
    this.globalsService.hidePopupMessage();
  }

  splitByPipe(stringToSplit: string): string[] {
    if (!stringToSplit) {
      return [];
    }

    return stringToSplit.split('|');
  }

}
