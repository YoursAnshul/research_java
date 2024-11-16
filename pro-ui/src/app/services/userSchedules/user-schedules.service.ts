import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Injectable } from '@angular/core';
import { ISchedule, IScheduleMin, ITimeCode, IGeneralResponse, IDateRange, IValidationMessage } from '../../interfaces/interfaces';
import { Utils } from '../../classes/utils';
import { LogsService } from '../logs/logs.service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserSchedulesService {

  private apiRootUrl = `${environment.DataAPIUrl}/api/userSchedules`;
  public userSchedules: BehaviorSubject<ISchedule[]> = new BehaviorSubject<ISchedule[]>([] as ISchedule[]);
  public scheduleCache: BehaviorSubject<ISchedule[]> = new BehaviorSubject<ISchedule[]>([] as ISchedule[]);
  public selectedDate: BehaviorSubject<Date> = new BehaviorSubject<Date>(new Date());
  public timeCodes: BehaviorSubject<ITimeCode[]> = new BehaviorSubject<ITimeCode[]>([] as ITimeCode[]);
  public scheduleFetchStatus: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  public errorMessage!: string;

  constructor(private http: HttpClient,
    private logsService: LogsService) {

    this.selectedDate.next(new Date());

    this.setAllUserSchedulesByAnchorDate(Utils.formatDateOnlyToString(new Date(), true, true, true) || '');

    //set time codes
    this.http.get<IGeneralResponse>(`${this.apiRootUrl}/timecodes`).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.timeCodes.next(<ITimeCode[]>response.Subject);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }

  //get all user schedules
  getAllUserSchedules(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(this.apiRootUrl);
  }

  //set the all users array based on the given anchor date
  setAllUserSchedulesByAnchorDate(anchorDate: string | null): void {
    //set the fetch status to false
    this.scheduleFetchStatus.next(true);
    const params = new HttpParams()
    .set('startDate', anchorDate || "")
    .set('endDate', anchorDate || "");

    this.http.get<IGeneralResponse>(`${this.apiRootUrl}/${anchorDate}`, { params }).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let userSchedules: ISchedule[] = <ISchedule[]>response.Subject;

          //use new Date() on each schedule to convert from the stored UTC time to the local browser time
          //also, set the string formatted start/end times for display purposes (this avoids having to format the time everywhere it's displayed)
          for (var i = 0; i < userSchedules.length; i++) {
            userSchedules[i].startdatetime = new Date(userSchedules[i].startdatetime || '');
            userSchedules[i].enddatetime = new Date(userSchedules[i].enddatetime || '');
            userSchedules[i].startTime = Utils.formatDateToTimeString(userSchedules[i].startdatetime, true) || '';
            userSchedules[i].endTime = Utils.formatDateToTimeString(userSchedules[i].enddatetime, true) || '';
            userSchedules[i].initialProjectid = userSchedules[i].projectid;
            //get the day of week - since this is 0-based but we expect Sunday to be at the end of the week (7), we adjust if the day of week is 0 by changing it to 7
            let dayOfWeek: number = userSchedules[i].startdatetime?.getDay() as number;
            if (dayOfWeek == 0)
              dayOfWeek = 7;
            userSchedules[i].dayOfWeek = dayOfWeek;
          }

          //userSchedules.sort((a, b) => {
          //  return <any>new Date(a.Startdatetime) - <any>new Date(b.Startdatetime);
          //});

          this.userSchedules.next(userSchedules);

          this.setScheduleCache();

          //set the fetch status to false
          this.scheduleFetchStatus.next(false);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage);
      }
    );
  }

  //get schedules for a given date range
  getAllUserSchedulesByRange(startDate: string, endDate: string): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/range?startDate=${startDate}&endDate=${endDate}`);
  }


  setAllUserSchedulesByRange(startDate: string | null,endDate: string | null): void {
    //set the fetch status to false
    this.scheduleFetchStatus.next(true);
    const params = new HttpParams()
      .set('startDate', startDate || "")
      .set('endDate', endDate || "");

    this.http.get<IGeneralResponse>(`${this.apiRootUrl}/range`, { params }).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let userSchedules: ISchedule[] = <ISchedule[]>response.Subject;

          //use new Date() on each schedule to convert from the stored UTC time to the local browser time
          //also, set the string formatted start/end times for display purposes (this avoids having to format the time everywhere it's displayed)
          for (var i = 0; i < userSchedules.length; i++) {
            userSchedules[i].startdatetime = new Date(userSchedules[i].startdatetime || '');
            userSchedules[i].enddatetime = new Date(userSchedules[i].enddatetime || '');
            userSchedules[i].startTime = Utils.formatDateToTimeString(userSchedules[i].startdatetime, true) || '';
            userSchedules[i].endTime = Utils.formatDateToTimeString(userSchedules[i].enddatetime, true) || '';
            userSchedules[i].initialProjectid = userSchedules[i].projectid;
            //get the day of week - since this is 0-based but we expect Sunday to be at the end of the week (7), we adjust if the day of week is 0 by changing it to 7
            let dayOfWeek: number = userSchedules[i].startdatetime?.getDay() as number;
            if (dayOfWeek == 0)
              dayOfWeek = 7;
            userSchedules[i].dayOfWeek = dayOfWeek;
          }

          //userSchedules.sort((a, b) => {
          //  return <any>new Date(a.Startdatetime) - <any>new Date(b.Startdatetime);
          //});

          this.userSchedules.next(userSchedules);

          this.setScheduleCache();

          //set the fetch status to false
          this.scheduleFetchStatus.next(false);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage);
      }
    );
  }



  //get a large cache of schedules for multi-year comparisons
  setScheduleCache(): void {
    this.http.get<IGeneralResponse>(`${this.apiRootUrl}/scheduleCache`).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          let userSchedules: ISchedule[] = <ISchedule[]>response.Subject;

          //use new Date() on each schedule to convert from the stored UTC time to the local browser time
          //also, set the string formatted start/end times for display purposes (this avoids having to format the time everywhere it's displayed)
          for (var i = 0; i < userSchedules.length; i++) {
            userSchedules[i].startdatetime = new Date(userSchedules[i].startdatetime || '');
            userSchedules[i].enddatetime = new Date(userSchedules[i].enddatetime || '');
            userSchedules[i].startTime = Utils.formatDateToTimeString(userSchedules[i].startdatetime, true) || '';
            userSchedules[i].endTime = Utils.formatDateToTimeString(userSchedules[i].enddatetime, true) || '';
          }

          this.scheduleCache.next(userSchedules);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage);
      });
  }

  //get validation messages for the specified month for any user
  getValidationMessages(inDate: Date): Observable<IGeneralResponse> {
    inDate.setDate(1);
    let inDateString: string = Utils.formatDateOnlyToString(inDate, true, true, true) || '';
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/validationMessages/${inDateString}`);
  }

  //get validation messages for the specified month and user
  getUserValidationMessages(inDate: Date, netId: string): Observable<IGeneralResponse> {
    inDate.setDate(1);
    let inDateString: string = Utils.formatDateOnlyToString(inDate, true, true, true) || '';
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/validationMessages/${inDateString}?netId=${netId}`);
  }

  //generate and return validation messages
  validateSchedules(validationMessages: IValidationMessage[]): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(`${this.apiRootUrl}/validation`, validationMessages);
  }

  //save one or more schedules
  saveSchedules(schedules: IScheduleMin[]): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(this.apiRootUrl, schedules);
  }

  //delete one or more schedules
  deleteSchedules(schedules: IScheduleMin[]): Observable<IGeneralResponse> {
    return this.http.request<IGeneralResponse>('delete', this.apiRootUrl, { body: schedules });
  }

}
