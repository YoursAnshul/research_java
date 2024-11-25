import { Component, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { IProjectGroup } from '../../interfaces/interfaces';
import { ProjectsService } from '../../services/projects/projects.service';
import { LogsService } from '../../services/logs/logs.service';
import { MatSort } from '@angular/material/sort';
import { FormControl } from '@angular/forms';

interface Participant {
  dateOfBirth: string;
  project: string;
  participantId: string;
  firstName: string;
  lastName: string;
  // Add other fields as needed
}

@Component({
  selector: 'app-participant-search',
  templateUrl: './participant-search.component.html',
  styleUrls: ['./participant-search.component.css']
})
export class ParticipantSearchComponent {
  //FILTERS
  phoneNumber: FormControl = new FormControl('');
  firstName: FormControl = new FormControl('');
  lastName: FormControl = new FormControl('');
  project: FormControl = new FormControl('');

  displayedColumns: string[] = ['dateOfBirth', 'project', 'participantId', 'participantFName', 'participantLName', 'phoneId', 'phoneType', 'contactSource', 'contactFName', 'contactLName', 'edit']; // Define all displayed columns
  dataSource = new MatTableDataSource<Participant>([]);
  selectedParticipant: Participant | null = null;
  projectGroups: IProjectGroup[] = [];
  // Example data
  participants: Participant[] = [
    { dateOfBirth: '09/13/1984', project: 'Project Eleven', participantId: '02-89932', firstName: 'Kyle', lastName: 'McCarthy' },
    // Add more participants here...
  ];
  //GENERAL
  errorMessage!: string;
  //@ViewChild(MatSort) sort: MatSort;

  constructor(private projectsService: ProjectsService, private logsService: LogsService) {
    this.phoneNumber.valueChanges.subscribe((newValue) => {
      console.log("Phone  : ", newValue)
    });
    this.firstName.valueChanges.subscribe((newValue) => {
      console.log("firstName  : ", newValue)
    });
    this.lastName.valueChanges.subscribe((newValue) => {
      console.log("lastName  : ", newValue)
    });
    this.project.valueChanges.subscribe((newValue) => {
      console.log("project  : ", newValue)
    });
    //subscribe to projects
    this.projectsService.allProjectsMin.subscribe(
      projects => {
        const _projects = projects.filter(x => x.active);
        let projectGroup: IProjectGroup = {
          name: 'Projects',
          projects: _projects.filter(x => x.projectType !== 'Administrative')
        };

        let adminGroup: IProjectGroup = {
          name: 'Admin',
          projects: _projects.filter(x => x.projectType == 'Administrative')
        };

        this.projectGroups = [projectGroup, adminGroup];
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
    this.dataSource.data =[...this.dataSource.data, ...this.participants]
  }

  reset() {
    // Filter participants based on search criteria
    this.dataSource.data =[...this.dataSource.data, ...this.participants]
    this.phoneNumber.reset();
    this.firstName.reset();
    this.lastName.reset();
    this.project.reset();
  }

  selectRow(row: Participant) {
    this.selectedParticipant = row;
  }

  ngAfterViewInit() {
    //this.dataSource.sort = this.sort;
  }
}
