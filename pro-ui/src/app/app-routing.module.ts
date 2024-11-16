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

const routes: Routes = [

  { path: '', component: HomeComponent, pathMatch: 'full'},
  { path: 'dashBoard', component: DashboardComponent, pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'scheduling', component: SchedulingComponent },
  { path: 'assignments', component: AssignmentsComponent},
  { path: 'participants', component: ParticipantsComponent},
  { path: 'projects', component: ProjectsComponent},
  { path: 'communications', component: CommunicationsComponent},
  { path: 'reports', component: ReportsComponent},
  { path: 'users', component: UsersComponent},
  { path: 'forecasting', component: ForecastingComponent},
  { path: 'requests', component: RequestsComponent },
  { path: 'configuration', component: ConfigurationComponent},

  //{ path: 'test', component: TestComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }