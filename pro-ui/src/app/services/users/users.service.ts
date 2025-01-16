import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject, catchError, tap } from 'rxjs';
import { Injectable } from '@angular/core';
import { ICoreHours, ICurrentUser, IFormFieldVariable, IGeneralResponse, ITimeCard } from '../../interfaces/interfaces';
import { LogsService } from '../logs/logs.service';
import { environment } from '../../../environments/environment';
import { User } from '../../models/data/user';

@Injectable({
  providedIn: 'root'
})

export class UsersService {

  private apiRootUrl = `${environment.DataAPIUrl}/api/users`;
  public currentUser: BehaviorSubject<ICurrentUser> = new BehaviorSubject<ICurrentUser>({} as ICurrentUser);
  public selectedUser: BehaviorSubject<User> = new BehaviorSubject<User>({} as User);
  public allUsersMin: BehaviorSubject<User[]> = new BehaviorSubject<User[]>([] as User[]);

  errorMessage!: string;

  constructor(private http: HttpClient,
    private logsService: LogsService) {
    //set list of all users
    this.setAllUsersMin();
  }

  getAllUsers(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(this.apiRootUrl);
  }

  setAllUsersMin(): void {
    this.http.get<IGeneralResponse>(`${this.apiRootUrl}/min`).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.allUsersMin.next(<User[]>response.Subject);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }

  getAllActiveUsers(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/active`);
  }

  getAllActiveUserIds(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/activeIds`);
  }

  getUserByNetId(netId: string): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/${netId}`);
  }

  setCurrentUser(netId: string): void {

    //get shibboleth session info and parse it out
    this.http.get<IGeneralResponse>(`${this.apiRootUrl}/current/${netId}`).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.currentUser.next(<ICurrentUser>response.Subject);
        } else {
          this.logsService.logError(response.Message);
          this.errorMessage = response.Message;
          this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

    this.reconcileCoreHours().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          //this.logsService.logMessage('General', response.Message);
          console.log(response.Message);
        } else {
          this.logsService.logError(response.Message);
          console.log(response.Message);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  //set selected user
  setSelectedUser(netId: string): void {
    //get user to bind form field values
    this.getUserByNetId(netId).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.selectedUser.next(<User>response.Subject);
        }
      }
    );

    this.reconcileCoreHours().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          //this.logsService.logMessage('General', response.Message);
          console.log(response.Message);
        } else {
          this.logsService.logError(response.Message);
          console.log(response.Message);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  timeInOut(timecard: ITimeCard): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(`${this.apiRootUrl}/timecards`, timecard);
  }

  saveUser(user: User): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(this.apiRootUrl, user);
  }

  SaveActiveAndLock(users: User[]): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(`${this.apiRootUrl}/saveActiveAndLock`, users);
  }

  saveUserCoreHours(coreHours: ICoreHours[]): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(`${this.apiRootUrl}/coreHours`, coreHours);
  }

  getUserCoreHoursByNetId(netId: string): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/coreHours-V2/${netId}`);
  }

  getAllUserCoreHours(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/corehours`);
  }

  reconcileCoreHours(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/coreHours/reconcile`);
  }

}
