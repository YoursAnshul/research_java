import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import {provideHttpClient} from "@angular/common/http";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatIconModule } from '@angular/material/icon';
import { HttpClientModule } from '@angular/common/http';
import { SidebarComponent } from './sidebar/sidebar.component';
import { HomeComponent } from './pages/home/home.component';
import { SchedulingComponent } from './pages/scheduling/scheduling.component';
import { AssignmentsComponent } from './pages/assignments/assignments.component';
import { ParticipantsComponent } from './pages/participants/participants.component';
import { ProjectsComponent } from './pages/projects/projects.component';
import { CommunicationsComponent } from './pages/communications/communications.component';
import { ReportsComponent } from './pages/reports/reports.component';
import { UsersComponent } from './pages/users/users.component';
import { ForecastingComponent } from './pages/forecasting/forecasting.component';
import { RequestsComponent } from './pages/requests/requests.component';
import { ConfigurationComponent } from './pages/configuration/configuration.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { LoginComponent } from './pages/login/login.component';
import { HeaderComponent } from './pages/header/header.component';
import { CalendarComponent } from './components/calendar/calendar.component';
import { DayViewComponent } from './components/calendar/day-view/day-view.component';
import { WeekViewComponent } from './components/calendar/week-view/week-view.component';
import { MonthViewComponent } from './components/calendar/month-view/month-view.component';
import { DayDatepickerComponent } from './components/calendar/datepickers/day-datepicker/day-datepicker.component';
import { CalendarControlsComponent } from './components/calendar/calendar-controls/calendar-controls.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { UserCardComponent } from './components/calendar/user-card/user-card.component';
import { ProjectSwatchComponent } from './components/snippets/project-swatch/project-swatch.component';
import { LegendComponent } from './components/calendar/legend/legend.component';

// angular material
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule, MAT_DATE_FORMATS, MatRippleModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialogModule} from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatTableModule } from '@angular/material/table';
import {MatSortModule} from '@angular/material/sort';

import { ScheduleComponent } from './components/schedule/schedule.component';
import { LanguageIconComponent } from './components/language-icon/language-icon.component';
import { DialogComponent } from './components/dialog/dialog.component';
import {MonthDatepickerComponent} from "./components/calendar/datepickers/month-datepicker/month-datepicker.component";
import {WeekDatepickerComponent} from "./components/calendar/datepickers/week-datepicker/week-datepicker.component";
import {ScheduleLineComponent} from "./components/schedule/schedule-line/schedule-line.component";
import {RangeDatepickerComponent} from "./components/calendar/datepickers/range-datepicker/range-datepicker.component";
import {MatPaginator} from "@angular/material/paginator";
import {ModalPopupComponent} from "./components/modal-popup/modal-popup.component";
import {NavMenuComponent} from "./components/nav-menu/nav-menu.component";
import { HighlightSearch } from './components/highlightsearch/highlight.search';
import {MatCheckboxModule} from '@angular/material/checkbox'
import { CustomPageComponent } from './pages/custom-page/custom.page.component';
import { ParticipantSearchComponent } from './components/participant-search/participant-search.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatPaginatorModule } from '@angular/material/paginator';
import { HoverMessageComponent } from './components/hover-message/hover-message.component';
import { ParticipantIdComponent } from './components/participant-id/participant-id.component';
import { AnnouncementsComponent } from './pages/announcements/announcements.component';
import { ManageAnnouncementsComponent } from './pages/announcements/announcement.management.component';
import { AddAnnouncementDialogComponent } from './pages/announcements/add-announcement-dialog.component';
@NgModule({
  declarations: [
    ManageAnnouncementsComponent,
    AddAnnouncementDialogComponent,
    AppComponent,
    CalendarComponent,
    UserCardComponent,
    ProjectSwatchComponent,
    DayViewComponent,
    DayDatepickerComponent,
    CalendarControlsComponent,
    DashboardComponent,
    SidebarComponent,
    HomeComponent,
    SchedulingComponent,
    AssignmentsComponent,
    ParticipantsComponent,
    ProjectsComponent,
    CommunicationsComponent,
    ReportsComponent,
    UsersComponent,
    ForecastingComponent,
    RequestsComponent,
    ConfigurationComponent,
    LoginComponent,
    HeaderComponent,
    LegendComponent,
    LanguageIconComponent,
    DialogComponent,
    LanguageIconComponent,
    WeekViewComponent,
    MonthViewComponent,
    MonthDatepickerComponent,
    WeekDatepickerComponent,
    ScheduleLineComponent,
    ModalPopupComponent,
    NavMenuComponent,
    RangeDatepickerComponent,
    ScheduleComponent,
    HighlightSearch,
    CustomPageComponent,
    ParticipantSearchComponent,
    HoverMessageComponent,
    ParticipantIdComponent,
    AnnouncementsComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatIconModule,
    HttpClientModule,
    MatTooltipModule,
    NgbModule,
    MatTabsModule,
    MatFormFieldModule,
    MatDatepickerModule,
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    MatNativeDateModule,
    MatSelectModule,
    MatCardModule,
    MatButtonModule,
    MatDividerModule,
    MatDialogModule,
    MatPaginator,
    MatPaginatorModule,
    MatMenuModule,
    MatCheckboxModule,
    MatTableModule,
    MatSortModule,
    MatProgressSpinnerModule,
    MatRippleModule
  ],
  providers: [provideHttpClient()],
  bootstrap: [AppComponent]
})
export class AppModule { }
