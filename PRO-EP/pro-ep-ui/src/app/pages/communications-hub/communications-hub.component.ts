import { ChangeDetectorRef, Component, HostListener, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';
import { Utils } from '../../classes/utils';
import { IAllConfiguration, IAuthenticatedUser, ICommunicationsHub, ICommunicationsHubEntry, IDropDownValue, IFormField, IFormFieldInstance, IFormFieldVariable, IProject, IProjectMin, IRequest, IUser, IUserMin, IWeekStartAndEnd } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { GlobalsService } from '../../services/globals/globals.service';
import { RequestsService } from '../../services/requests/requests.service';
import { UsersService } from '../../services/users/users.service';
import * as html2pdf from 'html2pdf.js';
import { LogsService } from '../../services/logs/logs.service';
import { CommunicationsHubService } from '../../services/communications-hub/communications-hub.service';
import { ProjectsService } from '../../services/projects/projects.service';

@Component({
  selector: 'app-communications-hub',
  templateUrl: './communications-hub.component.html',
  styleUrls: ['./communications-hub.component.css']
})
export class CommunicationsHubComponent implements OnInit {
  @HostListener('window:beforeunload') onBeforeUnload(e: any) {

    if (this.newCommHubEntry.changed || this.commHubEntriesChanged) {
      e.preventDefault();
      e.returnValue = '';
    }

  }

  errorMessage: string = '';
  noResultsMessage: string = 'Loading communications hub entries...';
  authenticatedUser!: IAuthenticatedUser;
  requestFormFields: IFormFieldVariable[] = [];
  allUsers: IUserMin[] = [];
  allProjects: IProjectMin[] = [];
  authenticatedUserProfile: IUserMin = {} as IUserMin;
  trainedOnCommHubProject: boolean = false;
  activeUsers: IUserMin[] = [];
  filteredUsersDv: IDropDownValue[] = [];
  trainedOnUsers: IDropDownValue[] = [];
  commHubTableColumns: string[] = [];
  allCommHubEntries: ICommunicationsHubEntry[] = [];
  filteredCommHubEntries: ICommunicationsHubEntry[] = [];
  newCommHubEntry: ICommunicationsHubEntry = {
    fieldValues: [],
    invalidFields: []
  };
  selectedCommHubEntry: ICommunicationsHubEntry = {} as ICommunicationsHubEntry;
  commHubFormFields: IFormFieldVariable[] = [];
  filteredCommHubFormFields: IFormFieldVariable[] = [];
  newCommHubFormFieldInstances: IFormFieldInstance[] = [];
  selectedCommHubFormFieldInstances: IFormFieldInstance[] = [];
  commHubNoResultsMessage: string = '';
  commHubEntriesChanged: boolean = false;
  commHubEntriesInvalid: boolean = false;
  showModalPopup: boolean = false;
  showEditPopup: boolean = false;
  showViewPopup: boolean = false;
  commHubProject: string = '';
  commHubProjectTitle: string = '';
  commHubProjectTitleEdit: string = '';
  commHubProjectTitleView: string = '';
  currentDate: Date = new Date();
  exportSelection: any = new FormControl();

  //request pagination
  pageSizeOptions = [5, 10, 20, 50, 100];
  //added
  pageIndex = 0;
  pageSize = 10;
  pagedLength = 0;
  pagedcommHubEntries: ICommunicationsHubEntry[] = [];

  commHubProjects: IProjectMin[] = [];
  selectedCommHubProject: IProject = {} as IProject;
  selectedCommHubProjectId: number = 0;

  //global filter variables
  globalSearch: string = '';
  phoneSearch: string = '';
  entryDtFilter: string = '';

  //sort variables
  sortFormFieldId: number = 0;
  sortDescending: boolean = true;

  constructor(private cdref: ChangeDetectorRef,
    private globalsService: GlobalsService,
    private projectsService: ProjectsService,
    private communicationsHubService: CommunicationsHubService,
    private configurationService: ConfigurationService,
    private usersService: UsersService,
    private authenticationService: AuthenticationService,
    private logsService: LogsService) {

    this.applyFilters();

    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;

        this.setAuthenticatedUserProfile();

        this.restrictProjectsToTrainedOn();
      }
    );

    //get all projects
    this.projectsService.allProjectsMin.subscribe(
      allProjects => {
        this.allProjects = allProjects.filter(x => x.active);

        this.setAuthenticatedUserProfile();

      }
    );

    //get active users
    this.usersService.allUsersMin.subscribe(
      allUsers => {
        this.allUsers = allUsers;
        this.activeUsers = allUsers.filter(x => x.active);
        //this.trySetRequestValues();
        this.filteredUsersDv = Utils.convertObjectArrayToDropDownValues(this.allUsers, 'Dempoid', 'DisplayName');

        this.setAuthenticatedUserProfile();

        this.restrictProjectsToTrainedOn();
      }
    );

  }

  ngAfterContentChecked() {
    this.cdref.detectChanges();
  }

  ngOnInit(): void {
    //set current page for navigation menu to track
    this.globalsService.selectedPage.next('communications-hub');

    //get comm hub projects
    this.projectsService.allProjectsMin.subscribe(
      allProjectsMin => {
        //this.commHubProjects = allProjectsMin.filter(x => x.DisplayedIn.split('|').includes('4'));
        this.commHubProjects = allProjectsMin.filter(x => (x.projectDisplayId.split('|').includes('4') && (x.projectType || '').toUpperCase() !== 'ADMINISTRATIVE' && x.active));

        this.restrictProjectsToTrainedOn();
      },
      error => this.errorMessage = <any>error
    );

    //get comm hub config
    this.getCommHubConfig();

    //get comm hub entries
    this.getCommHubEntries();
  }

  formatDateOnlyToString(dateToFormat: Date | undefined): string {
    if (!dateToFormat) {
      return '';
    }
    return (Utils.formatDateOnlyToString(dateToFormat) || '');
  }

  setNoResultsMessage(customMessage: string | undefined = undefined): void {
    if (customMessage) {
      this.noResultsMessage = customMessage;
      return;
    }

    if (this.filteredCommHubEntries.length < 1) {
      this.noResultsMessage = 'No requests found...';
    } else {
      this.noResultsMessage = 'Loading requests...';
    }
  }

  compareSelectValues(v1: any, v2: any): boolean {
    return (v1 || '').toString() === (v2 || '').toString();
  }

  applyFilters(): void {

    //filter by selected project id
    if (this.selectedCommHubProjectId)
      this.filteredCommHubEntries = this.allCommHubEntries.filter(x => x.fieldValues[0].projectId == this.selectedCommHubProjectId);

    //-----------------------------
    //filtering
    //-----------------------------
    //global search
    if (this.globalSearch.length > 0) {
      let globalSearchLocal: string = this.globalSearch;
      let commHubFormFieldsLocal: IFormFieldVariable[] = this.commHubFormFields;

      this.filteredCommHubEntries = this.filteredCommHubEntries.filter(
        function (commHub, index, array) {
          let commHubEntries: ICommunicationsHub[] = [];

          //entry date
          if (new Date(globalSearchLocal) instanceof Date && !isNaN(new Date(globalSearchLocal).valueOf())) {
            if ((Utils.formatDateOnlyToString(commHub.entryDt) + ' ' + Utils.formatDateToTimeString(commHub.entryDt, true)).includes(globalSearchLocal))
              return true;
          }

          commHubEntries = commHub.fieldValues.filter(
            function (commHubField, index, array) {
              let formFieldVariable: IFormFieldVariable = commHubFormFieldsLocal.find(x => x.formField.formFieldId == commHubField.formFieldId) as IFormFieldVariable;

              if (!commHubField.value)
                return false;

              let fieldValueString: string = (commHubField.value.toString() as string);

              if (fieldValueString.length < 1)
                return false;

              //phone handling
              if (formFieldVariable.formField.fieldType == 'phone') {
                if (globalSearchLocal.replace(/\D/g, '').length < 1)
                  return false;

                if (fieldValueString.replace(/\D/g, '').includes(globalSearchLocal.replace(/\D/g, '')))
                  return true;
                else
                  return false;
              }

              //date handling
              if (['dateonly', 'datetime'].includes(formFieldVariable.formField.fieldType)) {
                if (new Date(commHubField.value) instanceof Date && !isNaN(new Date(commHubField.value).valueOf())) {
                  fieldValueString = Utils.formatDateOnlyToString(commHubField.value) + ' ' + Utils.formatDateToTimeString(commHubField.value, true);

                  if (fieldValueString.toUpperCase().includes(globalSearchLocal.toUpperCase()))
                    return true;
                  else
                    return false;
                } else {
                  return false;
                }
              }

              //select/multi select handling
              if (['selectbox', 'combobox', 'trainedonusers'].includes(formFieldVariable.formField.fieldType)) {

                //multi select fields
                if (fieldValueString.includes('|')) {
                  let fieldValueArray: string[] = fieldValueString.split('|');
                  let returnValue: boolean = false;
                  for (var j = 0; j < fieldValueArray.length; j++) {
                    let dropdownValue: IDropDownValue | undefined = (formFieldVariable.dropDownValues || []).find(x => (x.codeValues || '').toString() == fieldValueArray[j]);

                    if (dropdownValue)
                      if ((dropdownValue.dropDownItem || '').toUpperCase().includes(globalSearchLocal.toUpperCase()))
                        returnValue = true;
                  }

                  return returnValue;
                }
                //single select fields
                else {
                  let dropdownValue: IDropDownValue | undefined = (formFieldVariable.dropDownValues || []).find(x => (x.codeValues || '').toString() == fieldValueString);
                  if (!dropdownValue)
                    return false;

                  if ((dropdownValue.dropDownItem || '').toUpperCase().includes(globalSearchLocal.toUpperCase()))
                    return true;
                  else
                    return false;
                }
              }

              if (fieldValueString.toUpperCase().includes(globalSearchLocal.toUpperCase()))
                return true;
              else
                return false;

            });

          return (commHubEntries ? commHubEntries.length > 0 : false);
        });
    }

    //phone search
    if (this.phoneSearch.length > 0) {

      let phoneSearchLocal: string = this.phoneSearch;
      let commHubFormFieldsLocal: IFormFieldVariable[] = this.commHubFormFields;

      this.filteredCommHubEntries = this.filteredCommHubEntries.filter(
        function (commHub, index, array) {
          let commHubEntries: ICommunicationsHub[] = [];

          commHubEntries = commHub.fieldValues.filter(
            function (commHubField, index, array) {
              let formFieldVariable: IFormFieldVariable = commHubFormFieldsLocal.find(x => x.formField.formFieldId == commHubField.formFieldId) as IFormFieldVariable;

              if (!formFieldVariable)
                return false;

              if (formFieldVariable.formField.fieldType !== 'phone')
                return false;

              if (!commHubField.value)
                return false;

              let fieldValueString: string = (commHubField.value.toString() as string);

              if (fieldValueString.length < 1)
                return false;

              if (fieldValueString.replace(/\D/g, '').includes(phoneSearchLocal.replace(/\D/g, '')))
                return true;
              else
                return false;
            });

          return (commHubEntries ? commHubEntries.length > 0 : false);
        });
    }

    //entry date filter
    if (this.entryDtFilter.length > 0) {
      this.filteredCommHubEntries = this.filteredCommHubEntries.filter(x => (Utils.formatDateOnlyToString(x.entryDt) + ' ' + Utils.formatDateToTimeString(x.entryDt, true)).includes(this.entryDtFilter));
    }

    //config field header search
    for (var i = 0; i < this.filteredCommHubFormFields.length; i++) {
      if (this.filteredCommHubFormFields[i].filterValue) {
        let filterValue: string = (this.filteredCommHubFormFields[i].filterValue ? this.filteredCommHubFormFields[i].filterValue.toString() : '') as string;
        let filterValueArray: string[] = (Array.isArray(this.filteredCommHubFormFields[i].filterValue) ? (this.filteredCommHubFormFields[i].filterValue as string[]) : []);
        let formFieldId: number = this.filteredCommHubFormFields[i].formField.formFieldId;

        if (filterValue.length > 0) {

          this.filteredCommHubEntries = this.filteredCommHubEntries.filter(
            function (commHub, index, array) {
              let commHubEntries: ICommunicationsHub[] = [];

              //single and multi-select handling
              if (filterValueArray.length > 0)
                commHubEntries = commHub.fieldValues.filter(
                  function (commHubField, index, array) {
                    if (commHubField.formFieldId !== formFieldId)
                      return false;

                    if (!commHubField.value)
                      return false;

                    let fieldValueString: string = (commHubField.value.toString() as string);

                    if (fieldValueString.length < 1)
                      return false;

                    //array within array for multi select fields
                    if (fieldValueString.includes('|')) {
                      let fieldValueArray: string[] = fieldValueString.split('|');
                      let returnValue: boolean = false;
                      for (var j = 0; j < fieldValueArray.length; j++) {
                        if (filterValueArray.includes(fieldValueArray[j]))
                          returnValue = true;
                      }

                      return returnValue;
                    }
                    //single select fields
                    else {
                      return filterValueArray.includes(commHubField.value as string);
                    }
                  });
              //all other - non-select/multi select
              else
                commHubEntries = commHub.fieldValues.filter(
                  function (commHubField, index, array) {
                    if (commHubField.formFieldId !== formFieldId)
                      return false;

                    if (!commHubField.value)
                      return false;

                    let fieldValueString: string = (commHubField.value.toString() as string);

                    if (fieldValueString.length < 1)
                      return false;

                    if (new Date(commHubField.value) instanceof Date && !isNaN(new Date(commHubField.value).valueOf()))
                      fieldValueString = Utils.formatDateOnlyToString(commHubField.value) + ' ' + Utils.formatDateToTimeString(commHubField.value, true);

                    if (fieldValueString.toUpperCase().includes(filterValue.toUpperCase()))
                      return true;
                    else
                      return false;

                  });
              //commHubEntries = commHub.FieldValues.filter(y => (y.FormFieldId == formFieldId && (y.Value ? ((y.Value as string).toUpperCase().includes(filterValue.toUpperCase()) ? true : false) : false)));

              return (commHubEntries ? commHubEntries.length > 0 : false);
            });

        }
      }
    }

    //-----------------------------
    //sorting
    //-----------------------------
    let sortDescendingLocal: boolean = this.sortDescending;

    if (this.filteredCommHubEntries.length > 0) {
      //sort by entry date (default)
      if (this.sortFormFieldId == 0) {
        this.filteredCommHubEntries.sort(
          function (x, y) {

            let firstDate: Date = new Date(x.entryDt || '');
            let secondDate: Date = new Date(y.entryDt || '');

            //handle nulls and ties first
            if (firstDate === null)
              return 1;
            if (secondDate === null)
              return -1;
            if (firstDate.getTime() === secondDate.getTime())
              return 0;

            if (sortDescendingLocal) {
              firstDate = new Date(y.entryDt || '');
              secondDate = new Date(x.entryDt || '');
            }

            return firstDate.getTime() - secondDate.getTime();
          });
      }
      //sort by dynamic field
      else {
        let sortFormField: IFormFieldVariable = this.filteredCommHubFormFields.find(x => x.formField.formFieldId == this.sortFormFieldId) as IFormFieldVariable;
        if (sortFormField) {
          this.filteredCommHubEntries.sort(
            function (x, y) {
              let firstCommHub: ICommunicationsHub = x.fieldValues.find(z => z.formFieldId == sortFormField.formField.formFieldId) as ICommunicationsHub;
              let secondCommHub: ICommunicationsHub = y.fieldValues.find(z => z.formFieldId == sortFormField.formField.formFieldId) as ICommunicationsHub;

              //handle nulls and ties first
              if (firstCommHub.value === '' || firstCommHub.value === null)
                return 1;
              if (secondCommHub.value === '' || secondCommHub.value === null)
                return -1;
              if (firstCommHub.value === secondCommHub.value)
                return 0;

              //date types
              let dateTypes: string[] = ['dateonly', 'datetime'];
              if (dateTypes.includes(sortFormField.formField.fieldType)) {
                let firstDate: Date = new Date(firstCommHub.value);
                let secondDate: Date = new Date(secondCommHub.value);

                if (sortDescendingLocal) {
                  firstDate = new Date(secondCommHub.value);
                  secondDate = new Date(firstCommHub.value);
                }
                return firstDate.getTime() - secondDate.getTime();
              }
              //all other types - for now
              else {
                //descending/ascending
                if (sortDescendingLocal)
                  return ('' + secondCommHub.value).localeCompare(firstCommHub.value);
                else
                  return ('' + firstCommHub.value).localeCompare(secondCommHub.value);
              }
            });
        }
      }
    }



    ////make sure requests are sorted by date
    //this.filteredRequests = this.allRequests.sort(function (x, y) {
    //  if (Utils.formatDateOnly(new Date(x.RequestDate)) < Utils.formatDateOnly(new Date(y.RequestDate))) { return 1; }
    //  else if (Utils.formatDateOnly(new Date(x.RequestDate)) > Utils.formatDateOnly(new Date(y.RequestDate))) { return -1; }
    //  else {
    //    if (x.RequestId < y.RequestId) return 1;
    //    else if (x.RequestId > y.RequestId) return -1;
    //    else return 0;
    //  }
    //});

    ////reset filters to all requests
    //this.filteredRequests = [...this.allRequests];

    ////request type
    //if (!this.requestTypeFilter.value.includes('0')) {
    //  this.filteredRequests = this.filteredRequests.filter(x => this.requestTypeFilter.value.includes((x.RequestCodeId ? x.RequestCodeId.toString() : '')));
    //}

    ////interviewer
    //if (!this.interviewerFilter.value.includes('0')) {
    //  this.filteredRequests = this.filteredRequests.filter(x => this.interviewerFilter.value.includes(x.InterviewerEmpId));
    //}

    ////decision
    //if (!this.decisionFilter.value.includes('0')) {
    //  this.filteredRequests = this.filteredRequests.filter(x => this.decisionFilter.value.includes((x.DecisionId ? x.DecisionId.toString() : '')));
    //}

    ////date range filtering
    //if (this.selectedDateRange.value) {
    //  if (this.selectedDateRange.value.start && this.selectedDateRange.value.end) {
    //    this.filteredRequests = this.filteredRequests.filter(x => (Utils.formatDateOnly(x.RequestDate) >= Utils.formatDateOnly(this.selectedDateRange.value.start)
    //      && Utils.formatDateOnly(x.RequestDate) <= Utils.formatDateOnly(this.selectedDateRange.value.end)));
    //  }
    //}

    //set paging
    this.setPagedCommHubEntries();
  }

  setActiveSort(sortFormFieldId: number, sortDescending: boolean): void {
    this.sortFormFieldId = sortFormFieldId;
    this.sortDescending = sortDescending;

    this.applyFilters();
  }

  commHubEntriesFilterChange(selectedValues: FormControl): void {
    let selectedValuesArray: string[] = selectedValues.value;
    //this.selectedProjects = this.activeProjects.filter(x => selectedValuesArray.includes(x.ProjectID.toString()));
    //this.selectedProjects = this.activeProjects.filter(x => selectedValuesArray.includes(x.ProjectID.toString())).map(x => x.ProjectID.toString());

    ////if "Any Project" set, uncheck trained on/not trained on radio and disable
    //if (selectedValuesArray.length == 1) {
    //  if (selectedValuesArray[0] == '0') {
    //    this.trainedOnRadio = null;
    //    this.trainedOnRadioDisabled = true;
    //  }
    //  //else, set disabled to false
    //  else {
    //    this.trainedOnRadio = 1;
    //    this.trainedOnRadioDisabled = false;
    //    this.trainedOnRadioToggle();
    //  }
    //}

    ////initiate user filtering
    //this.filterUserList();
    this.applyFilters();
  }

  resetDefaultFilters(): void {
    this.globalSearch = '';
    this.phoneSearch = '';
    this.entryDtFilter = '';

    for (var i = 0; i < this.filteredCommHubFormFields.length; i++) {
      this.filteredCommHubFormFields[i].filterValue = null;
    }

    this.applyFilters();
  };

  validateRequirement(commHubEntry: ICommunicationsHubEntry): void {
    ////reset invalid status and fields
    //request.Invalid = false;
    //request.InvalidFields = [];

    ////request type
    //if (!request.RequestCodeId) {
    //  request.Invalid = true;
    //  request.InvalidFields.push('RequestCodeId');
    //}

    ////interviewer
    //if (this.textFieldInvalid(request.InterviewerEmpId)) {
    //  request.Invalid = true;
    //  request.InvalidFields.push('InterviewerEmpId');
    //}

    ////resource team member
    //if (this.textFieldInvalid(request.ResourceTeamMemberId)) {
    //  request.Invalid = true;
    //  request.InvalidFields.push('ResourceTeamMemberId');
    //}

    ////request date
    //if (!request.RequestDate) {
    //  request.Invalid = true;
    //  request.InvalidFields.push('RequestDate');
    //}

    ////request details
    //if (this.textFieldInvalid(request.RequestDetails)) {
    //  request.Invalid = true;
    //  request.InvalidFields.push('RequestDetails');
    //}

    ////decision
    //if (!request.DecisionId) {
    //  request.Invalid = true;
    //  request.InvalidFields.push('DecisionId');
    //}

    //notes
    //if (this.textFieldInvalid(request.Notes)) {
    //  request.Invalid = true;
    //  request.InvalidFields.push('Notes');
    //}

    //set changed - function also includes global checking for invalid state on all requests
    //this.setChanged(commHubEntry, null, []);
  }

  textFieldInvalid(value: string): boolean {
    if (!value) {
      return true;
    }

    if (value.replace(/\s/g, '') == '') {
      return true;
    }

    return false;
  }

  setChanged(commHubEntry: ICommunicationsHubEntry, formField: IFormFieldInstance, formFieldInstances: IFormFieldInstance[]): void {
    commHubEntry.changed = true;
    formField.invalid = false;

    if (!commHubEntry.invalidFields)
      commHubEntry.invalidFields = [];


      //handle required fields
      if (formFieldInstances.length > 0) {
        for (var i = 0; i < formFieldInstances.length; i++) {

          let iFormField: IFormFieldInstance = formFieldInstances[i];
          let fieldHasValue: boolean = false;
          let isValidDate: boolean = false;
          if (iFormField.value) {
            isValidDate = !isNaN(Date.parse(iFormField.value));
            if (iFormField.value.length > 0 || isValidDate) {
              fieldHasValue = true;
            }
          }

          if (iFormField.formFieldVariable.formField.required && !fieldHasValue) {
            if (iFormField.formFieldVariable.formField?.columnName !== formField.formFieldVariable.formField?.columnName) {
              iFormField.missingRequired = true;
              iFormField.invalid = true;
            }

            if (!commHubEntry.invalidFields.includes(iFormField.formFieldVariable.formField?.columnName))
              commHubEntry.invalidFields.push(iFormField.formFieldVariable.formField?.columnName);
          } else {
            iFormField.missingRequired = false;
          }

        }
      }

    //handle invalid fields
    if (formField.validationError || formField.missingRequired) {
      if (!commHubEntry.invalidFields.includes(formField.formFieldVariable.formField?.columnName)) {
        commHubEntry.invalidFields.push(formField.formFieldVariable.formField?.columnName);
      }
      formField.invalid = true;
    } else {
      commHubEntry.invalidFields = commHubEntry.invalidFields.filter(x => (x !== formField.formFieldVariable.formField?.columnName));
      formField.invalid = false;
    }

    if (commHubEntry.invalidFields.length > 0)
      commHubEntry.invalid = true;
    else
      commHubEntry.invalid = false;

    //if (commHubEntry.RequestId > 0)
    //  this.commHubEntriesChanged = true;

    this.globalsService.currentChanges.next(true);
    //this.setRequestValues(commHubEntry);

    //set/unset global validation
    if (this.allCommHubEntries.filter(x => x.invalid).length > 0) {
      this.commHubEntriesInvalid = true;
    } else {
      this.commHubEntriesInvalid = false;
    }

  }

  //save a new comm hub entry
  saveNewCommHubEntry(): void {

    this.newCommHubEntry = this.remapBackCommHubFields(this.newCommHubEntry, this.newCommHubFormFieldInstances);

    //field level handling
    for (var i = 0; i < this.newCommHubEntry.fieldValues.length; i++) {
      //set multi-select value to bar-delimited
      if (Array.isArray(this.newCommHubEntry.fieldValues[i].value)) {
        this.newCommHubEntry.fieldValues[i].value = (this.newCommHubEntry.fieldValues[i].value as Array<string>).join('|');
      }

      //set user/date metadata
      this.newCommHubEntry.fieldValues[i].modBy = this.authenticatedUser.netID;
      this.newCommHubEntry.fieldValues[i].modDt = new Date();
      this.newCommHubEntry.fieldValues[i].entryBy = this.authenticatedUser.netID;
      this.newCommHubEntry.fieldValues[i].entryDt = new Date();

    }

    this.communicationsHubService.saveCommunicationsHubEntries([this.newCommHubEntry]).subscribe(
      response => {
        if (response.Status == 'Success') {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Communications Hub entry saved successfully', ['OK']));
          this.allCommHubEntries.push(this.newCommHubEntry);

          let savedRequests: ICommunicationsHub[] = <ICommunicationsHub[]>response.Subject;

          if (savedRequests) {
            if (savedRequests.length > 0) {
              //this.newCommHubEntry.RequestId = savedRequests[0].RequestId;
            }
          }

          let promotedRequest: IRequest = JSON.parse(JSON.stringify(this.newCommHubEntry));

          this.newCommHubEntry = {
            fieldValues: [],
            invalidFields: []
          };

          if (!this.commHubEntriesChanged) {
            this.globalsService.currentChanges.next(false);
          }

          this.applyFilters();

        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save new Communications Hub entry', ['OK']));
          this.logsService.logError(response.Message);
          this.errorMessage = response.Message;
          this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
        }

        this.showModalPopup = false;

        this.getCommHubEntries();

        this.newCommHubEntry = { fieldValues: [], invalidFields: [] } as ICommunicationsHubEntry;
        this.setNewCommHubFields();

      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save new Communications Hub entry:<br />' + this.errorMessage, ['OK']));
      }
    );

  }

  //save a new comm hub entry
  saveSelectedCommHubEntry(): void {

    this.selectedCommHubEntry = this.remapBackCommHubFields(this.selectedCommHubEntry, this.selectedCommHubFormFieldInstances);

    //field level handling
    for (var i = 0; i < this.selectedCommHubEntry.fieldValues.length; i++) {
      //set multi-select value to bar-delimited
      if (this.selectedCommHubEntry.fieldValues[i].value) {
        if (Array.isArray(this.selectedCommHubEntry.fieldValues[i].value)) {
          this.selectedCommHubEntry.fieldValues[i].value = (this.selectedCommHubEntry.fieldValues[i].value as Array<string>).join('|');
        }
      }

      //set user/date metadata
      this.selectedCommHubEntry.fieldValues[i].modBy = this.authenticatedUser.netID;
      this.selectedCommHubEntry.fieldValues[i].modDt = new Date();
      this.selectedCommHubEntry.fieldValues[i].entryBy = this.authenticatedUser.netID;
      this.selectedCommHubEntry.fieldValues[i].entryDt = new Date();

    }

    this.communicationsHubService.saveCommunicationsHubEntries([this.selectedCommHubEntry]).subscribe(
      response => {
        if (response.Status == 'Success') {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Communications Hub entry saved successfully', ['OK']));
          let savedRequests: ICommunicationsHub[] = <ICommunicationsHub[]>response.Subject;

          if (savedRequests) {
            if (savedRequests.length > 0) {
              //this.newCommHubEntry.RequestId = savedRequests[0].RequestId;
            }
          }

          let promotedRequest: IRequest = JSON.parse(JSON.stringify(this.newCommHubEntry));

          if (!this.commHubEntriesChanged) {
            this.globalsService.currentChanges.next(false);
          }

          this.applyFilters();

        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save Communications Hub entry', ['OK']));
          this.logsService.logError(response.Message);
          this.errorMessage = response.Message;
          this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
        }

        this.showEditPopup = false;

        this.getCommHubEntries();

      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save Communications Hub entry:<br />' + this.errorMessage, ['OK']));
      }
    );

  }

  //handle paging event
  handlePage(e: PageEvent): void {
    this.pageIndex = e.pageIndex;
    this.pageSize = e.pageSize;
    this.setPagedCommHubEntries();
  }

  //set paging variables
  setPagedCommHubEntries(): void {
    this.pagedLength = this.filteredCommHubEntries.length;
    const start = this.pageIndex * this.pageSize;
    const end = (this.pageIndex + 1) * this.pageSize;
    this.pagedcommHubEntries = this.filteredCommHubEntries.slice(start, end);
  }

  generatePdf(): void {
    //scroll to the top or we have weird gaps/cutoffs at top of pdf
    window.scrollTo(0, 0);

    //set filename
    let fileName: string = 'Communications_Hub_' + Utils.formatDateOnlyToString(new Date(), true);// + Utils.formatDateOnlyToString(this.projectTotalsDateRange.value.start, true) + '_' + Utils.formatDateOnlyToString(this.projectTotalsDateRange.value.end, true);

    Utils.hideShowClasses('actions');

    let pageElement: HTMLElement | null = document.getElementById('comm-hub-table');

    if (pageElement) {
      //setup options
      var options = {
        pagebreak: { avoid: ['.mat-row'] },
        margin: 0.25,
        filename: fileName + '.pdf',
        html2canvas: { scale: 2 },
        jsPDF: { unit: 'in', format: 'letter', orientation: 'landscape' }
      };

      //create pdf
      //html2pdf().set(options).from(pageElement).save();
      html2pdf.default(pageElement, options).then(function () {

        Utils.hideShowClasses('actions', false);
        //Utils.hideShowClasses('delete-request-row', false);

      });

    }
  }

  selectedDateChange(): void {
    //this.selectedWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date(this.selectedDate.value));
    this.applyFilters();
  }

  getCommHubConfig(): void {
    this.configurationService.getAllConfiguration().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let allConfiguration: IAllConfiguration = <IAllConfiguration>response.Subject;

          this.commHubFormFields = allConfiguration.formFields.filter(x => x.formField.tableName.toUpperCase() == 'COMMUNICATIONS HUB' && !x.formField.hidden);

          this.commHubFormFields.sort(function (x, y) { return x.formField.formOrder - y.formField.formOrder; });

          //filter the comm hub form fields by selected project
          this.filteredCommHubFormFields = this.commHubFormFields.filter(x => x.formField.projectId == this.selectedCommHubProjectId);

          //map/assign new, empty comm hub form fields
          this.setNewCommHubFields();
        }

      },
      error => {
        this.errorMessage = <any>error;
      }
    );
  }

  getCommHubEntries(): void {
    this.communicationsHubService.setAllCommunicationsHubEntries();
    this.communicationsHubService.communicationsHubEntries.subscribe(
      response => {
        this.allCommHubEntries = response;
        this.applyFilters();
        this.setNoResultsCommHubMessage();
      },
      error => {
        this.errorMessage = <any>error;
        this.setNoResultsCommHubMessage('Error loading communications hub entries');
      }
    );
  }

  commHubProjectSelect(): void {
    if (this.commHubProjects.length < 1)
      return;

    this.selectedCommHubProject = this.commHubProjects.find(x => x.projectID == this.selectedCommHubProjectId) as IProject;

    //set trained on users
    this.selectedCommHubProject.trainedOnUsersMin = this.allUsers.filter(x => x.trainedOnArray.includes((this.selectedCommHubProject.projectID || '').toString()));
    this.trainedOnUsers = Utils.convertObjectArrayToDropDownValues(this.selectedCommHubProject.trainedOnUsersMin, 'Dempoid', 'DisplayName');


    this.commHubProjectTitle = 'Add New Entry for ' + this.selectedCommHubProject.projectName;

    this.commHubProjectTitleEdit = 'Edit Entry for ' + this.selectedCommHubProject.projectName;

    this.commHubProjectTitleView = 'Review Entry for ' + this.selectedCommHubProject.projectName;

    //filter the comm hub form fields by selected project
    this.filteredCommHubFormFields = this.commHubFormFields.filter(x => x.formField.projectId == this.selectedCommHubProjectId);
    this.commHubTableColumns = ['Actions', ...this.filteredCommHubFormFields.filter(x => x.formField.displayInTable).map(y => y.formField?.columnName), 'EntryDate'];

    this.setNewCommHubFields();

    this.applyFilters();

  }

  //map/assign new, empty comm hub form fields - for editing
  setNewCommHubFields(): void {
    this.newCommHubFormFieldInstances = [];

    for (var i = 0; i < this.filteredCommHubFormFields.length; i++) {
      let newFormField: IFormFieldInstance = {
        value: null,
        formFieldVariable: this.filteredCommHubFormFields[i],
        invalid: false,
        validationMessage: ''
      }

      this.newCommHubFormFieldInstances.push(newFormField);
    }

    this.newCommHubFormFieldInstances.sort((x, y) => {
      return x.formFieldVariable.formField.formOrder - y.formFieldVariable.formField.formOrder;
    });

  }

  //map/assign existing comm hub form fields for selected entry - for editing
  setSelectedCommHubFields(commHubEntry: ICommunicationsHubEntry): void {
    this.selectedCommHubFormFieldInstances = [];

    for (var i = 0; i < commHubEntry.fieldValues.length; i++) {

      let currentFormFieldVariable: IFormFieldVariable | undefined = this.commHubFormFields.find(x => x.formField.formFieldId == commHubEntry.fieldValues[i].formFieldId);

      if (!currentFormFieldVariable) {
        continue;
      }

      let newFormField: IFormFieldInstance = {
        value: commHubEntry.fieldValues[i].value,
        formFieldVariable: currentFormFieldVariable,
        invalid: false,
        validationMessage: ''
      }

      this.selectedCommHubFormFieldInstances.push(newFormField);
    }

    this.selectedCommHubFormFieldInstances.sort((x, y) => {
      return x.formFieldVariable.formField.formOrder - y.formFieldVariable.formField.formOrder;
    });

  }

  //map form field instances back to a comm hub entry object - for saving
  remapBackCommHubFields(commHubEntry: ICommunicationsHubEntry, commHubFormFieldInstances: IFormFieldInstance[]): ICommunicationsHubEntry {

    if (!commHubEntry.fieldValues)
      commHubEntry.fieldValues = [];

    for (var i = 0; i < commHubFormFieldInstances.length; i++) {

      let currentFieldValue: ICommunicationsHub | undefined = commHubEntry.fieldValues.find(x => x.formFieldId == commHubFormFieldInstances[i].formFieldVariable.formField.formFieldId);

      if (currentFieldValue) {
        let j: number = commHubEntry.fieldValues.indexOf(currentFieldValue);

        commHubEntry.fieldValues[j].value = commHubFormFieldInstances[i].value;
        commHubEntry.fieldValues[i].modBy = this.authenticatedUser.netID;

      } else {

        let newCommHubField: ICommunicationsHub = {
          communicationsHubId: 0,
          entryId: 0,
          projectId: commHubFormFieldInstances[i].formFieldVariable.formField.projectId as number,
          formFieldId: commHubFormFieldInstances[i].formFieldVariable.formField.formFieldId,
          value: commHubFormFieldInstances[i].value,
          entryBy: this.authenticatedUser.netID,
          modBy: this.authenticatedUser.netID
        }

        commHubEntry.fieldValues.push(newCommHubField);

      }

    }

    return commHubEntry;
  }

  setNoResultsCommHubMessage(customMessage: string | undefined = undefined): void {
    if (customMessage) {
      this.commHubNoResultsMessage = customMessage;
      return;
    }

    if (this.filteredCommHubEntries.length < 1) {
      this.commHubNoResultsMessage = 'No communications hub entries found...';
    } else {
      this.commHubNoResultsMessage = 'Loading communications hub entries...';
    }
  }

  openEditPopup(commHubEntry: ICommunicationsHubEntry): void {
    this.selectedCommHubEntry = commHubEntry;
    this.setSelectedCommHubFields(commHubEntry);

    this.showEditPopup = true;
  }

  closeNewPopup(): void {
    if (this.newCommHubEntry.changed) {
      if (confirm('You have unsaved changes. Are you sure you want to close this window?')) {
        this.showModalPopup = false;
        this.newCommHubEntry = { fieldValues: [], invalidFields: [] } as ICommunicationsHubEntry;
        this.setNewCommHubFields();
      } else {
        return;
      }
    } else {
      this.showModalPopup = false;
      this.newCommHubEntry = { fieldValues: [], invalidFields: [] } as ICommunicationsHubEntry;
      this.setNewCommHubFields();
    }
  }

  closeEditPopup(): void {
    if (this.commHubEntriesChanged) {
      if (confirm('You have unsaved changes. Are you sure you want to close this window?')) {
        this.showEditPopup = false;
      } else {
        return;
      }
    } else {
      this.showEditPopup = false;
    }
  }

  openViewPopup(commHubEntry: ICommunicationsHubEntry): void {
    this.selectedCommHubEntry = commHubEntry;
    this.setSelectedCommHubFields(commHubEntry);

    this.showViewPopup = true;
  }

  openDeletePopup(commHubEntry: ICommunicationsHubEntry): void {
    if (confirm('Are you sure you want to delete this entry? This cannot be undone.')) {

      //field level handling
      for (var i = 0; i < commHubEntry.fieldValues.length; i++) {

        //set user/date metadata
        commHubEntry.fieldValues[i].modBy = this.authenticatedUser.netID;
        commHubEntry.fieldValues[i].modDt = new Date();

      }

      this.communicationsHubService.deleteCommunicationsHub([commHubEntry]).subscribe(
        response => {
          if (response.Status == 'Success') {
            //this.filteredCommHubEntries = this.filteredCommHubEntries.filter(x => x.FieldValues[0].EntryId !== commHubEntry.FieldValues[0].EntryId);
            this.getCommHubEntries();
          } else {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to delete entry', ['OK']));
            this.logsService.logError(response.Message);
            this.errorMessage = response.Message;
            this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
          }
        },
        error => {
          this.errorMessage = <string>(error.message);
          this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to delete entry:<br />' + this.errorMessage, ['OK']));
        }
      );
    }
  }

  getFieldValue(commHubEntry: ICommunicationsHubEntry, formFieldId: number): string | null {

    if (this.filteredCommHubFormFields.length < 1)
      return '';

    let commHubField: ICommunicationsHub | undefined = commHubEntry.fieldValues.find(x => x.formFieldId == formFieldId);
    if (!commHubField)
      return '';

    if (!commHubField.value)
      return '';

    let commHubEntryValue: string = '';
    let formFieldVariable: IFormFieldVariable = this.filteredCommHubFormFields.find(x => x.formField.formFieldId == formFieldId) as IFormFieldVariable;

    let multiTypes: string[] = ['COMBOBOX', 'COMBOFRAME'];

    if (multiTypes.includes(formFieldVariable.formField.fieldType.toUpperCase())) {
      let fieldValues: string[] = commHubField.value.toString().split('|');

      let dropDownItems: string[] = (formFieldVariable.dropDownValues || []).filter(x => fieldValues.includes((x.codeValues || '').toString())).map(y => y.dropDownItem) as string[];
      return dropDownItems.join(', ');
    } else {
      commHubEntryValue = commHubField.value as string;

      if (formFieldVariable.formField.fieldType.toUpperCase() == 'SELECTBOX') {
        let selectedDropdown: IDropDownValue = (formFieldVariable.dropDownValues || []).find(x => (x.codeValues || '').toString() == commHubEntryValue) as IDropDownValue;
        return selectedDropdown.dropDownItem || '';
      } else if (formFieldVariable.formField.fieldType == 'trainedonusers' || formFieldVariable.formField.fieldTypeSpecific == 'trainedonusers') {
        let selectedUser: IUserMin = this.allUsers.find(x => x.dempoid == commHubEntryValue) as IUserMin;
        return (selectedUser.displayName || null);
      } else if (formFieldVariable.formField.fieldType.toUpperCase() == 'DATEONLY') {
        return Utils.formatDateOnlyToString(new Date(commHubEntryValue));
      } else if (formFieldVariable.formField.fieldType.toUpperCase() == 'DATETIME') {
        return Utils.formatDateOnlyToString(new Date(commHubEntryValue)) + ' ' + Utils.formatDateToTimeString(new Date(commHubEntryValue), true);
      } else {
        if (commHubEntryValue == 'dotta001')
          console.log('return no change');
        return commHubEntryValue;
      }
    }

  }

  getFieldInstanceValue(formFieldInstance: IFormFieldInstance, defaultString: string = ''): string | null {

    if (!formFieldInstance)
      return defaultString;

    if (!formFieldInstance.value)
      return defaultString;

    let fieldInstanceValue: string = defaultString;
    let formFieldVariable: IFormFieldVariable = formFieldInstance.formFieldVariable;

    let multiTypes: string[] = ['COMBOBOX', 'COMBOFRAME'];

    if (multiTypes.includes(formFieldVariable.formField.fieldType.toUpperCase())) {
      let fieldValues: string[] = formFieldInstance.value.toString().split('|');

      let dropDownItems: string[] = (formFieldVariable.dropDownValues || []).filter(x => fieldValues.includes((x.codeValues || '').toString())).map(y => y.dropDownItem) as string[];
      if (dropDownItems.length < 1)
        return defaultString;
      else
        return dropDownItems.join(', ');
    } else {
      fieldInstanceValue = formFieldInstance.value as string;

      if (formFieldVariable.formField.fieldType.toUpperCase() == 'SELECTBOX') {
        let selectedDropdown: IDropDownValue = (formFieldVariable.dropDownValues || []).find(x => (x.codeValues || '').toString() == fieldInstanceValue) as IDropDownValue;
        return selectedDropdown.dropDownItem || '';
      } else if (formFieldVariable.formField.fieldType == 'trainedonusers' || formFieldVariable.formField.fieldTypeSpecific == 'trainedonusers') {
        let selectedUser: IUserMin = this.allUsers.find(x => x.dempoid == fieldInstanceValue) as IUserMin;
        return (selectedUser.displayName || null);
      } else if (formFieldVariable.formField.fieldType.toUpperCase() == 'DATEONLY') {
        return Utils.formatDateOnlyToString(new Date(fieldInstanceValue));
      } else if (formFieldVariable.formField.fieldType.toUpperCase() == 'DATETIME') {
        return Utils.formatDateOnlyToString(new Date(fieldInstanceValue)) + ' ' + Utils.formatDateToTimeString(new Date(fieldInstanceValue), true);
      } else {
        return fieldInstanceValue;
      }
    }

  }

  getEntryDate(commHubEntry: ICommunicationsHubEntry): string {
    return Utils.formatDateOnlyToString(commHubEntry.entryDt) + ' ' + Utils.formatDateToTimeString(commHubEntry.entryDt, true);
  }

  setAuthenticatedUserProfile(): void {
    if (!this.authenticatedUser || this.allUsers.length < 1 || this.allProjects.length < 1)
      return;

    this.authenticatedUserProfile = this.allUsers.find(x => x.dempoid == this.authenticatedUser.netID) as IUserMin;
    let commHubProjects: IProjectMin[] = this.allProjects.filter(
      function (project) {
        if (project.projectDisplayId) {
          let ProjectDisplayId: string[] = project.projectDisplayId.split('|');
          if (ProjectDisplayId.includes('4'))
            return true;
          else
            return false;
        } else
          return false;
      });

    if (commHubProjects.length < 1)
      return;

    let commHubProjectIds: string[] = commHubProjects.map(x => x.projectID.toString());

    if (Utils.arrayIncludesAny(this.authenticatedUserProfile.trainedOnArray, commHubProjectIds))
      this.trainedOnCommHubProject = true;

    //DEBUG
    //this.authenticatedUser.Admin = false;
    //this.authenticatedUser.ResourceGroup = false;
    //this.authenticatedUser.Interviewer = true;
    //this.trainedOnCommHubProject = true;
  }

  exportCsv(): void {
    if (this.allCommHubEntries.length < 1)
      return;

    let rows: object[] = [];
    let currentDate: Date = new Date();
    let fileName: string = `Communications_Hub_${currentDate.getFullYear()}${Utils.zeroPad(currentDate.getMonth() + 1)}${Utils.zeroPad(currentDate.getDate())}_${Utils.zeroPad(currentDate.getHours())}${Utils.zeroPad(currentDate.getMinutes())}${Utils.zeroPad(currentDate.getSeconds())}`;

    //CSV (all fields)
    if ((this.exportSelection as number) == 1) {
      fileName = `Communications_Hub_All_Fields_${currentDate.getFullYear()}${Utils.zeroPad(currentDate.getMonth() + 1)}${Utils.zeroPad(currentDate.getDate())}_${Utils.zeroPad(currentDate.getHours())}${Utils.zeroPad(currentDate.getMinutes())}${Utils.zeroPad(currentDate.getSeconds())}`;
      let allProjectCommHubEntries: ICommunicationsHubEntry[] = this.allCommHubEntries.filter(x => x.fieldValues[0].projectId == this.selectedCommHubProjectId);

      rows = [];
      for (var i = 0; i < allProjectCommHubEntries.length; i++) {
        let row: Record<string, any> = {};
        let commHubValues: ICommunicationsHub[] = allProjectCommHubEntries[i].fieldValues;

        for (var j = 0; j < this.filteredCommHubFormFields.length; j++) {
          let commHubValue: ICommunicationsHub | undefined = commHubValues.find(x => x.formFieldId == this.filteredCommHubFormFields[j].formField.formFieldId);
          if (commHubValue) {
            let formField: IFormFieldVariable = this.filteredCommHubFormFields[j];
            let displayValue: any = commHubValue.value;

            //remove string value with the word "null"
            if (displayValue !== null) {
              if (displayValue.toString().toUpperCase() == 'NULL')
                displayValue = '';
            } else
              displayValue = '';

            //single select support
            if (formField.formField.fieldType == 'selectbox' && displayValue) {
              let dropdownValue: IDropDownValue | undefined = (formField.dropDownValues || []).find(
                function (dropdownValue) {
                  if (dropdownValue.codeValues && displayValue)
                    return dropdownValue.codeValues.toString() == displayValue.toString();
                  else
                    return false;
                });
              if (dropdownValue)
                displayValue = dropdownValue.dropDownItem;
            }
            //multi select support
            if (formField.formField.fieldType == 'combobox' && displayValue) {
              let displayValuesArray: string[] = displayValue.toString().split('|');
              let dropdownValues: IDropDownValue[] = (formField.dropDownValues || []).filter(x => displayValuesArray.includes((x.codeValues ? x.codeValues.toString() : '')));
              if (dropdownValues.length > 0)
                displayValue = dropdownValues.map(x => x.dropDownItem).join(', ');
            }
            //date support
            if (['dateonly', 'datetime'].includes(formField.formField.fieldType) && displayValue) {
              displayValue = Utils.formatDateOnlyToString(new Date(displayValue)) + ' ' + Utils.formatDateToTimeString(new Date(displayValue), true)
            }

            row[`"${this.filteredCommHubFormFields[j].formField.fieldLabel}"`] = `"${displayValue}"`;
          }
        }

        let entryDt: Date = new Date(allProjectCommHubEntries[i].entryDt || '');
        row['Entry Date'] = Utils.formatDateOnlyToString(entryDt) + ' ' + Utils.formatDateToTimeString(entryDt, true);

        rows.push(row);

      }
    }

    //CSV (displayed fields)
    if ((this.exportSelection as number) == 2) {
      fileName = `Communications_Hub_Displayed_Fields_${currentDate.getFullYear()}${Utils.zeroPad(currentDate.getMonth() + 1)}${Utils.zeroPad(currentDate.getDate())}_${Utils.zeroPad(currentDate.getHours())}${Utils.zeroPad(currentDate.getMinutes())}${Utils.zeroPad(currentDate.getSeconds())}`;
      let displayedFormFields: IFormFieldVariable[] = this.filteredCommHubFormFields.filter(x => x.formField.displayInTable);

      rows = [];
      for (var i = 0; i < this.filteredCommHubEntries.length; i++) {
        let row: Record<string, any> = {};
        let commHubValues: ICommunicationsHub[] = this.filteredCommHubEntries[i].fieldValues;

        for (var j = 0; j < displayedFormFields.length; j++) {
          let commHubValue: ICommunicationsHub | undefined = commHubValues.find(x => x.formFieldId == displayedFormFields[j].formField.formFieldId);
          if (commHubValue) {
            let formField: IFormFieldVariable = displayedFormFields[j];
            let displayValue: any = commHubValue.value;

            //remove string value with the word "null"
            if (displayValue !== null) {
              if (displayValue.toString().toUpperCase() == 'NULL')
                displayValue = '';
            } else
              displayValue = '';

            //single select support
            if (formField.formField.fieldType == 'selectbox' && displayValue) {
              let dropdownValue: IDropDownValue | undefined = (formField.dropDownValues || []).find(
                function (dropdownValue) {
                  if (dropdownValue.codeValues && displayValue)
                    return dropdownValue.codeValues.toString() == displayValue.toString();
                  else
                    return false;
                });

              if (dropdownValue)
                displayValue = dropdownValue.dropDownItem;
            }
            //multi select support
            if (formField.formField.fieldType == 'combobox' && displayValue) {
              let displayValuesArray: string[] = displayValue.toString().split('|');
              let dropdownValues: IDropDownValue[] = (formField.dropDownValues || []).filter(x => displayValuesArray.includes((x.codeValues ? x.codeValues.toString() : '')));
              if (dropdownValues.length > 0)
                displayValue = dropdownValues.map(x => x.dropDownItem).join(', ');
            }
            //date support
            if (['dateonly', 'datetime'].includes(formField.formField.fieldType) && displayValue) {
              displayValue = Utils.formatDateOnlyToString(new Date(displayValue)) + ' ' + Utils.formatDateToTimeString(new Date(displayValue), true);
            }

            //add value to object
            row[`"${displayedFormFields[j].formField.fieldLabel}"`] = `"${displayValue}"`;
          }
        }
        let entryDt: Date = new Date(this.filteredCommHubEntries[i].entryDt || '');
        row['Entry Date'] = Utils.formatDateOnlyToString(entryDt) + ' ' + Utils.formatDateToTimeString(entryDt, true);

        rows.push(row);

      }
    }

    var csvData = this.convertToCSV(rows);
    var blob = new Blob([csvData], { type: 'text/csv' });
    var url = window.URL.createObjectURL(blob);

    if ((navigator as any).msSaveOrOpenBlob) {
      (navigator as any).msSaveBlob(blob, fileName);
    } else {
      var a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    }
    window.URL.revokeObjectURL(url);

    //reset dropdown so no selected value shows and we just see the word "Export"
    this.exportSelection = new FormControl();
  }

  convertToCSV(objArray: any): string {
    var array = typeof objArray != 'object' ? JSON.parse(objArray) : objArray;
    var str = '';
    var row = "";

    for (var index in objArray[0]) {
      //Now convert each value to string and comma-separated
      row += index + ',';
    }
    row = row.slice(0, -1);
    //append Label row with line break
    str += row + '\r\n';

    for (var i = 0; i < array.length; i++) {
      var line = '';
      for (var index in array[i]) {
        if (line != '') line += ','

        line += array[i][index];
      }
      str += line + '\r\n';
    }
    return str;
  }

  restrictProjectsToTrainedOn(): void {
    if (this.allUsers.length < 1 || this.commHubProjects.length < 1 || !this.authenticatedUser.netID)
      return;

    //restrict project list to projects the current user is trained on - if they are an interviewer role
    if (this.authenticatedUser.interviewer && !this.authenticatedUser.resourceGroup && !this.authenticatedUser.admin) {
      let currentUser: IUserMin = this.allUsers.find(x => x.dempoid == this.authenticatedUser.netID) as IUserMin;

      this.commHubProjects = this.commHubProjects.filter(x => currentUser.trainedOnArray.includes(x.projectID.toString()));
    }
  }

}
