import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Injectable } from '@angular/core';
import { IAllConfiguration, IAdminOptionsVariable, IFormField, IFormFieldVariable, ISaveResponse, IUser, IGeneralResponse, IBlockOutDate } from '../../interfaces/interfaces';
import { environment } from '../../../environments/environment';
import { AppConfig } from '../../models/presentation/app-config';

@Injectable({
  providedIn: 'root'
})
export class ConfigurationService {
  private baseUrl = environment.DataAPIUrl;
  private apiRootUrl = `${environment.DataAPIUrl}/api/configuration`;
  public errorMessage!: string;
  public languages: BehaviorSubject<IFormFieldVariable> = new BehaviorSubject<IFormFieldVariable>({} as IFormFieldVariable);
  public appConfig: BehaviorSubject<AppConfig> = new BehaviorSubject<AppConfig>({} as AppConfig);

  constructor(private http:HttpClient) {
    this.setLanguages();
  }


  setLanguages(): void {
    this.http.get<IGeneralResponse>(`${this.apiRootUrl}/languages`).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.languages.next(<IFormFieldVariable>response.Subject);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
       // this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }

  //get app configuration
  setAppConfiguration(): void {
    this.http.get<IGeneralResponse>(this.apiRootUrl + '/frontEndConfiguration').subscribe(
      response => {
        this.appConfig.next(<AppConfig>response.Subject);
      }
    );
  }

  //get lock date
  getLockDate(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/adminOptions/schedulelock`);
  }

  //block-out dates
  //get future block-out dates
  getBlockOutDates(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/blockOutDates`);
  }
}
