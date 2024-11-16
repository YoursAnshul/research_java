import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { ILegend, IMonthSchedules, IProject, ISchedule, IUserSchedule, IWeekSchedules } from '../../../interfaces/interfaces';
import { MatDialog } from '@angular/material/dialog';
import { DialogComponent } from '../../dialog/dialog.component';
import { ProjectsService } from '../../../services/projects/projects.service';
import { Subject, takeUntil } from 'rxjs';
import { GlobalsService } from '../../../services/globals/globals.service';

@Component({
  selector: 'app-legend',
  templateUrl: './legend.component.html',
  styleUrl: './legend.component.css',
  changeDetection: ChangeDetectionStrategy.Default
})
export class LegendComponent implements OnInit, OnChanges, OnDestroy {
  @ViewChild('dialogTemplate') dialogTemplate!: TemplateRef<any>;
  uniqueProjectDetails: ILegend[] = [];
  activeProjectDetails: ILegend[] = [];

  @Input() userSchedules!: IUserSchedule[];
  @Input() weekSchedules!: IWeekSchedules | null;
  @Input() monthSchedules!: IMonthSchedules;

  readonly unsub$ = new Subject<void>();

  constructor(
    private dialog: MatDialog, 
    private projectsService: ProjectsService,
    private globalsService: GlobalsService,
    private cDRef: ChangeDetectorRef,
  ) {}
  
  ngOnInit(): void {
  }
  
  ngOnChanges(changes: SimpleChanges): void {
    // passing the legends info to legend component
    this.uniqueProjectDetails.length = 0;
    if ('userSchedules' in changes) {
      this.userSchedules.forEach(obj => {
        obj.schedules.forEach(schedule => {
          if (!this.uniqueProjectDetails.some(exisingItem => exisingItem.projectName === schedule.projectName)) {
            this.uniqueProjectDetails.push(
              {
                projectName: schedule.projectName,
                projectColor: schedule.projectColor ?? '',
                projectid: schedule.projectid
              }
            );
          }
        })
      })
    }

    if ('weekSchedules' in changes) {
      if (this.weekSchedules) {
        for (const [key, value] of Object.entries(this.weekSchedules)) {
          if (key !== 'weekStart') {
            (value as ISchedule[]).forEach(schedule => {
              if (!this.uniqueProjectDetails.some(exisingItem => exisingItem.projectName === schedule.projectName)) {
                this.uniqueProjectDetails.push(
                  {
                    projectName: schedule.projectName,
                    projectColor: schedule.projectColor ?? '',
                    projectid: schedule.projectid
                  }
                );
              }
            })
          }
        }
      }
    }

    if ('monthSchedules' in changes) {
      if (this.monthSchedules?.weekSchedules.length) {
        this.monthSchedules.weekSchedules.forEach(weekSchedule => {
          for (const [key, value] of Object.entries(weekSchedule)) {
            if (key !== 'weekStart') {
              (value as ISchedule[]).forEach(schedule => {
                if (!this.uniqueProjectDetails.some(exisingItem => exisingItem.projectName === schedule.projectName)) {
                  this.uniqueProjectDetails.push(
                    {
                      projectName: schedule.projectName,
                      projectColor: schedule.projectColor ?? '',
                      projectid: schedule.projectid
                    }
                  );
                }
              })
            }
          }
        })
      }
    }

    this.uniqueProjectDetails.length ? this.uniqueProjectDetails.sort((a: ILegend, b:ILegend): number => {
      return (a.projectName?? '').localeCompare(b.projectName ?? '')
    }) : null;
  }

  ngOnDestroy(): void {
    this.unsub$.next();
    this.unsub$.complete();
  }

  openViewAllDialog(): void {
    this.projectsService.getAllActiveProjectDetails$().pipe(takeUntil(this.unsub$)).subscribe(res => {
      const activeProjects = res.Subject as IProject[];
      if (activeProjects.length) {
        //before pusing the data into this array need to empty this one
        this.activeProjectDetails.length = 0;
        activeProjects.forEach((value) => {
          //checking the project name already exist in the activeProjectDetails
          if (!this.activeProjectDetails.some(exisingItem => exisingItem.projectName === value.projectName)) {
            this.activeProjectDetails.push({
              projectName: value.projectName,
              projectColor: value.projectColor ?? '',
              projectid: value.projectID
            })
          }
        })
        this.dialog.open(DialogComponent, {
          width: '21.5%',
          data: {
            title: "Project Color Legend",
            content: this.activeProjectDetails,
            template: this.dialogTemplate,
          }
        })
      }
    })
  }
}
