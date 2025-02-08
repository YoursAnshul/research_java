import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Utils } from '../../classes/utils';
import { IForecastHours, IGeneralResponse, IProject, IProjectMin } from '../../interfaces/interfaces';
import { LogsService } from '../logs/logs.service';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProjectsService {

  private apiRootUrl = `${environment.DataAPIUrl}/api/projects`;
  public errorMessage!: string;
  public allProjectsMin: BehaviorSubject<IProjectMin[]> = new BehaviorSubject<IProjectMin[]>([] as IProjectMin[]);
  public selectedProject: BehaviorSubject<IProject> = new BehaviorSubject<IProject>({} as IProject);
  isLoading = new BehaviorSubject<boolean>(false); 

  constructor(private http: HttpClient,
    private logsService: LogsService) {
    this.setAllProjectsMin();

    this.reconcileForecasting().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          //this.logsService.logMessage('General', response.Message);
          console.log(response.Message);
        } else {
          this.logsService.logError(response.Message);
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );

  }

  setAllProjectsMin(): void {
    this.isLoading.next(true); 
    this.http.get<IGeneralResponse>(`${this.apiRootUrl}/min`).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.allProjectsMin.next(<IProjectMin[]>response.Subject);
        }
        this.isLoading.next(false); 
      },
      error => {
        this.isLoading.next(false);
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }

  //set selected user
  setSelectedProject(projectId: number): void {
    //get user to bind form field values
    this.getProjectById(projectId).subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.selectedProject.next(<IProject>response.Subject);
        }
      }
    );
  }

  getProjectById(projectId: number): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/${projectId}`);
  }

  saveProject(project: IProject): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(this.apiRootUrl, project);
  }

  saveTrainedOn(project: IProjectMin): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(`${this.apiRootUrl}/trainedOn`, project);
  }

  saveForecasting(forecastHours: IForecastHours[]): Observable<IGeneralResponse> {
    return this.http.post<IGeneralResponse>(`${this.apiRootUrl}/forecasting`, forecastHours);
  }

  getForecastingByProjectId(projectId: number): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/forecasting/${projectId}`);
  }

  getAllForecasting(): Observable<IGeneralResponse> {
  return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/forecasting`);
  }

  reconcileForecasting(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/forecasting/reconcile`);
  }

  getProjectTotalsReport(weekStart: Date, weekEnd: Date): Observable<IGeneralResponse> {
    let weekStartString: string = Utils.formatDateOnlyToStringUTC(weekStart, true,true,true) || '';
    let weekEndString: string = Utils.formatDateOnlyToStringUTC(weekEnd, true,true,true) || '';
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/projectTotals?weekStart=${weekStartString}&weekEnd=${weekEndString}`);
  }

  getAllActiveProjectDetails$(): Observable<IGeneralResponse> {
    return this.http.get<IGeneralResponse>(`${this.apiRootUrl}/active`);
  }

}
