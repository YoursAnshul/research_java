import { Component, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { IProjectGroup } from '../../interfaces/interfaces';
import { ProjectsService } from '../../services/projects/projects.service';
import { LogsService } from '../../services/logs/logs.service';
import { MatSort } from '@angular/material/sort';
import { FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { ParticipantService } from '../../services/participant/participant.service';
import { MatPaginator } from '@angular/material/paginator';

interface Participant {
  dateOfBirth: string;
  project: string;
  participantId: string;
  participantFName: string;
  participantLName: string;
  phoneId: string;
  phoneType: string;
  contactSource: string;
  contactFName: string;
  contactLName: string;
  // Add other fields as needed
  assignedTo?: string;
  recordLocation?: string;
  lastContactDate?: string;
  participantName?: string;
  sex?: string;
  language?: string;
  site?: string;
  localTime?: string;
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

  participants: Participant[] = [];
  //GENERAL
  errorMessage!: string;
  //@ViewChild(MatSort) sort: MatSort;

  //Filters
  phoneNumberFilter: string = '';
  firstNameFilter: string = '';
  lastNameFilter: string = '';
  projectFilter: string[] = [];
  @ViewChild(MatSort) sort: MatSort | undefined;
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  showCancel: boolean = false;
  constructor(private projectsService: ProjectsService, private logsService: LogsService, private participantService: ParticipantService, private router: Router) {

    this.phoneNumber.valueChanges.subscribe((newValue) => {
      this.phoneNumberFilter = newValue?.toLowerCase();
      this.filter();
    });
    this.firstName.valueChanges.subscribe((newValue) => {
      this.firstNameFilter = newValue?.toLowerCase();
      this.filter();
    });
    this.lastName.valueChanges.subscribe((newValue) => {
      this.lastNameFilter = newValue?.toLowerCase();
      this.filter();
    });
    this.project.valueChanges.subscribe((newValue) => {
      if (newValue.length > this.projectFilter.length) {
        const addedValues = newValue.filter((v: any) => !this.projectFilter.includes(v.toLowerCase()));
        let updatedValue = [...newValue]; // Copy the new value

        if (addedValues.includes('0')) {
          // If '0' is selected with other values, retain only '0'
          updatedValue = ['0'];
        } else if (updatedValue.includes('0')) {
          // If other values are selected after '0', remove '0'
          updatedValue = updatedValue.filter((v: any) => v !== '0');
        }
        // Update the form control to reflect the new value
        this.project.setValue(updatedValue, { emitEvent: false });
        // Update projectFilter with the new value
        this.projectFilter = updatedValue.map((v: any) => v.toLowerCase());
      } else {
        // Update projectFilter with the new value
        this.projectFilter = newValue.map((v: any) => v.toLowerCase());
      }

      this.filter();
    });
    // Participant Lookup
    this.participantService.getLookup().subscribe(
      response => {
        if ((response.Status || '').toUpperCase() == 'SUCCESS') {
          this.participants = response.Subject.map((res: any) => {
            const { dob: dateOfBirth, project, participantno: participantId, participantfname: participantFName, participantlname: participantLName,
              phone: phoneId, phonetype: phoneType, contactsource: contactSource, contactfname: contactFName, contactlname: contactLName
            } = res;
            return {
              dateOfBirth,
              project,
              participantId,
              participantFName,
              participantLName,
              phoneId,
              phoneType,
              contactSource,
              contactFName,
              contactLName
            }
          });
          this.dataSource.data = [...this.participants];
        }
      }
    )
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
        this.logsService.logError(this.errorMessage);
      }
    );
  }
  // Method to check if the route contains 'search-participants'
  checkRouteContainsSearchParticipants() {
    const currentRoute = this.router.url; // Get the current URL
    if (!currentRoute.includes('search-participants')) {
      this.showCancel = true;
      console.log('Route contains "search-participants"');
      // Add your logic here
    } else {
      console.log('Route does not contain "search-participants"');
    }
  }
  reset() {
    // Filter participants based on search criteria
    this.phoneNumber.reset();
    this.firstName.reset();
    this.lastName.reset();
    this.project.reset();
    this.selectedParticipant = null;
  }

  filter() {
    let lookups = this.participants;
    if (this.firstNameFilter) {
      lookups = lookups.filter((lookup) => lookup?.participantFName?.toLowerCase().includes(this.firstNameFilter))
    }
    if (this.lastNameFilter) {
      lookups = lookups.filter((lookup) => lookup?.participantLName?.toLowerCase().includes(this.lastNameFilter))
    }
    if (this.phoneNumberFilter) {
      lookups = lookups.filter((lookup) => lookup?.phoneId?.toLowerCase().includes(this.phoneNumberFilter))
    }

    if (this.projectFilter && this.projectFilter?.length > 0) {
      lookups = lookups.filter((lookup) => this.projectFilter.some((filterTerm) => filterTerm === '0' || lookup.project.toLowerCase().includes(filterTerm)))
    }
    this.dataSource.data = lookups;
  }

  ngOnInit() {
    this.checkRouteContainsSearchParticipants();
  }
  selectRow(row: Participant) {
    this.selectedParticipant = {
      ...row,
      participantName: `${row.participantFName} ${row.participantLName}`
    };
  }

  openLinkInNewTab(id: string): void {
    const url = this.router.createUrlTree(['/participants', id]).toString();
    window.open(url, '_blank');
  }

  ngAfterViewInit() {
    this.dataSource.sort = this.sort!;
    this.dataSource.paginator = this.paginator;
  }

  moveToPage(event: MouseEvent, id: string): void {
    event.stopPropagation();
    const url = this.router.createUrlTree(['/participants', id]).toString();
    const width = 500;
    const height = 1000;
    const left = window.screen.width / 2 - width / 2;
    const top = window.screen.height / 2 - height / 2;

    window.open(
      url,
      "_blank",
      `width=${width},height=${height},top=${top},left=${left},resizable=no,scrollbars=no,toolbar=no,menubar=no,location=no,status=no`
    );
  }

  moveToParticipantsPage(event: MouseEvent): void {
    event.stopPropagation();
    const url = 'search-participants';
    const width = 500;
    const height = 1000;
    const left = window.screen.width / 2 - width / 2;
    const top = window.screen.height / 2 - height / 2;

    window.open(
      url,
      "_blank",
      `width=${width},height=${height},top=${top},left=${left},resizable=no,scrollbars=no,toolbar=no,menubar=no,location=no,status=no`
    );
  }

}
