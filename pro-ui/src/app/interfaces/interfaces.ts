import { FormControl } from "@angular/forms";
import { User } from "../models/data/user";

export interface ITimeCard {
  timeCardId: number;
  dempoid: string;
  //workDate?: Date;
  timeIn?: Date | null;
  timeOut?: Date | null;
  entryBy: string;
  entryDt?: Date;
  modBy: string;
  modDt?: Date;
  datetimein?: Date | null;
  datetimeout?: Date | null;
  externalIp: string | null;
  //MachineName: string;
}

export interface IDateTimeCards {
  timeCards: ITimeCard[];
  date: Date;
}

export interface ICurrentUser {
  user: User;
  timecards: ITimeCard[];
}

export interface IProject {
  projectID: number | null;
  projectName: string | null;
  projectLeadId?: number | null;
  projectLead: string | null;
  projectCoordinatorId?: number | null;
  projectCoordinator: string | null;
  active?: number | null;
  projectStatus: string | null;
  projectDisplayId: string | null;
  interview?: number | null;
  projectColor: string | null;
  projectColorIview: string | null;
  projectedStartDate?: Date | null;
  projectedEndDate?: Date | null;
  projectEpmcode: string | null;
  projectType?: number | null;
  sponsor: string | null;
  principalInvestigator: string | null;
  totalBudget?: number | null;
  comments: string | null;
  scheduleDisplay?: number | null;
  ssdatabaseName: string | null;
  forecast?: number | null;
  tollFreeNumber: string | null;
  studyEmailAddress: string | null;
  pdriveLocation: string | null;
  tdriveLocation: string | null;
  irbnumber: string | null;
  fundCode: string | null;
  sisterCode: string | null;
  passThruCode: string | null;
  rescue?: number | null;
  orphan?: number | null;
  edcsystem: string | null;
  therapeuticArea: string | null;
  projectAbbr: string | null;
  projectManager: string | null;
  faxNumber: string | null;
  faxLocation: string | null;
  maxEnrollment?: number | null;
  firstEnrollDate?: Date | null;
  visitDate: string | null;
  totalProjected?: number | null;
  actualFollowUp?: number | null;
  numofIntervals?: number;
  entryFormName: string | null;
  entryDt: Date | null;
  entryBy: string | null;
  modDt: Date | null;
  modBy: string | null;
  trainedOnUsersMin?: User[];
  trainedOnUsers?: string[];
  notTrainedOnUsers?: string[];
}

export interface IProjectMin  {
  projectID: number;
  projectName: string;
  projectAbbr: string;
  projectDisplayId: string;
  displayedIn?: string;
  active?: boolean | null;
  projectStatus?: boolean | null;
  projectColor: string;
  projectType?: string | null;
  selected?: boolean;
  trainedOnUsersMin?: User[];
  trainedOnUsers?: string[];
  notTrainedOnUsers?: string[];
  clicked?:boolean;
  defaultproject:number;
}

export interface DefPro  {
  defaultproject:number;
}

export interface IDropDownValue {
  dropDownValueId?: number;
  sortOrder?: number;
  codeValues?: number;
  formFieldId?: number;
  dropDownItem?: string;
  colorCode?: string | null;
  abbr?: string | null;
  hidden?: boolean;
  confirmDelete?: boolean;
  tempKey?: number;
  changed?: boolean;
  invalid?: boolean;
  entryDt?: Date | null;
  entryBy?: string | null;
  modDt?: Date | null;
  modBy?: string | null;
}

export interface IFormField {
  formFieldId: number;
  fieldLabel: string;
  defaultLabel: string;
  columnName: string;
  tableName: string;
  formOrder: number;
  tab: string | null;
  fieldType: string;
  fieldTypeSpecific?: string;
  configurable: string;
  wasRequired?: boolean;
  required?: boolean;
  displayInTable?: boolean;
  hidden?: boolean;
  projectId?: number;
  requireConfirmation?: boolean;
  entryDt?: Date;
  entryBy: string;
  modDt?: Date;
  modBy: string;
  formSection?: number
}

export interface IAdminOptionsVariable {
  adminOptionsId: number;
  optionValue: string;
  optionValueMulti: Array<string>;
  fieldType: string;
  sortOrder: number;
  fieldLabel: string;
  dropDownValues?: Array<IDropDownValue>;
  formFieldId: number;
}

export interface IFormFieldVariable {
  formField: IFormField;
  dropDownValues?: Array<IDropDownValue>;
  expanded?: boolean;
  changed?: boolean;
  invalid?: boolean;
  filterValue?: any;
  sortActive?: boolean;
}

export interface IFormFieldInstance {
  value: any;
  formFieldVariable: IFormFieldVariable;
  invalid: boolean;
  missingRequired?: boolean;
  validationError?: boolean;
  validationMessage: string;
}

export interface IAllConfiguration {
  adminOptionsVariables: Array<IAdminOptionsVariable>;
  formFields: Array<IFormFieldVariable>;
}

export interface IFormFieldsByTable {
  tableName: string;
  formFields: Array<IFormFieldVariable>;
}

