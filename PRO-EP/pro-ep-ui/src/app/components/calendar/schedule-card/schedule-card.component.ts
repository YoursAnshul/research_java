import { Component, Input, OnInit } from '@angular/core';
import { ISchedule } from '../../../interfaces/interfaces';

@Component({
  selector: 'app-schedule-card',
  templateUrl: './schedule-card.component.html',
  styleUrls: ['./schedule-card.component.css']
})
export class ScheduleCardComponent implements OnInit {

  @Input() schedule!: ISchedule;

  constructor() { }

  ngOnInit(): void {
  }

}
