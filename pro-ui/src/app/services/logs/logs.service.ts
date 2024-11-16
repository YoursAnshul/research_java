import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { IGeneralResponse, ILog } from '../../interfaces/interfaces';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LogsService {
  private apiRootUrl = `${environment.DataAPIUrl}/api/logs`;

  constructor(private http: HttpClient) { }

  logError(message: string, messageLocation: string | null = null, fullMessage: string | null = null): void {
    this.logMessage('Error', message, messageLocation || '');
  }

  logMessage(type: string, message: string, messageLocation: string | null = null, fullMessage: string | null = null): void {
    let log: ILog = {
      logId: 0,
      netId: 'Unknown',
      type: type,
      message: message,
      messageLocation: messageLocation || '',
      fullMessage: fullMessage || '',
      logDate: new Date()
    };

    this.logMessageObject(log);

  }

  logMessageObject(log: ILog): void {
    this.http.post<IGeneralResponse>(this.apiRootUrl, log).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          //console.log(response.Message);
        } else {
          console.log(response.Message);
        }
      },
      error => {
        console.log(<string>(error.message));
      }
    );
  }

}
