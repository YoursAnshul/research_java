import { Component, OnInit } from '@angular/core';
import { Utils } from '../../classes/utils';
import { IActionButton, IAuthenticatedUser, IProjectMin } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { GlobalsService } from '../../services/globals/globals.service';
import { ProjectsService } from '../../services/projects/projects.service';
import { UsersService } from '../../services/users/users.service';
import { User } from '../../models/data/user';

@Component({
  selector: 'app-nav-menu',
  templateUrl: './nav-menu.component.html',
  styleUrls: ['./nav-menu.component.css']
})
export class NavMenuComponent implements OnInit {
  authenticatedUser!: IAuthenticatedUser;
  usersLabel: string = 'User';
  selectedPage: string = '';
  allProjects: IProjectMin[] = [];
  currentChanges: boolean = false;
  allowNavigation: boolean = true;
  allUsers: User[] = [];
  authenticatedUserProfile: User = {} as User;
  trainedOnCommHubProject: boolean = false;

  constructor(private authenticationService: AuthenticationService,
    private globalsService: GlobalsService,
    private projectsService: ProjectsService,
    private usersService: UsersService) {

    //get all projects
    this.projectsService.allProjectsMin.subscribe(
      allProjects => {
        this.allProjects = allProjects.filter(x => x.active);

        this.setAuthenticatedUserProfile();

      }
    );

    //subscribe to users
    this.usersService.allUsersMin.subscribe(
      allUsers => {
        this.allUsers = allUsers;

        this.setAuthenticatedUserProfile();

      }
    );

  }

  ngOnInit(): void {
    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;
        if (!authenticatedUser.interviewer) {
          this.usersLabel = 'Users';
        }

        this.setAuthenticatedUserProfile();

      }
    );

    //subscribe to current data changes
    this.globalsService.currentChanges.subscribe(
      currentChanges => {
        this.currentChanges = currentChanges;

        if (currentChanges) {
          this.allowNavigation = false;
        } else {
          this.allowNavigation = true;
        }

      }
    );

    //subscribe to the selected page
    this.globalsService.selectedPage.subscribe(
      selectedPage => {
        this.selectedPage = selectedPage;
      }
    );
  }

  //open add a schedule popup
  openSchedulePopup(): void {
    this.globalsService.openSchedulePopup();
  }

  //check if the current page is selected
  checkSelected(route: string): boolean {
    if (route == this.selectedPage)
      return true;
    else
      return false;
  }

  projectSwatchStyle(projectColor: string) {
    return {
      'width': '30px',
      'min-height': '33px',
      'display': 'inline-block',
      'margin-right': '10px',
      'border': '1px solid #87cefa',
      'box-shadow': '0 3px 5px -1px rgb(0 0 0 / 20%), 0 25px 25px 0 rgb(0 0 0 / 14%), 0 1px 0 0 rgb(0 0 0 / 12%)',
      'background-color': projectColor,
    };
  }

  confirmIfChanges(elementId: string): void {

    let leaveFunction: Function = (): void => {
      this.allowNavigation = true;

      setTimeout(function () {
        document.getElementById(elementId)?.click();
      }, 100);
      this.globalsService.currentChanges.next(false);
    }

    if (this.currentChanges) {
      let leaveButton: IActionButton = {
        label: 'Leave',
        callbackFunction: leaveFunction
      }

      let stayButton: IActionButton = {
        label: 'Stay',
        callbackFunction: (): void => {}
      }

      this.globalsService.displayPopupMessage(Utils.generatePopupMessageWithCallbacks('Confirm', '<p>You have unsaved changes on this page.</p><p>Are you sure you want to navigate away?</p>', [leaveButton, stayButton], true));

    }

  }

  setAuthenticatedUserProfile(): void {
    if (!this.authenticatedUser || this.allUsers.length < 1 || this.allProjects.length < 1)
      return;

    this.authenticatedUserProfile = this.allUsers.find(x => x.dempoid == this.authenticatedUser.netID) as User;
    let commHubProjects: IProjectMin[] = this.allProjects.filter(
      function (project) {
        if (project.projectDisplayId) {
          let ProjectDisplayId: string[] = project.projectDisplayId.split('|');
          if (ProjectDisplayId.includes('4'))
            return true;
          else
            return false;
        } else
          return false;
      });

    if (commHubProjects.length < 1)
      return;

    let commHubProjectIds: string[] = commHubProjects.map(x => (x.projectID || '').toString());

    if (Utils.arrayIncludesAny(this.authenticatedUserProfile.trainedOnArray, commHubProjectIds))
      this.trainedOnCommHubProject = true;

    //DEBUG
    //this.authenticatedUser.Admin = false;
    //this.authenticatedUser.ResourceGroup = false;
    //this.authenticatedUser.Interviewer = true;
    //this.trainedOnCommHubProject = false;
  }

}
