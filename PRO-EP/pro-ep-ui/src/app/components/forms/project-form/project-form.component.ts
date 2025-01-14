import { Component, Input, OnInit, Output, EventEmitter, HostListener } from '@angular/core';
import { Utils } from '../../../classes/utils';
import { IActionButton, IAuthenticatedUser, IDropDownValue, IForecastHours, IFormField, IFormFieldInstance, IFormFieldVariable, IProject, IProjectMin, IUser, IUserMin } from '../../../interfaces/interfaces';
import { AuthenticationService } from '../../../services/authentication/authentication.service';
import { CommunicationsHubService } from '../../../services/communications-hub/communications-hub.service';
import { ConfigurationService } from '../../../services/configuration/configuration.service';
import { GlobalsService } from '../../../services/globals/globals.service';
import { LogsService } from '../../../services/logs/logs.service';
import { ProjectsService } from '../../../services/projects/projects.service';
import { UsersService } from '../../../services/users/users.service';

@Component({
  selector: 'app-project-form',
  templateUrl: './project-form.component.html',
  styleUrls: ['./project-form.component.css']
})
export class ProjectFormComponent implements OnInit {
  @HostListener('window:beforeunload') onBeforeUnload(e: any) {

    if (this.changed || this.createForm) {
      e.preventDefault();
      e.returnValue = '';
    }

  }

  @Input() createForm: boolean = false;
  @Output() projectSaved = new EventEmitter<IProject>();

  authenticatedUser!: IAuthenticatedUser;
  selectedProject!: IProject;
  selectedProjectMin!: IProjectMin;
  forecastHours: any = {} as any;
  activeUsers!: IUserMin[];
  trainedOnUsers!: IUserMin[];
  notTrainedOnUsers!: IUserMin[];
  projectFields!: IFormFieldVariable[];
  projectFormFields: IFormFieldInstance[] = [];
  tab1ProjectFields!: IFormFieldInstance[];
  tab2ProjectFields!: IFormFieldInstance[];
  tab3ProjectFields!: IFormFieldInstance[];
  activeFormField!: IFormFieldInstance;
  errorMessage!: string;
  changed: boolean = false;
  readOnly!: boolean;
  invalid: boolean = false;
  tab1Invalid: boolean = false;
  tab2Invalid: boolean = false;
  tab3Invalid: boolean = false;
  tab4Invalid: boolean = false;
  forecastingInvalid: boolean = false;
  schedulingFrequency!: IFormFieldVariable;
  selectedForecastingMonth: number = 0;
  forecastMonths: string[] = [];

  constructor(private globalsService: GlobalsService,
    private authenticationService: AuthenticationService,
    private usersService: UsersService,
    private configurationService: ConfigurationService,
    private projectsService: ProjectsService,
    private logsService: LogsService,
    private communicationsHubService: CommunicationsHubService) {

    //get project form fields to build the forms
    this.configurationService.getProjectFields().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let projectFields: IFormFieldVariable[] = <IFormFieldVariable[]>response.Subject;
          this.projectFields = projectFields;

          //set selected project to a blank project object if creating a new object
          if (this.createForm) {
            this.selectedProject = this.blankProject();
            //this.selectedProject = {} as IProject;

            //set trained/not trained on if not already set and if we have already populated active projects
            if (this.activeUsers) {
              this.setTrainedOnNotTrainedOn();
            }

            //every time we set the selected project we will remap the project fields and reassign to tabs, but only if we have projectFields populated already
            if (this.projectFields) {
              this.mapProjectFieldsAndAssignTabs(this.selectedProject, this.projectFields);
            }
          }
          //if the selected project has been set but we haven't assigned project fields, let's map and assign the fields
          else if (this.selectedProject) {
            this.mapProjectFieldsAndAssignTabs(this.selectedProject, projectFields);
          }
        }
      }
    );

    //get active users
    this.usersService.allUsersMin.subscribe(
      allUsers => {
        this.activeUsers = allUsers.filter(x => x.active);
      }
    );

    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;

        this.readOnly = this.isReadOnly();
      }
    );

    //get scheduling frequency values
    this.configurationService.getFormField('SchedulingFrequency').subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.schedulingFrequency = <IFormFieldVariable>response.Subject;
        }
      }
    );

  }

  ngOnInit(): void {

    if (this.createForm) {
      this.selectedProject = this.blankProject();
      this.forecastHours = {} as IForecastHours;
    }

    this.setDefaultForecastHours();

    if (!this.createForm) {
      //subscribe to the selected project
      this.projectsService.selectedProject.subscribe(
        project => {
          this.selectedProject = project;
          if (this.selectedProject?.projectID) {

            //get forecast hours
            this.projectsService.getForecastingByProjectId(this.selectedProject.projectID).subscribe(
              response => {
                if ((response.Status || '').toUpperCase() == 'SUCCESS') {
                let currentDate: Date = new Date();
                this.forecastMonths[0] = Utils.formatDateToMonthYear(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1), true, true);
                this.forecastMonths[1] = Utils.formatDateToMonthYear(new Date(currentDate.getFullYear(), currentDate.getMonth(), 1), true, true);
                for (var i = 1; i < 13; i++) {
                  this.forecastMonths[i + 1] = Utils.formatDateToMonthYear(new Date(currentDate.getFullYear(), currentDate.getMonth() + i, 1), true, true);
                }
                  this.forecastHours = <IForecastHours>response.Subject;

                  this.setDefaultForecastHours();

                }
              }
            );

          }

          //set trained/not trained on if not already set and if we have already populated active projects
          if (this.activeUsers) {
            this.setTrainedOnNotTrainedOn();
          }

          //every time we set the selected project we will remap the project fields and reassign to tabs, but only if we have projectFields populated already
          if (this.projectFields && !this.createForm) {
            this.mapProjectFieldsAndAssignTabs(project, this.projectFields);
          }
        }
      );
    }
  }

  setDefaultForecastHours(): void {
    if (this.forecastHours) {
      if (this.forecastHours.month1)
        return;
    }

    let fh: any = {};
    let currentDate: Date = new Date();
    fh.month1 = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1);
    fh.month2 = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
    for (var i = 1; i < 13; i++) {
      fh[`month${(i + 2)}`] = new Date(currentDate.getFullYear(), currentDate.getMonth() + (i), 1);
    }
    if (this.selectedProject) {
      if (this.selectedProject.projectID) {
        fh.projectId = this.selectedProject.projectID;
      }
    }
    this.forecastHours = fh;

  }

  //map/assign project and project fields to tabs
  mapProjectFieldsAndAssignTabs(project: any, projectFields: IFormFieldVariable[]): void {
    //assign values to form field instances
    this.projectFormFields = [];
    for (var property in project) {
      let propertyFormFieldVariable: IFormFieldVariable | undefined = projectFields.find(x => (x.formField?.columnName ? x.formField?.columnName.toLowerCase() : null) == property.toLowerCase());
      if (propertyFormFieldVariable) {
        let value: any = project[property];

        let projectFormField: IFormFieldInstance = {
          value: value,
          formFieldVariable: propertyFormFieldVariable,
          invalid: false,
          missingRequired: false,
          validationError: false,
          validationMessage: ''
        };
        if (property == 'rescue' || property == 'orphan') {
          projectFormField.value = projectFormField.value !== null ? projectFormField.value.toString() : null;
        }

        this.projectFormFields.push(projectFormField);
      }
    }

    this.projectFormFields.sort((x, y) => {
      return x.formFieldVariable.formField.formOrder - y.formFieldVariable.formField.formOrder;
    });

    this.activeFormField = this.projectFormFields.find(x => x.formFieldVariable.formField?.columnName == 'active') as IFormFieldInstance;
    //assign to tab arrays
    this.tab1ProjectFields = this.projectFormFields.filter(x => x.formFieldVariable.formField.tab == '1');
    this.tab2ProjectFields = this.projectFormFields.filter(x => x.formFieldVariable.formField.tab == '2');
    this.tab3ProjectFields = this.projectFormFields.filter(x => x.formFieldVariable.formField.tab == '3');

    this.validateRequiredFields();

  }

  //map field values back to project
  mapProjectFieldsBackToProject(): void {
    //assign values to project form field instances
    let sp: any = this.selectedProject;
    for (var property in sp) {
      let propertyFormFieldInstance: IFormFieldInstance | undefined = this.projectFormFields.find(x => (x.formFieldVariable.formField?.columnName ? x.formFieldVariable.formField?.columnName.toLowerCase() : null) == property.toLowerCase());

      if (propertyFormFieldInstance) {
        let value: any = propertyFormFieldInstance.value;

        //turn arrays (comboboxes/multi-select types) into pipe-delimited strings
        if (propertyFormFieldInstance.formFieldVariable.formField.fieldType.includes('combo')) {
          value = <string[]>(value).join('|');
        }

        //set the value
        sp[property] = value;
      }
    }

    this.selectedProject = sp;
  }

  setChanged(): void {
    this.changed = true;
    this.globalsService.currentChanges.next(true);
    this.validateRequiredFields();
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

  validateRequiredFields(): void {
    //set fields as invalid
    for (var i = 0; i < this.projectFormFields.length; i++) {
      if (this.projectFormFields[i].formFieldVariable.formField.required) {
        //if null
        if (!this.projectFormFields[i].value) {
          this.invalid = true;
          this.projectFormFields[i].invalid = true;
        } else if (this.projectFormFields[i].formFieldVariable.formField.fieldType == 'textbox'
          || this.projectFormFields[i].formFieldVariable.formField.fieldType == 'longtextbox') {
          //if text is blank or empty
          if (this.projectFormFields[i].value.replace(/\s/g, '') == '') {
            this.invalid = true;
            this.projectFormFields[i].invalid = true;
          } else {
            this.invalid = false;
            this.projectFormFields[i].invalid = false;
          }
        } else {
          this.projectFormFields[i].invalid = false;
        }
      }
    }

    //tab validation
    this.invalid = this.projectFormFields.filter(x => (x.formFieldVariable.formField.required && x.invalid)).length > 0;
    if (this.tab1ProjectFields.filter(x => x.invalid).length > 0) { this.tab1Invalid = true; } else { this.tab1Invalid = false; }
    if (this.tab2ProjectFields.filter(x => x.invalid).length > 0) { this.tab2Invalid = true; } else { this.tab2Invalid = false; }
    if (this.tab3ProjectFields.filter(x => x.invalid).length > 0) { this.tab3Invalid = true; } else { this.tab3Invalid = false; }

  }

  //save project profile
  saveProjectProfile(): void {
    this.mapProjectFieldsBackToProject();

    //set modified metadata
    this.selectedProject.modBy = this.authenticatedUser.netID;
    this.selectedProject.modDt = new Date();
    this.selectedProject.active = (this.selectedProject.active ? 1 : 0);
    //set created metadata (if applicable)
    if ((this.selectedProject.projectID || 0) < 1) {
      this.selectedProject.entryBy = this.authenticatedUser.netID;
      this.selectedProject.entryDt = new Date();
    }

    this.selectedProjectMin = {
      projectAbbr: this.selectedProject.projectAbbr || '',
      projectColor: this.selectedProject.projectColor || '',
      projectID: this.selectedProject.projectID as number,
      projectName: this.selectedProject.projectName || '',
      active: (this.selectedProject.active == 1 ? true : false),
      projectStatus: (this.selectedProject.projectStatus == '1' ? true : false),
      projectType: '',
      notTrainedOnUsers: this.selectedProject.notTrainedOnUsers,
      trainedOnUsers: this.selectedProject.trainedOnUsers,
      trainedOnUsersMin: this.selectedProject.trainedOnUsersMin,
      projectDisplayId: this.selectedProject.projectDisplayId || ''
    };

    //pass to save project api to save
    this.projectsService.saveProject(this.selectedProject).subscribe(
      response => {

        //if saving a new project, get the project response so we can extract and use the new project id
        if (this.selectedProject.projectID == 0 && response.Subject) {
          let projectResponse: IProject = <IProject>response.Subject;
          if (projectResponse) {
            this.selectedProject.projectID = projectResponse.projectID;
          }
        }

        //pass to save project trained on api to save
        this.projectsService.saveTrainedOn(this.selectedProjectMin).subscribe(
          response => {

            //console.log(this.selectedProject.ProjectDisplayId.split('|').includes('4'));
            //console.log(this.selectedProject.ProjectDisplayId.split('|'));
            //console.log(this.selectedProject.ProjectDisplayId);
            console.log(this.selectedProject);
            if ((this.selectedProject.projectDisplayId || '').split('|').includes('4')) {
              this.configurationService.addDefaultCommHubConfig(this.selectedProject.projectID as number).subscribe(
                response => {

                },
                error => {
                  this.errorMessage = <string>(error.message);
                  //this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to set default Communications Hub fields for this project:<br />' + this.errorMessage, ['OK']));
                  this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
                });
            } else {
              this.configurationService.deleteCommHubConfig(this.selectedProject.projectID as number).subscribe(
                response => {

                },
                error => {
                  this.errorMessage = <string>(error.message);
                  //this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to set default Communications Hub fields for this project:<br />' + this.errorMessage, ['OK']));
                  this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
                });
            }

            //sync users trained on
            this.usersService.setAllUsersMin();
          },
          error => {
            this.errorMessage = <string>(error.message);
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save project trained on:<br />' + this.errorMessage, ['OK']));
            this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
          });

        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let savedProject: IProject = <IProject>response.Subject;

          //set the project id for forecast hours so they will save
          this.forecastHours.projectId = savedProject.projectID as number;

          //save forecasting
          this.projectsService.saveForecasting([this.forecastHours]).subscribe(
            response => {
              if ((response.Status || '').toUpperCase() == 'SUCCESS') {
                this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Project saved successfully', ['OK']));
              } else {
                this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Partial Success', 'Project saved successfully but there may have been an issue saving forecasting', ['OK']));
              }

              //set changed to false to re-disable the save button
              this.changed = false;
              this.globalsService.currentChanges.next(false);

              //set ID for "selected" project if project is newly added
              if ((this.selectedProject.projectID || 0) < 1) {
                let subject: IProject = <IProject>response.Subject;
                this.selectedProject.projectID = subject.projectID;
              }

              this.usersService.setAllUsersMin();
              this.projectsService.setSelectedProject(this.selectedProject.projectID as number);
              this.projectSaved.emit(this.selectedProject);


            });

        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save project', ['OK']));
          this.logsService.logError(this.errorMessage);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save project:<br />' + this.errorMessage, ['OK']));
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  isReadOnly(): boolean {
    if (this.authenticatedUser?.resourceGroup || this.authenticatedUser?.admin) {
      return false;
    } else {
      return true;
    }
  }

  trainedOnChange(user: IUserMin): void {
    //sort new list of trained on
    this.trainedOnUsers.sort((x, y) => {
      return ((x.displayName || '') < (y.displayName || '')) ? -1 : ((x.displayName || '') > (y.displayName || '')) ? 1 : 0;
    });

    //sort new list of not trained on
    this.notTrainedOnUsers.sort((x, y) => {
      return ((x.displayName || '') < (y.displayName || '')) ? -1 : ((x.displayName || '') > (y.displayName || '')) ? 1 : 0;
    });


    //this.selectedProjectMin = this.blankProjectMin();
    //this.selectedProjectMin.ProjectID = this.selectedProject.ProjectId;
    //this.selectedProjectMin.TrainedOnUsersMin = this.trainedOnUsers;
    this.selectedProject.trainedOnUsers = this.trainedOnUsers.filter(x => x.changed).map(x => x.dempoid);
    this.selectedProject.notTrainedOnUsers = this.notTrainedOnUsers.filter(x => x.changed).map(x => x.dempoid);

    this.changed = true;
    this.globalsService.currentChanges.next(true);
  }

  setTrainedOnNotTrainedOn(): void {
    if (this.selectedProject?.projectID) {
      //trained on
      this.trainedOnUsers = this.activeUsers.filter(x => x.trainedOnArray.includes((this.selectedProject.projectID ? this.selectedProject.projectID.toString() : '')));
      //not trained on
      this.notTrainedOnUsers = this.activeUsers.filter(x => !x.trainedOnArray.includes((this.selectedProject.projectID ? this.selectedProject.projectID.toString() : '').toString()));

      this.selectedProject.trainedOnUsersMin = this.trainedOnUsers;
      this.selectedProject.trainedOnUsers = this.trainedOnUsers.map(x => x.dempoid);
      this.selectedProject.notTrainedOnUsers = this.notTrainedOnUsers.map(x => x.dempoid);
    }
  }

  //open modal scheduler
  openProjectSchedule(): void {
    //tab index of 0 = Add schedule tab
    this.globalsService.showContextualPopup(0, null, this.selectedProject.projectName, null);
  }

  blankProject(): IProject {
    return {
      projectID: 0,
      projectName: null,
      projectLeadId: null,
      projectLead: null,
      projectCoordinatorId: null,
      projectCoordinator: null,
      active: null,
      projectStatus: null,
      projectDisplayId: null,
      interview: null,
      projectColor: null,
      projectColorIview: null,
      projectedStartDate: null,
      projectedEndDate: null,
      projectEpmcode: null,
      projectType: null,
      sponsor: null,
      principalInvestigator: null,
      totalBudget: null,
      comments: null,
      scheduleDisplay: null,
      ssdatabaseName: null,
      forecast: null,
      tollFreeNumber: null,
      studyEmailAddress: null,
      pdriveLocation: null,
      tdriveLocation: null,
      irbnumber: null,
      fundCode: null,
      sisterCode: null,
      passThruCode: null,
      rescue: null,
      orphan: null,
      edcsystem: null,
      therapeuticArea: null,
      projectAbbr: null,
      projectManager: null,
      faxNumber: null,
      faxLocation: null,
      maxEnrollment: null,
      firstEnrollDate: null,
      visitDate: null,
      totalProjected: null,
      actualFollowUp: null,
      numofIntervals: 0,
      entryFormName: null,
      entryDt: null,
      entryBy: null,
      modDt: null,
      modBy: null,
    };
  }

  blankProjectMin(): IProjectMin {
    return {
      projectID: 0,
      projectName: '',
      projectAbbr: '',
      active: null,
      projectColor: '',
      projectType: null,
      projectStatus: null,
      projectDisplayId: ''
    };
  }

  formatDateToShortMonthYear(dateToFormat: Date | null): string | null {
    if (!dateToFormat) {
      return null;
    }
console.log('dateToFormat :', dateToFormat);
    return Utils.formatDateToMonthYear(dateToFormat, true, true);

  }

  formatDateToNamedMonthYear(dateToFormat: Date | null): string | null {
    if (!dateToFormat) {
      return null;
    }

    return Utils.formatDateToMonthYear(dateToFormat, false, false, true);

  }

  //function to return list of numbers from 0 to n-1
  numSequence(n: number): Array<number> {
    return Array(n);
  }

  compareSelectValues(v1: any, v2: any): boolean {
    return (v1 || '').toString() === (v2 || '').toString();
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

}
