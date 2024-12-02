import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { BehaviorSubject, Observable } from 'rxjs';
import { IGeneralResponse, IRequest } from '../../interfaces/interfaces';
import { LogsService } from '../logs/logs.service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RequestsService {
  private apiRootUrl = `${environment.DataAPIUrl}/api/requests`;
  public requests: BehaviorSubject<IRequest[]> = new BehaviorSubject<IRequest[]>([] as IRequest[]);
  private errorMessage!: string;

  constructor(private http: HttpClient,
    private logsService: LogsService) { }

  //set the requests array
  setAllRequests(): void {
    this.http.get<IGeneralResponse>(this.apiRootUrl).subscribe(
      response => {
        if (response.Status == 'Success') {
          this.requests.next(<IRequest[]>response.Subject);
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

  //get all requests
  getAllRequests(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(this.apiRootUrl);
  }

  //get requests by Net ID
  getRequestsByNetId(netId: string): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/${netId}`);
  }

  //save one or more requests
  saveRequests(requests: IRequest[]): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(this.apiRootUrl, requests);
  }

  //delete one or more requests
  deleteRequests(requests: IRequest[]): Observable<IGeneralResponse> {
    return this.http.request<IGeneralResponse>('delete', this.apiRootUrl, { body: requests });
  }

}
