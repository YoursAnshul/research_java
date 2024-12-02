import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { LogsService } from '../logs/logs.service';
import { IGeneralResponse } from '../../interfaces/interfaces';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ParticipantService {
  private apiRootUrl = `${environment.DataAPIUrl}/api/participants`;

  constructor(private http: HttpClient,
    private logsService: LogsService) { }

  getLookup(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/lookup`);
  }
}
