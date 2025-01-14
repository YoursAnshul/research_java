import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { request } from 'http';
import { BehaviorSubject, Observable } from 'rxjs';
import { ICommunicationsHubEntry, IGeneralResponse } from '../../interfaces/interfaces';
import { LogsService } from '../logs/logs.service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CommunicationsHubService {
  private apiRootUrl = `${environment.DataAPIUrl}/api/communicationsHub`;
  public communicationsHubEntries: BehaviorSubject<ICommunicationsHubEntry[]> = new BehaviorSubject<ICommunicationsHubEntry[]>([] as ICommunicationsHubEntry[]);
  private errorMessage!: string;

  constructor(private http: HttpClient,
    private logsService: LogsService) { }

  //set the comm hub entries array
  setAllCommunicationsHubEntries(): void {
    this.http.get<IGeneralResponse>(this.apiRootUrl).subscribe(
      response => {
        if (response.Status == 'Success') {
          this.communicationsHubEntries.next(<ICommunicationsHubEntry[]>response.Subject);
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
  }

  //get all comm hub entries
  getAllCommunicationsHub(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(this.apiRootUrl);
  }

  //get comm hub entries by project ID
  getCommunicationsHubByProjectId(projectId: string): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/${projectId}`);
  }

  //save one or more comm hub entries
  saveCommunicationsHubEntries(communicationsHubEntries: ICommunicationsHubEntry[]): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(this.apiRootUrl, communicationsHubEntries);
  }

  //delete one or more comm hub entries
  deleteCommunicationsHub(communicationsHubEntries: ICommunicationsHubEntry[]): Observable<IGeneralResponse> {
    return this.http.request<IGeneralResponse>('delete', this.apiRootUrl, { body: communicationsHubEntries });
  }

}
