import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { HomeComponent } from './pages/home/home.component';
import { AssignmentsComponent } from './pages/assignments/assignments.component';
import { CommunicationsComponent } from './pages/communications/communications.component';
import { ParticipantsComponent } from './pages/participants/participants.component';
import { ProjectsComponent } from './pages/projects/projects.component';
import { ReportsComponent } from './pages/reports/reports.component';
import { SchedulingComponent } from './pages/scheduling/scheduling.component';
import { UsersComponent } from './pages/users/users.component';
import { ForecastingComponent } from './pages/forecasting/forecasting.component';
import { RequestsComponent } from './pages/requests/requests.component';
import { ConfigurationComponent } from './pages/configuration/configuration.component';
import { LoginComponent } from './pages/login/login.component';
import { CustomPageComponent } from './pages/custom-page/custom.page.component';
import { ParticipantIdComponent } from './components/participant-id/participant-id.component';
import { ParticipantSearchComponent } from './components/participant-search/participant-search.component';
import { ManageAnnouncementsComponent } from './pages/announcements/announcement.management.component';
import { SchedulingInfoComponent } from './components/scheduling-info/scheduling-info.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { UnsavedChangesGuard } from './guards/unsaved-changes.guard';

const routes: Routes = [

  { path: '', component: HomeComponent, pathMatch: 'full'},
  { path: 'dashBoard', component: DashboardComponent, pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'scheduling', component: SchedulingComponent },
  { path: 'assignments', component: AssignmentsComponent},
  { path: 'participants', component: ParticipantsComponent},
  { path: 'search-participants',  data: { hideHeaderFooter: true }, component: ParticipantSearchComponent},
  { path: 'participants/:id',  component: ParticipantIdComponent},
  { path: 'projects', component: ProjectsComponent},
  { path: 'communications', component: CommunicationsComponent},
  { path: 'reports', component: ReportsComponent},
  { path: 'users', component: UsersComponent, canDeactivate: [UnsavedChangesGuard]},
  { path: 'forecasting', component: ForecastingComponent},
  { path: 'requests', component: RequestsComponent },
  { path: 'configuration', component: ConfigurationComponent},
  { path: 'book-reference', data: { hideHeaderFooter: true }, component: CustomPageComponent},
  { path: 'announcement-management', component: ManageAnnouncementsComponent},
  { path: 'scheduling-info', data: { hideHeaderFooter: true }, component: SchedulingInfoComponent},
  { path: 'user-profile', component: UserProfileComponent, canDeactivate: [UnsavedChangesGuard]}
  //{ path: 'test', component: TestComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
