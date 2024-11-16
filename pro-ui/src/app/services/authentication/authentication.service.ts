import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
//-not used in ep import { SessionMonitor } from 'oidc-client';
import { Observable, Subject, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
//-not used in ep import { Utils } from '../../classes/utils';
import { IAuthenticatedUser, IGeneralResponse } from '../../interfaces/interfaces';
//import { GlobalsService } from '../globals/globals.service';
import { LogsService } from '../logs/logs.service';
import { UsersService } from '../users/users.service';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  public authenticatedUser: BehaviorSubject<IAuthenticatedUser> = new BehaviorSubject<IAuthenticatedUser>({} as IAuthenticatedUser);
  public isAuthenticated: BehaviorSubject<boolean | undefined> = new BehaviorSubject<boolean | undefined>(undefined);
  public errorMessage!: string;
  private apiRootUrl = `${environment.DataAPIUrl}/api/users`;
  private debugUser: IAuthenticatedUser;
  private expectedRoles: Array<string>;

  //private allConfiguration: Observable<IAllConfiguration>;

  constructor(private http: HttpClient,
    private usersService: UsersService,
    private logsService: LogsService,
   /* private globalsService: GlobalsService*/) {
    //this.authenticatedUser = {
    //  DisplayName: '',
    //  DuDukeID: '',
    //  IsMemberOf: '',
    //  Interviewer: false,
    //  ResourceGroup: false,
    //  Admin: false,
    //}

    //set a default user for dev environment
    this.debugUser = {
      displayName: 'Jeremiah Reed',
      duDukeID: '0731702',
      eppn: 'jmr110@duke.edu',
      netID: 'jmr110@duke.edu'.replace('@duke.edu', ''),
      isMemberOf: 'urn:mace:duke.edu:groups:group-manager:roles:ccep-admin-val',
      interviewer: true,
      resourceGroup: false,
      admin: true,
      sessionMinsLeft: 25,
      timecards: [],
      projectTeam: false,
      outcomesIt: false
    }

    //set expected roles to validate against
    this.expectedRoles = [
      'urn:mace:duke.edu:groups:group-manager:roles:ccep-interviewer-' + environment.abbreviation.toLowerCase(),
      'urn:mace:duke.edu:groups:group-manager:roles:ccep-resourcegroup-' + environment.abbreviation.toLowerCase(),
      'urn:mace:duke.edu:groups:group-manager:roles:ccep-admin-' + environment.abbreviation.toLowerCase(),
    ]

  }

  //user authentication
  checkAuthenticatedUser(): Observable<IGeneralResponse> {

    return this.http.request<IGeneralResponse>('post', this.apiRootUrl + '/current', { withCredentials: true }).pipe(
      tap((response) => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.isAuthenticated.next(true);
          this.authenticatedUser.next(<IAuthenticatedUser>response.Subject);
        } else {
          this.isAuthenticated.next(false);
        }
      })
    ) as Observable<IGeneralResponse>;

  }

  setAuthenticatedUser(): void {
  }

  public logout(): void {
    this.http.request<IGeneralResponse>('delete', this.apiRootUrl + '/current', { withCredentials: true }).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          document.location.href = '/';
        }

      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage);
        alert('Unable to log you out. Please refresh the page and try again.');
        console.log(this.errorMessage);
      }
    );

  }

  getCurrentUser(netId: string):  Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/current/${netId}`).pipe(
      tap((response) => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          console.log("CurrentUser data (timecards) recieved  from the Server Sucessfully");
        } else {
          console.log("Could not get CurrentUser data from Server");
        }
      })
    ) as Observable<IGeneralResponse>;
  }


  //get value in session attributes page string based on the regular expression passed in
  private GetRegexValue(pattern: RegExp, sessionPage: string): string {
    const results = pattern.exec(sessionPage);
    if (!results)
      return '';
    if (results.length > 1)
      return results[1].trim();
    else
      return '';
  }

  //cleans up the isMemberOf to include only valid and expected roles, if any
  validateRoles(isMemberOf: string): string {
    let isMemberOfArray: string[] = isMemberOf.split(';');
    let returnValueArray: string[] = [];
    for (var i = 0; i < isMemberOfArray.length; i++) {
      if (this.expectedRoles.includes(isMemberOfArray[i])) {
        returnValueArray.push(isMemberOfArray[i]);
      }
    }

    return returnValueArray.join(';');
  }
}
