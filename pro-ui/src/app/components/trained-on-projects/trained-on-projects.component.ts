import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { DefPro,IProjectMin } from '../../interfaces/interfaces';
import { ProjectsService } from '../../services/projects/projects.service';

@Component({
  selector: 'app-trained-on-projects',
  templateUrl: './trained-on-projects.component.html',
  styleUrls: ['./trained-on-projects.component.css']
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
  private previousDefaultProject: number | null = null;
  isLoading = false; 
  @Input() newFlag: boolean = false; 
  constructor(private projectsService: ProjectsService
  ) { 
    this.projectsService.isLoading.subscribe(loading => {
      this.isLoading = loading;
    });
  }

    ngOnInit(): void {      
      if(this.defPro && this.defPro.length>0){
        this.defaultproject = this.defPro[0]?.defaultproject;
      }
      if (this.trainedOnProjects.length === 1) {
        let singleProject = this.trainedOnProjects[0];
        this.defaultproject =
          this.previousDefaultProject ?? singleProject.projectID;
        singleProject.defaultproject = this.defaultproject;
        singleProject.clicked = true;
        this.trainedOnChange.emit(singleProject);
      } 
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
    if (this.trainedOnProjects.length === 1) {
      let singleProject = this.trainedOnProjects[0];
      this.defaultproject =
        this.previousDefaultProject ?? singleProject.projectID;
      singleProject.defaultproject = this.defaultproject;
      singleProject.clicked = true;
      console.log("this.trainedOnProjects.length-----add if------------",this.trainedOnProjects.length);
      
      this.trainedOnChange.emit(this.projectToAdd);
    } else {
      if (this.projectToAdd.projectID === this.previousDefaultProject) {
        this.defaultproject = this.previousDefaultProject;
        this.projectToAdd.clicked = true;
      } else {
        this.projectToAdd.clicked = false;
      }
      this.projectToAdd.defaultproject = this.defaultproject;
      console.log("this.trainedOnProjects.length------add else-----------",this.trainedOnProjects.length);
      this.trainedOnChange.emit(this.projectToAdd);
    }
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
    if (this.trainedOnProjects.length === 1) {
      let singleProject = this.trainedOnProjects[0];
      this.defaultproject =
        this.previousDefaultProject ?? singleProject.projectID;
      singleProject.defaultproject = this.defaultproject;
      singleProject.clicked = true;
      console.log("this.trainedOnProjects.length-----rem if------------",this.trainedOnProjects.length);
      this.trainedOnChange.emit(singleProject);
    } 
    if (this.projectToRemove.projectID === this.defaultproject) {
      this.previousDefaultProject = this.defaultproject;
      this.defaultproject = 0;
      this.projectToRemove.clicked = false;
    }

    if (this.trainedOnProjects.length === 1) {
      let singleProject = this.trainedOnProjects[0];
      this.defaultproject = singleProject.projectID;
      singleProject.clicked = true;
    } else if (this.trainedOnProjects.length === 0) {
      this.defaultproject = 0;
    }

    this.projectToRemove.defaultproject = this.defaultproject;
    console.log("this.trainedOnProjects.length--------rem out---------",this.trainedOnProjects.length);
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
