import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Utils } from '../../classes/utils';
import { IActionButton, IAuthenticatedUser, IDropDownValue, IFormFieldInstance, IFormFieldVariable, IProject, IProjectMin, IUser, IUserMin } from '../../interfaces/interfaces';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { GlobalsService } from '../../services/globals/globals.service';
import { ProjectsService } from '../../services/projects/projects.service';
import { UsersService } from '../../services/users/users.service';

@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.css']
})
export class ProjectsComponent implements OnInit {

  authenticatedUser!: IAuthenticatedUser;
  allProjects!: IProjectMin[];
  activeProjects: IProjectMin[] = [];
  //activeProjectsDv: IDropDownValue[];

  filteredProjects: IProjectMin[] = [];
  selectedProjects: string[] = [];
  selectedProject!: IProject;

  showModalPopup: boolean = false;
  trainedOnRadio!: number;
  trainedOnRadioDisabled: boolean = true;
  selectAll: boolean = false;
  currentChanges: boolean = false;
  errorMessage!: string;

  noProjectsSelected: boolean = true;
  actionsControl: FormControl = new FormControl('0');

  //default to active projects
  projectListFilter: FormControl = new FormControl('2');

  constructor(private globalsService: GlobalsService,
    private authenticationService: AuthenticationService,
    private usersService: UsersService,
    private projectsService: ProjectsService) {

    //get active projects
    this.projectsService.allProjectsMin.subscribe(
      allProjects => {
        this.allProjects = allProjects;
        this.activeProjects = allProjects.filter(x => (x.active && x.projectType !== 'Administrative'));

        //this.filteredProjects = allProjects.filter(x => x.ProjectType !== 'Administrative');
        this.filterProjectList();

        if (!this.selectedProject?.projectID && this.activeProjects) {
          if (this.activeProjects.length > 0) {
            this.projectsService.setSelectedProject(this.activeProjects[0].projectID);
          }
        }

        //set selected project as the current project if we don't have one already selected
        if (!this.selectedProject && this.activeProjects) {
          if (this.activeProjects.length > 0) {
            this.setSelectedProject(this.activeProjects[0].projectID);
          }
        }
      }
    );

    //subscribe to the selected project
    this.projectsService.selectedProject.subscribe(
      project => {
        this.selectedProject = project;
      }
    );

    //subscribe to current data changes
    this.globalsService.currentChanges.subscribe(
      currentChanges => {
        this.currentChanges = currentChanges;
      }
    );

  }

  ngOnInit(): void {
    //set current page for navigation menu to track
    this.globalsService.selectedPage.next('projects');

    //subscribe to the authenticated user
    this.authenticationService.authenticatedUser.subscribe(
      authenticatedUser => {
        this.authenticatedUser = authenticatedUser;
      }
    );

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

  //add user
  addUser(): void {

  }

  formFieldStyle(index: number) {
    let remainder: number = 0;
    let display: string = 'block';

    if (index > 0) {
      remainder = index % 2;
    }

    if (remainder != 0) {
      display = 'inline-block';
    }

    return {
      'display': display,
    }
  }

  closeModalPopup(): void {
    if (this.showModalPopup) {
      if (confirm('You have unsaved changes. Are you sure you want to close the Add a New Project window?')) {
        this.showModalPopup = false;
      }
    }
  }

  //set selected project
  setSelectedProject(projectId: number): void {
    //get user to bind form field values
    this.projectsService.setSelectedProject(projectId);
  }

  //confirmation before going to another user if changes were made
  confirmIfChanges(projectId: number): void {

    let leaveFunction: Function = (): void => {
      this.setSelectedProject(projectId);
      this.globalsService.currentChanges.next(false);
    }

    if (this.currentChanges) {
      let leaveButton: IActionButton = {
        label: 'Leave',
        callbackFunction: leaveFunction
      }

      let stayButton: IActionButton = {
        label: 'Stay',
        callbackFunction: (): void => { }
      }

      this.globalsService.displayPopupMessage(Utils.generatePopupMessageWithCallbacks('Confirm', '<p>You have unsaved changes for this project.</p><p>Are you sure you want to navigate away?</p>', [leaveButton, stayButton], true));

    } else {
      this.setSelectedProject(projectId);
    }

  }

  projectSaved(savedProject: IProject): void {
    this.projectsService.setAllProjectsMin();
  }

  projectCreated(newProject: IProject): void {
    this.projectsService.setAllProjectsMin();
    this.showModalPopup = false;
  }


  //filter project list
  filterProjectList(): void {

    //default to all users
    this.filteredProjects = this.allProjects;

    //filter users by status
    switch (this.projectListFilter.value) {
      case '1':
        //redundant per above default
        break;
      case '2':
        this.filteredProjects = this.allProjects.filter(x => (x.active && x.projectType !== 'Administrative'));
        break;
      case '3':
        this.filteredProjects = this.allProjects.filter(x => (!x.active && x.projectType !== 'Administrative'));
        break;
      case '4':
        this.filteredProjects = this.allProjects.filter(x => x.projectType == 'Administrative');
        break;
      default:
    }
  }


}
