import {Component, HostListener, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { PageEvent } from '@angular/material/paginator';
import { Utils } from '../../classes/utils';
import {
  IAuthenticatedUser,
  IBlockOutDate,
  IDropDownValue,
  IFormFieldVariable,
  IRequest,
  IUserMin,
  IWeekStartAndEnd
} from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { GlobalsService } from '../../services/globals/globals.service';
import { RequestsService } from '../../services/requests/requests.service';
import { UsersService } from '../../services/users/users.service';
import * as html2pdf from 'html2pdf.js';
import { LogsService } from '../../services/logs/logs.service';
import {User} from "../../models/data/user";
import {CanComponentDeactivate} from "../../guards/unsaved-changes.guard";
import {UnsavedChangesDialogComponent} from "../../components/unsaved-changes-dialog/unsaved-changes-dialog.component";
import {MatDialog} from "@angular/material/dialog";
import {Router} from "@angular/router";

@Component({
  selector: 'app-requests',
  templateUrl: './requests.component.html',
  styleUrls: ['./requests.component.css']
})
export class RequestsComponent implements OnInit,CanComponentDeactivate {
  @HostListener('window:beforeunload') onBeforeUnload(e: any) {
    if (this.newRequest.changed || this.requestsChanged) {
      e.preventDefault();
      e.returnValue = '';
    }
  }

  errorMessage: string = '';
  noResultsMessage: string = 'Loading requests...';
  authenticatedUser!: IAuthenticatedUser;
  showPopupMessage: boolean = false;
  requestCodeDropDown!: IDropDownValue[];
  decisionIdDropDown!: IDropDownValue[];
  requestFormFields: IFormFieldVariable[] = [];
  allUsers: User[] = [];
  activeUsers: User[] = [];
  filteredUsersDv: IDropDownValue[] = [];
  interViewerRequestTableColumns: string[] = ['RequestType', 'InterviewerEmpName', 'ResourceTeamMemberName', 'RequestDate', 'RequestDetails', 'Decision', 'Notes'];
  requestTableColumns: string[] = ['RequestType', 'InterviewerEmpName', 'ResourceTeamMemberName', 'RequestDate', 'RequestDetails', 'Decision', 'Notes', 'Actions'];
  allRequests: IRequest[] = [];
  filteredRequests: IRequest[] = [];
  newRequest: IRequest = {
    invalidFields: [],
    decisionId: null,
    requestCodeId: null,
    interviewerEmpId: null,
    resourceTeamMemberId: null,
    requestId: 0,
    requestDate: new Date(),
    requestDetails: '',
    notes: '',
    modBy: '',
    entryBy: '',
  };
  requestsChanged: boolean = false;
  requestsInvalid: boolean = false;

  //filters
  requestTypeFilter: FormControl = new FormControl(['0']);
  interviewerFilter: FormControl = new FormControl(['0']);
  decisionFilter: FormControl = new FormControl(['0']);

  //date filter
  selectedWeekStartAndEnd: IWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date());
  selectedDate: FormControl = new FormControl(new Date());
  selectedDateRange: FormGroup;

  //request pagination
  pageSizeOptions = [5, 10, 20, 50, 100];
  //added
  pageIndex = 0;
  pageSize = 10;
  pagedLength = 0;
  pagedRequests: IRequest[] = [];
  public nextUrl: string | null = null;
  constructor(private globalsService: GlobalsService,
              private requestsService: RequestsService,
              private configurationService: ConfigurationService,
              private usersService: UsersService,
              private authenticationService: AuthenticationService,
              private logsService: LogsService,private dialog: MatDialog,
              private router: Router) {

    this.selectedDateRange = new FormGroup({
      start: new FormControl(new Date(this.selectedWeekStartAndEnd.weekStart)),
      end: new FormControl(new Date(this.selectedWeekStartAndEnd.weekEnd)),
    });

    this.applyFilters();

    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;
        this.newRequest.resourceTeamMemberId = this.authenticatedUser.netID;
        this.newRequest.resourceTeamMemberName = this.authenticatedUser.displayName;
      }
    );

    //get active users
    this.usersService.allUsersMin.subscribe(
      allUsers => {
        this.allUsers = allUsers;
        this.activeUsers = allUsers;
        this.trySetRequestValues();
        this.filteredUsersDv = Utils.convertObjectArrayToDropDownValues(this.allUsers, 'dempoid', 'displayName');
      }
    );

    this.globalsService.showPopupMessage.subscribe(
      showPopupMessage => {
        this.showPopupMessage = showPopupMessage;
      }
    );

    //get requests dropdown configurations
    this.configurationService.getFormFieldsByTable('Requests').subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let requestFormFields: IFormFieldVariable[] = <IFormFieldVariable[]>response.Subject;

          this.requestFormFields = requestFormFields;
          let requestCodeFormField: IFormFieldVariable | undefined = requestFormFields.find(x => x.formField?.columnName == 'requestCodeID');
          if (requestCodeFormField) {
            this.requestCodeDropDown = requestCodeFormField.dropDownValues || [];
          }
          let decisionFormField: IFormFieldVariable | undefined = requestFormFields.find(x => x.formField?.columnName == 'decisionID');
          if (decisionFormField) {
            this.decisionIdDropDown = decisionFormField.dropDownValues || [];
          }
          this.trySetRequestValues();
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage);
        console.log(this.errorMessage);
      }
    );

    this.setAllRequests();

    //subscribe to all requests
    this.requestsService.requests.subscribe(
      requests => {
        this.allRequests = requests;
        if (this.allRequests.length > 0) {
          this.trySetRequestValues();
          this.filteredRequests = this.allRequests;
          this.setNoResultsMessage();
          this.applyFilters();
        } else {
          this.setNoResultsMessage('Loading requests...');
        }
      },
      error => {
        this.setNoResultsMessage('Error loading requests...');
      }
    );

  }

  ngOnInit(): void {
    //set current page for navigation menu to track
    this.globalsService.selectedPage.next('requests');

  }

  setAllRequests(): void {
    //set all requests
    this.requestsService.setAllRequests();
  }

  //populate string values for coded fields in requests
  trySetRequestValues(): void {
    if (!(this.allRequests.length > 0 && this.requestFormFields.length > 0 && this.allUsers.length > 0)) {
      return;
    }

    for (var i = 0; i < this.allRequests.length; i++) {
      this.setRequestValues(this.allRequests[i]);
    }
  }

  //populate string values for coded fields in a single request
  setRequestValues(request: IRequest): void {
    let requestTypeCode = this.requestCodeDropDown.find(x => x.codeValues == request.requestCodeId);
    let decisionCode = this.decisionIdDropDown.find(x => x.codeValues == request.decisionId);
    let interviewerUser = this.allUsers.find(x => x.dempoid == request.interviewerEmpId);
    let resourceUser = this.allUsers.find(x => x.dempoid == request.resourceTeamMemberId);

    request.requestType = (requestTypeCode ? requestTypeCode.dropDownItem : '');
    request.decision = (decisionCode ? decisionCode.dropDownItem : '');
    request.interviewerEmpName = (interviewerUser ? (interviewerUser.displayName || '') : '');
    request.resourceTeamMemberName = (resourceUser ? (resourceUser.displayName || '') : '');
    request.requestDate = new Date(request.requestDate);
  }

  formatDateOnlyToString(dateToFormat: Date): string {
    if (!dateToFormat) {
      return '';
    }

    return Utils.formatDateOnlyToString(dateToFormat) || '';
  }

  setNoResultsMessage(customMessage: string | null = null): void {
    if (customMessage) {
      this.noResultsMessage = customMessage;
      return;
    }

    if (this.filteredRequests.length < 1) {
      this.noResultsMessage = 'No requests found...';
    } else {
      this.noResultsMessage = 'Loading requests...';
    }
  }
  closeModalPopup(): void {
    this.globalsService.hidePopupMessage();
  }
  compareSelectValues(v1: any, v2: any): boolean {
    return (v1 || '').toString() === (v2 || '').toString();
  }

  applyFilters(): void {
    //make sure requests are sorted by date
    this.filteredRequests = this.allRequests.sort(function (x, y) {
      if ((Utils.formatDateOnly(new Date(x.requestDate)) || new Date()) < (Utils.formatDateOnly(new Date(y.requestDate)) || new Date())) {
        return 1;
      } else if ((Utils.formatDateOnly(new Date(x.requestDate)) || new Date()) > (Utils.formatDateOnly(new Date(y.requestDate)) || new Date())) {
        return -1;
      } else {
        if (x.requestId < y.requestId) return 1;
        else if (x.requestId > y.requestId) return -1;
        else return 0;
      }
    });

    //reset filters to all requests
    this.filteredRequests = [...this.allRequests];

    //request type
    if (!this.requestTypeFilter.value.includes('0')) {
      this.filteredRequests = this.filteredRequests.filter(x => this.requestTypeFilter.value.includes((x.requestCodeId ? x.requestCodeId.toString() : '')));
    }

    //interviewer
    if (!this.interviewerFilter.value.includes('0')) {
      this.filteredRequests = this.filteredRequests.filter(x => this.interviewerFilter.value.includes(x.interviewerEmpId));
    }

    //decision
    if (!this.decisionFilter.value.includes('0')) {
      this.filteredRequests = this.filteredRequests.filter(x => this.decisionFilter.value.includes((x.decisionId ? x.decisionId.toString() : '')));
    }

    //date range filtering
    if (this.selectedDateRange.value) {
      if (this.selectedDateRange.value.start && this.selectedDateRange.value.end) {
        this.filteredRequests = this.filteredRequests.filter(x => ((Utils.formatDateOnly(x.requestDate) || new Date()) >= (Utils.formatDateOnly(this.selectedDateRange.value.start) || new Date())
          && (Utils.formatDateOnly(x.requestDate) || new Date()) <= (Utils.formatDateOnly(this.selectedDateRange.value.end) || new Date())));
      }
    }

    //set paging
    this.setPagedRequests();
  }

  requestsFilterChange(selectedValues: FormControl): void {
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
    this.requestTypeFilter = new FormControl(['0']);
    this.interviewerFilter = new FormControl(['0']);
    this.decisionFilter = new FormControl(['0']);

    this.selectedWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date());
    this.selectedDate = new FormControl(new Date());
    this.selectedDateRange = new FormGroup({
      start: new FormControl(new Date(this.selectedWeekStartAndEnd.weekStart)),
      end: new FormControl(new Date(this.selectedWeekStartAndEnd.weekEnd)),
    });

    this.applyFilters();
  };

  validateRequirement(request: IRequest): void {
    //reset invalid status and fields
    request.invalid = false;
    request.invalidFields = [];

    //request type
    if (!request.requestCodeId) {
      request.invalid = true;
      request.invalidFields.push('RequestCodeId');
    }

    //interviewer
    if (this.textFieldInvalid(request.interviewerEmpId || '')) {
      request.invalid = true;
      request.invalidFields.push('InterviewerEmpId');
    }

    //resource team member
    if (this.textFieldInvalid(request.resourceTeamMemberId || '')) {
      request.invalid = true;
      request.invalidFields.push('ResourceTeamMemberId');
    }

    //request date
    if (!request.requestDate) {
      request.invalid = true;
      request.invalidFields.push('RequestDate');
    }

    //request details
    if (this.textFieldInvalid(request.requestDetails)) {
      request.invalid = true;
      request.invalidFields.push('RequestDetails');
    }

    //decision
    if (!request.decisionId) {
      request.invalid = true;
      request.invalidFields.push('DecisionId');
    }

    //notes
    //if (this.textFieldInvalid(request.Notes)) {
    //  request.Invalid = true;
    //  request.InvalidFields.push('Notes');
    //}

    //set changed - function also includes global checking for invalid state on all requests
    this.setChanged(request);
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

  setChanged(request: IRequest): void {
    request.changed = true;

    if (request.requestId > -1)
      this.requestsChanged = true;

    this.globalsService.currentChanges.next(true);
    this.setRequestValues(request);

    //set/unset global validation
    if (this.allRequests.filter(x => x.invalid).length > 0) {
      this.requestsInvalid = true;
    } else {
      this.requestsInvalid = false;
    }

  }

  //save a new request
  saveNewRequest(): void {

    //set user/date metadata
    this.newRequest.modBy = this.authenticatedUser.netID;
    this.newRequest.modDt = new Date();
    this.newRequest.entryBy = this.authenticatedUser.netID;
    this.newRequest.entryDt = new Date();

    this.requestsService.saveRequests([this.newRequest]).subscribe(
      response => {
        if (response.Status == 'Success') {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Request saved successfully', ['OK']));
          let savedRequests: IRequest[] = <IRequest[]>response.Subject;

          if (savedRequests) {
            if (savedRequests.length > 0) {
              this.newRequest.requestId = savedRequests[0].requestId;
            }
          }

          let promotedRequest: IRequest = JSON.parse(JSON.stringify(this.newRequest));
          this.setRequestValues(promotedRequest);
          this.allRequests.push(promotedRequest);
          this.newRequest = {
            invalidFields: [],
            decisionId: null,
            requestCodeId: null,
            interviewerEmpId: null,
            resourceTeamMemberId: this.authenticatedUser.netID,
            resourceTeamMemberName: this.authenticatedUser.displayName,
            requestId: 0,
            requestDate: new Date(),
            requestDetails: '',
            notes: '',
            modBy: '',
            entryBy: '',
            changed: false
          };

          if (!this.requestsChanged) {
            this.globalsService.currentChanges.next(false);
          }

          this.applyFilters();

        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save new request', ['OK']));
          this.logsService.logError(response.Message);
          this.errorMessage = response.Message;
          this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save new request:<br />' + this.errorMessage, ['OK']));
      }
    );

  }

  saveRequestChanges(): void {
    let changedRequests: IRequest[] = this.allRequests.filter(x => (x.changed && !x.markedForDeletion));
    let deleteRequests: IRequest[] = this.allRequests.filter(x => x.markedForDeletion);

    //set mod by/date metadata
    for (var i = 0; i < changedRequests.length; i++) {
      changedRequests[i].modBy = this.authenticatedUser.netID;
      changedRequests[i].modDt = new Date();
    }

    if (changedRequests.length > 0) {
      this.requestsService.saveRequests(changedRequests).subscribe(
        response => {

          if (response.Status == 'Success') {
            this.requestsChanged = false;
            if (this.newRequest.changed !== true) {
              this.globalsService.currentChanges.next(false);
            }

            if (deleteRequests.length > 0) {
              this.commitDeletions(true);
              this.setAllRequests();
            } else {
              this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Request(s) saved successfully', ['OK']));
              this.setAllRequests();
            }

          } else {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save request(s)', ['OK']));
            this.logsService.logError(response.Message);
            this.errorMessage = response.Message;
            this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
          }
        },
        error => {
          this.errorMessage = <string>(error.message);
          this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to save request(s):<br />' + this.errorMessage, ['OK']));
        }
      );
    } else if (deleteRequests.length > 0) {
      this.commitDeletions();
    }

  }

  commitDeletions(afterSave: boolean = false): void {
    this.requestsService.deleteRequests(this.allRequests.filter(x => x.markedForDeletion)).subscribe(
      response => {
        if (response.Status == 'Success') {
          if (afterSave) {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Request(s) saved/deleted successfully', ['OK']));
          } else {
            this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Success', 'Request(s) deleted successfully', ['OK']));
            this.requestsChanged = false;
            if (this.newRequest.changed !== true) {
              this.globalsService.currentChanges.next(false);
            }
          }

          this.setAllRequests();

        } else {
          this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to delete request(s)', ['OK']));
          this.logsService.logError(response.Message);
          this.errorMessage = response.Message;
          this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
        this.globalsService.displayPopupMessage(Utils.generatePopupMessage('Error', 'Encountered an error while trying to delete request(s):<br />' + this.errorMessage, ['OK']));
      }
    );

  }

  //handle paging event
  handlePage(e: PageEvent): void {
    this.pageIndex = e.pageIndex;
    this.pageSize = e.pageSize;
    this.setPagedRequests();
  }

  //set paging variables
  setPagedRequests(): void {
    this.pagedLength = this.filteredRequests.length;
    const start = this.pageIndex * this.pageSize;
    const end = (this.pageIndex + 1) * this.pageSize;
    this.pagedRequests = this.filteredRequests.slice(start, end);
  }

  generatePdf(): void {
    //scroll to the top or we have weird gaps/cutoffs at top of pdf
    window.scrollTo(0, 0);

    //set filename
    let fileName: string = 'Requests_' + Utils.formatDateOnlyToString(new Date(), true);// + Utils.formatDateOnlyToString(this.projectTotalsDateRange.value.start, true) + '_' + Utils.formatDateOnlyToString(this.projectTotalsDateRange.value.end, true);

    Utils.hideShowClasses('actions');
    //Utils.hideShowClasses('delete-request-row');

    let pageElement: HTMLElement = <HTMLElement>document.getElementById('requests-table');

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
    if (this.selectedDateRange.value.start && this.selectedDateRange.value.end) {
      this.selectedWeekStartAndEnd = Utils.setSelectedWeekStartAndEnd(new Date(this.selectedDateRange.value));
      this.applyFilters();
    }
  }

  canDeactivate(nextUrl: string | null): boolean {

    if(this.requestsChanged){
      this.nextUrl = nextUrl;
      this.openDialog({
        dialogType: 'error',
        isUserProfile: true
      })
      return false;
    }


    return true;

  }

  openDialog(data: any,): void {
    const dialogRef = this.dialog.open(UnsavedChangesDialogComponent, {
      width: '300px',
      data: {
        ...data
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == 'discardChanges') {
        this.requestsChanged=false;
        if (this.nextUrl) {
          this.router.navigateByUrl(this.nextUrl);
        }
      }
    });
  }
}
