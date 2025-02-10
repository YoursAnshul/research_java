import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Injectable } from '@angular/core';
import { IAllConfiguration, IAdminOptionsVariable, IFormField, IFormFieldVariable, ISaveResponse, IGeneralResponse, IBlockOutDate } from '../../interfaces/interfaces';
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

  getAllConfiguration(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(this.apiRootUrl);
  }

  //get app configuration
  setAppConfiguration(): void {
    this.http.get<IGeneralResponse>(this.apiRootUrl + '/frontEndConfiguration').subscribe(
      response => {
        this.appConfig.next(<AppConfig>response.Subject);
      }
    );
  }

  saveConfigurationVariables(configurations: IAdminOptionsVariable[]): Observable<IGeneralResponse> {
    return this.http.put<IGeneralResponse>(`${this.apiRootUrl}/adminOptionVariables`, configurations);
  }

  saveFieldNames(formFields: IFormFieldVariable[]): Observable<IGeneralResponse> {
    return this.http.put<IGeneralResponse>(`${this.apiRootUrl}/formFieldVariables`, formFields);
  }

  saveCodeLists(formFields: IFormFieldVariable[]): Observable<IGeneralResponse> {
    return this.http.put<IGeneralResponse>(`${this.apiRootUrl}/dropDownLists`, formFields);
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

  getUserFields(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/userFields`);
  }

  getProjectFields(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/projectFields`);
  }

  getFormField(fieldName: string): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/formField/${fieldName}`);
  }

  getFormFieldsByTable(tableName: string): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/formFieldsByTable/${tableName}`);
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

  //save a block-out date
  saveBlockOutDate(blockOutDate: IBlockOutDate): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(`${this.apiRootUrl}/blockOutDates`, blockOutDate);
  }

  saveBlockOutDateWithAudit(blockOutDate: IBlockOutDate,netId:String): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(`${this.apiRootUrl}/blockOutDates/${netId}`, blockOutDate);
  }

  //delete a block-out date
  deleteBlockOutDate(blockOutDate: IBlockOutDate): Observable<IGeneralResponse> {
    return this.http.request<IGeneralResponse>('delete', `${this.apiRootUrl}/blockOutDates`, { body: blockOutDate });
  }

  deleteBlockOutDateWithAudit(blockOutDate: IBlockOutDate,netId:String): Observable<IGeneralResponse> {
    return this.http.request<IGeneralResponse>('delete', `${this.apiRootUrl}/blockOutDates/${netId}`, { body: blockOutDate });
  }

  //comm hub
  //add default comm hub configuration
  addDefaultCommHubConfig(projectId: number): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(`${this.apiRootUrl}/commHubConfig`, { projectId: projectId });
  }

  //delete comm hub configuration
  deleteCommHubConfig(projectId: number): Observable<IGeneralResponse> {
    return this.http.request<IGeneralResponse>('delete', `${this.apiRootUrl}/commHubConfig`, { body: projectId });
  }

  //delete comm hub form field
  deleteCommHubFormField(FormFieldId: number): Observable<IGeneralResponse> {
    return this.http.request<IGeneralResponse>('delete', `${this.apiRootUrl}/commHubFormField`, { body: FormFieldId });
  }

}