export interface ICommunicationsHub {
  communicationsHubId: number;
  projectId: number;
  entryId: number;
  formFieldId: number;
  value?: any;
  entryBy: string;
  entryDt?: Date;
  modBy: string;
  modDt?: Date;
}

export interface ICommunicationsHubEntry {
  fieldValues: ICommunicationsHub[];
  changed?: boolean;
  invalid?: boolean;
  invalidFields: string[];
  entryBy?: string;
  entryDt?: Date;
}

export interface ILegend {
  projectName: string | null;
  projectColor: string;
  projectid: number | null | undefined;
}

export interface ISchedule {
  preschedulekey: number;
  dempoid: string | null;
  fname: string | null;
  lname: string | null;
  preferredfname: string | null;
  preferredlname: string | null;
  displayName: string | null;
  userName: string | null;
  projectid?: number | null;
  projectName: string | null;
  projectColor: string;
  scheduledate: Date;
  comments: string | null;
  startdatetime?: Date | null;
  dayWiseDate?: Date | null;
  enddatetime?: Date | null;
  startTime?: string;
  endTime?: string;
  dayNumber?: number | null;
  weekNumber?: number | null;
  weekStart: Date | null;
  weekEnd: Date | null;
  dayOfWeek: number;
  dayOfWeekText?: string;
  monthNumber?: number | null;
  month: string | null;
  scheduleyear?: number | null;
  scheduledHours?: number | null;
  expr1: string | null;
  requestId?: number | null;
  requestDetails: string | null;
  requestCodeId?: number | null;
  requestCode: string | null;
  userid: number | null;
  trainedon: string | null;
  language: string | null;
  changed?: boolean;
  saved?: boolean | null;
  markedForDeletion?: boolean | null;
  addTab?: boolean;
  addInitial?: boolean;
  invalid?: boolean;
  invalidFields?: string[];
  entryBy: string | null;
  entryDt?: Date;
  validationMessages?: string[];
  hoverMessage?: string;
  scheduleConflict?: boolean;
  justAdded?: boolean;
  initialProjectid?: number | null;
  duration?: number | null;
}

export interface IScheduleMin {
  preschedulekey: number;
  dempoid: string | null;
  scheduledate?: Date;
  startdatetime?: Date | null;
  enddatetime?: Date | null;
  status?: number;
  projectid?: number | null;
  comments: string | null;
  entryBy: string;
  entryDt?: Date;
  modBy: string;
  modDt?: Date;
  machineName: string;
}

export interface IUserSchedule {
  user: User;
  schedules: Array<ISchedule>;
}

export interface IWeekStartAndEnd {
  weekStart: Date;
  weekEnd: Date;
}

export interface IWeekStartAndEndStrings {
  weekStart: string;
  weekEnd: string;
}

export interface IWeekSchedules {
  weekStart: Date;
  day1Schedules: ISchedule[];
  day2Schedules: ISchedule[];
  day3Schedules: ISchedule[];
  day4Schedules: ISchedule[];
  day5Schedules: ISchedule[];
  day6Schedules: ISchedule[];
  day7Schedules: ISchedule[];
}

export interface IMonthSchedules {
  weekSchedules: IWeekSchedules[];
}

export interface ITimeCode {
  timePeriod: string;
  timeCodeValue: number;
  timePeriodampm: string;
}

export interface IAnyFilter {
  filterControl: FormControl;
  anyToggle: boolean;
}

export interface ISaveResponse {
  status: string;
  successCount: number;
  errors: Array<string>;
}

export interface IGeneralResponse {
  Status: string;
  Message: string;
  Subject: any;
}

export interface IAuthenticatedUser {
  displayName: string;
  duDukeID: string;
  eppn: string;
  netID: string;
  isMemberOf: string;
  interviewer: boolean;
  resourceGroup: boolean;
  admin: boolean;
  userRoles?: string[] | undefined;
  sessionMinsLeft: number;
  timecards: ITimeCard[];
  projectTeam: boolean;
	outcomesIt: boolean;
}

export interface IDateRange {
  startDate: Date;
  endDate: Date;
}


export interface IUserTrainedOn {
  netId: string;
  trainedOn: string[];
}

export interface IPopupMessage {
  title: string;
  body: string;
  buttons: IActionButton[] | null;
  closeCallback: Function | null;
  noBlackFilter?: boolean;
}

export interface IActionButton {
  label: string;
  callbackFunction?: Function;
}

export interface ICoreHours {
  coreHoursId: number;
  dempoid: string;
  userDisplayName: string;
  month1?: Date;
  corehours1?: number;
  month2?: Date;
  corehours2?: number;
  month3?: Date;
  corehours3?: number;
  month4?: Date;
  corehours4?: number;
  month5?: Date;
  corehours5?: number;
  month6?: Date;
  corehours6?: number;
  month7?: Date;
  corehours7?: number;
  month8?: Date;
  corehours8?: number;
  month9?: Date;
  corehours9?: number;
  month10?: Date;
  corehours10?: number;
  month11?: Date;
  corehours11?: number;
  month12?: Date;
  corehours12?: number;
  month13?: Date;
  corehours13?: number;
  month14?: Date;
  corehours14?: number;
  entryBy: string;
  entryDt?: Date;
  modBy: string;
  modDt?: Date;
}

