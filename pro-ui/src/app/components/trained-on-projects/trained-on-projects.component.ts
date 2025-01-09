import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { IProjectMin } from '../../interfaces/interfaces';

@Component({
  selector: 'app-trained-on-projects',
  templateUrl: './trained-on-projects.component.html',
  styleUrls: ['./trained-on-projects.component.css']
})
export class TrainedOnProjectsComponent implements OnInit {
  @Input() trainedOnProjects!: IProjectMin[];
  @Input() notTrainedOnProjects!: IProjectMin[];
  @Input() listingOnly!: boolean;
  @Output() trainedOnChange = new EventEmitter<IProjectMin>();
  projectToAdd!: IProjectMin | null;
  projectToRemove!: IProjectMin | null;

  constructor() { }

    ngOnInit(): void {
        const trainedProjectIds = new Set(this.trainedOnProjects.map(p => p.projectID));
        this.notTrainedOnProjects = this.notTrainedOnProjects.filter(project =>
            !trainedProjectIds.has(project.projectID));
    }

  addTrainedOn(): void {
    if (!this.projectToAdd) {
      return;
    }
    if(this.projectAlreadyExists(this.projectToAdd.projectID)){
      return;
    }

    this.projectToAdd.selected = false;

    this.trainedOnProjects.push(this.projectToAdd);
    this.notTrainedOnProjects.splice(this.notTrainedOnProjects.indexOf(this.projectToAdd), 1);
    this.trainedOnChange.emit(this.projectToAdd);
    this.projectToAdd = null;
  }

  removeTrainedOn(): void {
    if (!this.projectToRemove) {
      return;
    }

    this.projectToRemove.selected = false;

    this.trainedOnProjects.splice(this.trainedOnProjects.indexOf(this.projectToRemove), 1);
    this.notTrainedOnProjects.push(this.projectToRemove);
    this.notTrainedOnProjects.sort((a, b) => a.projectName.localeCompare(b.projectName));
    this.trainedOnChange.emit(this.projectToRemove);
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
    return this.trainedOnProjects.some(function(el) {
      return el.projectID === id;
    });
  }

  public getSwatchColor(project: IProjectMin) {
    let color: string = project?.projectColor || '';
    return {
      'background-color': color
    }
  }

}
