import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { MatNativeDateModule, MAT_DATE_FORMATS } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { MatCheckbox, MatCheckboxModule } from '@angular/material/checkbox';
import { MatPaginatorModule } from '@angular/material/paginator';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSelectModule } from '@angular/material/select';
import { MatRadioModule } from '@angular/material/radio';
import { MatTableModule } from '@angular/material/table';
import { MatListModule } from '@angular/material/list';
import { MatListOption } from '@angular/material/list';
import { MatSelectionListChange } from '@angular/material/list';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatMenuModule } from '@angular/material/menu';

import { AppComponent } from './app.component';
import { HeaderComponent } from './components/header/header.component';
import { NavMenuComponent } from './components/nav-menu/nav-menu.component';
import { HomeComponent } from './pages/home/home.component';
import { UsersComponent } from './pages/users/users.component';
import { ProjectsComponent } from './pages/projects/projects.component';
import { ReportsComponent } from './pages/reports/reports.component';
import { ForecastingComponent } from './pages/forecasting/forecasting.component';
import { AdminComponent } from './pages/admin/admin.component';
import { TestComponent } from './pages/test/test.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { UsersService } from './services/users/users.service';
import { ConfigurationComponent } from './pages/configuration/configuration.component';
import { CalendarComponent } from './components/calendar/calendar.component';
import { DayViewComponent } from './components/calendar/day-view/day-view.component';
import { WeekViewComponent } from './components/calendar/week-view/week-view.component';
import { MonthViewComponent } from './components/calendar/month-view/month-view.component';
import { UserCardComponent } from './components/calendar/user-card/user-card.component';
import { ScheduleCardComponent } from './components/calendar/schedule-card/schedule-card.component';
import { CalendarControlsComponent } from './components/calendar/calendar-controls/calendar-controls.component';
import { LanguageIconComponent } from './components/language-icon/language-icon.component';
import { AuthenticationService } from './services/authentication/authentication.service';
import { IAuthenticatedUser } from './interfaces/interfaces';
import { environment } from '../environments/environment';
import { RequestsComponent } from './pages/requests/requests.component';
import { GlobalsService } from './services/globals/globals.service';
import { MomentDateAdapter, MAT_MOMENT_DATE_ADAPTER_OPTIONS } from '@angular/material-moment-adapter';
import { DayDatepickerComponent } from './components/calendar/datepickers/day-datepicker/day-datepicker.component';
import { WeekDatepickerComponent } from './components/calendar/datepickers/week-datepicker/week-datepicker.component';
import { MonthDatepickerComponent } from './components/calendar/datepickers/month-datepicker/month-datepicker.component';
import { TimeInOutComponent } from './components/nav-menu/time-in-out/time-in-out.component';
import { Utils } from './classes/utils';
import { ProjectsService } from './services/projects/projects.service';
import { ConfigurationService } from './services/configuration/configuration.service';
import { ScheduleComponent } from './components/schedule/schedule.component';
import { ScheduleLineComponent } from './components/schedule/schedule-line/schedule-line.component';
import { UserSchedulesService } from './services/userSchedules/user-schedules.service';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { RangeDatepickerComponent } from './components/calendar/datepickers/range-datepicker/range-datepicker.component';
import { FormFieldComponent } from './components/form-field/form-field.component';
import { TrainedOnProjectsComponent } from './components/trained-on-projects/trained-on-projects.component';
import { TrainedOnUsersComponent } from './components/trained-on-users/trained-on-users.component';
import { AnySelectComponent } from './components/any-select/any-select.component';
import { ModalPopupComponent } from './components/modal-popup/modal-popup.component';
import { UserFormComponent } from './components/forms/user-form/user-form.component';
import { ProjectFormComponent } from './components/forms/project-form/project-form.component';
import { ProjectSwatchComponent } from './components/snippets/project-swatch/project-swatch.component';
import { DataSource } from '@angular/cdk/collections';
import { CommunicationsHubComponent } from './pages/communications-hub/communications-hub.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { WithCredentialsInterceptor } from './components/http-interceptor/with-credentials-interceptor';
import { LoginComponent } from './pages/login/login.component';
//import { FormFieldCustomPhoneControl, MyTelInput } from './components/form-field-phone/form-field-phone.component';
//import { CdkVirtualScrollViewport } from "@angular/cdk/scrolling";

@NgModule({
  declarations: [
    AppComponent,
    NavMenuComponent,
    HeaderComponent,
    HomeComponent,
    UsersComponent,
    ProjectsComponent,
    ReportsComponent,
    ForecastingComponent,
    AdminComponent,
    TestComponent,
    ConfigurationComponent,
    CalendarComponent,
    DayViewComponent,
    WeekViewComponent,
    MonthViewComponent,
    UserCardComponent,
    ScheduleCardComponent,
    CalendarControlsComponent,
    LanguageIconComponent,
    RequestsComponent,
    DayDatepickerComponent,
    WeekDatepickerComponent,
    MonthDatepickerComponent,
    TimeInOutComponent,
    ScheduleComponent,
    ScheduleLineComponent,
    RangeDatepickerComponent,
    FormFieldComponent,
    TrainedOnProjectsComponent,
    TrainedOnUsersComponent,
    AnySelectComponent,
    ModalPopupComponent,
    UserFormComponent,
    ProjectFormComponent,
    ProjectSwatchComponent,
    CommunicationsHubComponent,
    LoginComponent,
    //MyTelInput,
  ],
  exports: [
    FormsModule,
    MatFormFieldModule,
    MatTabsModule,
    MatSelectModule,
    MatRadioModule,
    MatDatepickerModule,
    MatInputModule,
    ScrollingModule,
    MatCheckboxModule,
    MatPaginatorModule,
    MatListModule,
    MatGridListModule,
    MatMenuModule,
    //FormFieldCustomPhoneControl,
    //MyTelInput
    //CdkVirtualScrollViewport
  ],
  imports: [
    BrowserModule.withServerTransition({ appId: 'ng-cli-universal' }),
    HttpClientModule,
    FormsModule,
    MatFormFieldModule,
    MatTabsModule,
    MatSelectModule,
    MatRadioModule,
    MatDatepickerModule,
    MatInputModule,
    MatCheckboxModule,
    MatPaginatorModule,
    MatNativeDateModule,
    ReactiveFormsModule,
    MatTableModule,
    MatListModule,
    RouterModule.forRoot([
      { path: '', component: HomeComponent, pathMatch: 'full' },
      { path: 'users', component: UsersComponent },
      { path: 'projects', component: ProjectsComponent },
      { path: 'reports', component: ReportsComponent },
      { path: 'forecasting', component: ForecastingComponent },
      { path: 'requests', component: RequestsComponent },
      { path: 'configuration', component: ConfigurationComponent },
      { path: 'communications-hub', component: CommunicationsHubComponent },
      { path: 'test', component: TestComponent },
    ],),
    BrowserAnimationsModule,
    ScrollingModule,
  ],
  providers: [
    UsersService,
    UserSchedulesService,
    ProjectsService,
    ConfigurationService,
    AuthenticationService,
    GlobalsService,
    { provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: { appearance: 'fill' } },
    { provide: HTTP_INTERCEPTORS, useClass: WithCredentialsInterceptor, multi: true },
    provideAnimationsAsync(),
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  public authenticatedUser!: IAuthenticatedUser;
  errorMessage!: string;

  constructor(private authenticationService: AuthenticationService, private usersService: UsersService, private globalsService: GlobalsService) {
      //set authenticated user
    this.authenticationService.setAuthenticatedUser();
    console.log(environment.name);

  }
}