export interface IUserCoreHours {
  coreHours: any;
  user: User;
}

export interface IForecastHours {
  forecastHoursId: number;
  projectId?: number;
  month1?: Date;
  forecastHours1?: number;
  comment1: string;
  weekchk1?: boolean;
  weekendChk1?: boolean;
  freq1?: number;
  month2?: Date;
  forecastHours2?: number;
  comment2: string;
  weekchk2?: boolean;
  weekendChk2?: boolean;
  freq2?: number;
  month3?: Date;
  forecastHours3?: number;
  comment3: string;
  weekchk3?: boolean;
  weekendChk3?: boolean;
  freq3?: number;
  month4?: Date;
  forecastHours4?: number;
  comment4: string;
  weekchk4?: boolean;
  weekendChk4?: boolean;
  freq4?: number;
  month5?: Date;
  forecastHours5?: number;
  comment5: string;
  weekchk5?: boolean;
  weekendChk5?: boolean;
  freq5?: number;
  month6?: Date;
  forecastHours6?: number;
  comment6: string;
  weekchk6?: boolean;
  weekendChk6?: boolean;
  freq6?: number;
  month7?: Date;
  forecastHours7?: number;
  comment7: string;
  weekchk7?: boolean;
  weekendChk7?: boolean;
  freq7?: number;
  month8?: Date;
  forecastHours8?: number;
  comment8: string;
  weekchk8?: boolean;
  weekendChk8?: boolean;
  freq8?: number;
  month9?: Date;
  forecastHours9?: number;
  comment9: string;
  weekchk9?: boolean;
  weekendChk9?: boolean;
  freq9?: number;
  month10?: Date;
  forecastHours10?: number;
  comment10: string;
  weekchk10?: boolean;
  weekendChk10?: boolean;
  freq10?: number;
  month11?: Date;
  forecastHours11?: number;
  comment11: string;
  weekchk11?: boolean;
  weekendChk11?: boolean;
  freq11?: number;
  month12?: Date;
  forecastHours12?: number;
  comment12: string;
  weekchk12?: boolean;
  weekendChk12?: boolean;
  freq12?: number;
  month13?: Date;
  forecastHours13?: number;
  comment13: string;
  weekchk13?: boolean;
  weekendChk13?: boolean;
  freq13?: number;
  month14?: Date;
  forecastHours14?: number;
  comment14: string;
  weekchk14?: boolean;
  weekendChk14?: boolean;
  freq14?: number;
  entryBy: string;
  entryDt?: Date;
  modBy: string;
  modDt?: Date;
}

export interface IProjectForecastHours {
  project: IProjectMin;
  forecastHours: any;
}

export interface IProjectTotals {
  projectid?: number;
  projectName: string;
  scheduledate: string;
  dayNumber?: number;
  weekNumber?: number;
  yearNumber?: number;
  startdatetime: Date;
  enddatetime: Date;
  totalHours?: number;
}

export interface IProjectTotalsSummed {
  total?: number;
  yearnumber?: number;
  weeknumber?: number;
  daynumber?: number;
  scheduledate: string;
  startdatetime: Date;
}

export interface IProjectTotalsReport {
  projectTotals: IProjectTotals[];
  projectTotalsSummed: IProjectTotalsSummed[];
}

export interface IProjectTotalsReportFlat {
  projectColumn: string;
  column1: number;
  column2: number;
  column3: number;
  column4: number;
  column5: number;
  column6: number;
  column7: number;
  totalColumn: number;
}

export interface IRequest {
  requestId: number;
  requestType?: string;
  requestCodeId?: number | null;
  interviewerEmpName?: string;
  interviewerEmpId: string | null;
  resourceTeamMemberName?: string;
  resourceTeamMemberId: string | null;
  requestDate: Date;
  requestDetails: string;
  decision?: string;
  decisionId?: number | null;
  notes: string | null;
  entryBy: string;
  entryDt?: Date;
  modBy: string;
  modDt?: Date;
  changed?: boolean;
  edit?: boolean;
  invalid?: boolean;
  markedForDeletion?: boolean;
  invalidFields: string[];
}

export interface IProjectGroup {
  name: string;
  projects: IProjectMin[];
}

export interface IValidationMessage {
  validationMessagesId: number;
  dempoId: string | null;
  messageId: number;
  inMonth: Date;
  scheduleKeys: string | null;
  messageText: string | null;
  details: string | null;
  schedules?: ISchedule[];
}

export interface IValidationMessageText {
  validationMessageTextId: number;
  messageId: number;
  orderId: number;
  messageText: string;
}

export interface IBlockOutDate {
  blockOutDateId: number;
  blockOutDay: Date | null;
  allDay?: boolean;
  startTime: string;
  endTime: string;
}

export interface ILog {
  logId: Number;
  netId: string;
  type: string;
  messageLocation: string;
  message: string;
  fullMessage: string;
  logDate: Date;
}

export interface IKeyValue {
  key: string;
  value: string;
}
