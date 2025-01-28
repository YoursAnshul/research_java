import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import {
  IDropDownValue,
  ILegend,
  IMonthSchedules,
  IProject,
  IProjectMin,
  ISchedule,
  IUserSchedule,
  IWeekSchedules,
} from '../../../interfaces/interfaces';
import { MatDialog } from '@angular/material/dialog';
import { DialogComponent } from '../../dialog/dialog.component';
import { ProjectsService } from '../../../services/projects/projects.service';
import { Subject, takeUntil } from 'rxjs';
import { GlobalsService } from '../../../services/globals/globals.service';
import { Utils } from '../../../classes/utils';
import { User } from '../../../models/data/user';

@Component({
  selector: 'app-legend',
  templateUrl: './legend.component.html',
  styleUrl: './legend.component.css',
  changeDetection: ChangeDetectionStrategy.Default,
})
export class LegendComponent implements OnInit, OnChanges, OnDestroy {
  @ViewChild('dialogTemplate') dialogTemplate!: TemplateRef<any>;
  uniqueProjectDetails: ILegend[] = [];
  activeProjectDetails: ILegend[] = [];

  @Input() userSchedules!: IUserSchedule[];
  @Input() weekSchedules!: IWeekSchedules | null;
  @Input() monthSchedules!: IMonthSchedules;
  activeProjects!: IProjectMin[];
  @Input() selectedUser!: User;

  readonly unsub$ = new Subject<void>();

  constructor(
    private dialog: MatDialog,
    private projectsService: ProjectsService,
    private globalsService: GlobalsService,
    private cDRef: ChangeDetectorRef
  ) {
    this.projectsService.allProjectsMin.subscribe((allProjects) => {
      this.activeProjects = allProjects.filter(
        (x) => x.active && x.projectType !== 'Administrative'
      );
    });
  }

  ngOnInit(): void {    
    if (this.activeProjects && this.selectedUser) {
      this.uniqueProjectDetails = [];
      for (let item of this.activeProjects) {
        if (item.projectID === this.selectedUser.defaultproject) {
          console.log('Item found:', item); 
          this.uniqueProjectDetails = [
            {
              projectName: item.projectName,
              projectColor: item.projectColor,
              projectid: item.projectID,
            },
          ];
        }
      }
    } else {
      console.log('activeProjects or selectedUser is undefined');
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (
      changes['userSchedules'] ||
      changes['weekSchedules'] ||
      changes['monthSchedules']
    ) {
      // Preserve initial defaults and clear only if changes exist
      const newUniqueProjectDetails: ILegend[] = [];

      if ('userSchedules' in changes && this.userSchedules) {
        this.userSchedules.forEach((obj) => {
          obj.schedules.forEach((schedule) => {
            if (
              !newUniqueProjectDetails.some(
                (item) => item.projectName === schedule.projectName
              )
            ) {
              newUniqueProjectDetails.push({
                projectName: schedule.projectName,
                projectColor: schedule.projectColor ?? '',
                projectid: schedule.projectid,
              });
            }
          });
        });
      }

      if ('weekSchedules' in changes && this.weekSchedules) {
        for (const [key, value] of Object.entries(this.weekSchedules)) {
          if (key !== 'weekStart') {
            (value as ISchedule[]).forEach((schedule) => {
              if (
                !newUniqueProjectDetails.some(
                  (item) => item.projectName === schedule.projectName
                )
              ) {
                newUniqueProjectDetails.push({
                  projectName: schedule.projectName,
                  projectColor: schedule.projectColor ?? '',
                  projectid: schedule.projectid,
                });
              }
            });
          }
        }
      }

      if (
        'monthSchedules' in changes &&
        this.monthSchedules?.weekSchedules.length
      ) {
        this.monthSchedules.weekSchedules.forEach((weekSchedule) => {
          for (const [key, value] of Object.entries(weekSchedule)) {
            if (key !== 'weekStart') {
              (value as ISchedule[]).forEach((schedule) => {
                if (
                  !newUniqueProjectDetails.some(
                    (item) => item.projectName === schedule.projectName
                  )
                ) {
                  newUniqueProjectDetails.push({
                    projectName: schedule.projectName,
                    projectColor: schedule.projectColor ?? '',
                    projectid: schedule.projectid,
                  });
                }
              });
            }
          }
        });
      }

      // If no new data is added, retain the original default values
      this.uniqueProjectDetails = newUniqueProjectDetails.length
        ? newUniqueProjectDetails
        : [...this.uniqueProjectDetails];

      // Sort the details if not empty
      if (this.uniqueProjectDetails.length) {
        this.uniqueProjectDetails.sort((a: ILegend, b: ILegend) => {
          return (a.projectName ?? '').localeCompare(b.projectName ?? '');
        });
      }
    }
  }

  ngOnDestroy(): void {
    this.unsub$.next();
    this.unsub$.complete();
  }

  openViewAllDialog(): void {
    this.projectsService
      .getAllActiveProjectDetails$()
      .pipe(takeUntil(this.unsub$))
      .subscribe((res) => {
        const activeProjects = res.Subject as IProject[];
        if (activeProjects.length) {
          //before pusing the data into this array need to empty this one
          this.activeProjectDetails.length = 0;
          activeProjects.forEach((value) => {
            //checking the project name already exist in the activeProjectDetails
            if (
              !this.activeProjectDetails.some(
                (exisingItem) => exisingItem.projectName === value.projectName
              )
            ) {
              this.activeProjectDetails.push({
                projectName: value.projectName,
                projectColor: value.projectColor ?? '',
                projectid: value.projectID,
              });
            }
          });
          this.dialog.open(DialogComponent, {
            width: '21.5%',
            data: {
              title: 'Project Color Legend',
              content: this.activeProjectDetails,
              template: this.dialogTemplate,
            },
          });
        }
      });
  }
}
