import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { DefPro, IProjectMin } from '../../interfaces/interfaces';

@Component({
  selector: 'app-trained-on-projects',
  templateUrl: './trained-on-projects.component.html',
  styleUrls: ['./trained-on-projects.component.css'],
})
export class TrainedOnProjectsComponent implements OnInit {
  @Input() trainedOnProjects!: IProjectMin[];
  @Input() defPro!: DefPro[];
  @Input() notTrainedOnProjects!: IProjectMin[];
  @Input() listingOnly!: boolean;
  @Output() trainedOnChange = new EventEmitter<IProjectMin>();
  projectToAdd!: IProjectMin | null;
  projectToRemove!: IProjectMin | null;
  defaultproject: number = 0;

  constructor() {}

  ngOnInit(): void {
    this.defaultproject = this.defPro[0].defaultproject;
    const trainedProjectIds = new Set(
      this.trainedOnProjects.map((p) => p.projectID)
    );
    if (this.trainedOnProjects.length === 1) {
      const singleProject = this.trainedOnProjects[0];
      this.toggleClickState(singleProject);
    }
    this.notTrainedOnProjects = this.notTrainedOnProjects.filter(
      (project) => !trainedProjectIds.has(project.projectID)
    );
  }

  addTrainedOn(): void {
    if (!this.projectToAdd) {
      return;
    }
    if (this.projectAlreadyExists(this.projectToAdd.projectID)) {
      return;
    }
    this.projectToAdd.selected = false;
    this.trainedOnProjects.push(this.projectToAdd);
    this.notTrainedOnProjects.splice(
      this.notTrainedOnProjects.indexOf(this.projectToAdd),
      1
    );
    this.trainedOnChange.emit(this.projectToAdd);
    this.toggleClickState(this.projectToAdd);
    this.projectToAdd = null;
  }

  removeTrainedOn(): void {
    if (!this.projectToRemove) {
      return;
    }
    this.projectToRemove.selected = false;

    this.trainedOnProjects.splice(
      this.trainedOnProjects.indexOf(this.projectToRemove),
      1
    );
    this.notTrainedOnProjects.push(this.projectToRemove);
    this.notTrainedOnProjects.sort((a, b) =>
      a.projectName.localeCompare(b.projectName)
    );
    this.trainedOnChange.emit(this.projectToRemove);
    if (this.trainedOnProjects.length === 1) {
      let singleProject = this.trainedOnProjects[0];
      singleProject.clicked = true;
      this.toggleClickState(singleProject);
    } else {
      this.toggleClickState(this.projectToRemove);
    }
    this.projectToRemove = null;
  }

  setProjectToAdd(projectToAdd: IProjectMin): void {
    this.projectToAdd = projectToAdd;
    this.projectToRemove = null;
    this.notTrainedOnProjects.forEach(function (project, index) {
      if (project.projectID == projectToAdd.projectID) {
        project.selected = true;
      } else {
        project.selected = false;
      }
    });
  }

  setProjectToRemove(projectToRemove: IProjectMin): void {
    this.projectToRemove = projectToRemove;
    this.projectToAdd = null;
    this.trainedOnProjects.forEach(function (project, index) {
      if (project.projectID == projectToRemove.projectID) {
        project.selected = true;
      } else {
        project.selected = false;
      }
    });
  }

  buttonAllowed(addButton: boolean): boolean {
    if (addButton && !this.projectToAdd) {
      return true;
    }

    if (!addButton && !this.projectToRemove) {
      return true;
    }

    return false;
  }

  projectAlreadyExists(id: number) {
    return this.trainedOnProjects.some(function (el) {
      return el.projectID === id;
    });
  }

  public getSwatchColor(project: IProjectMin) {
    let color: string = project?.projectColor || '';
    return {
      'background-color': color,
    };
  }
  toggleClickState(projectToToggle: IProjectMin) {
    if (this.defaultproject === projectToToggle.projectID) {
      this.trainedOnProjects.forEach((project) => {
        project.clicked = false;
        project.defaultproject = 0;
      });
      projectToToggle.clicked = false;
      projectToToggle.defaultproject = 0;
      this.defaultproject = 0;
      this.trainedOnChange.emit(projectToToggle);
    } else {
      this.trainedOnProjects.forEach((project) => {
        project.clicked = false;
        project.defaultproject = 0;
      });
      projectToToggle.clicked = true;
      projectToToggle.defaultproject = projectToToggle.projectID;
      this.defaultproject = projectToToggle.projectID;
      this.trainedOnChange.emit(projectToToggle);
    }
  }
}
