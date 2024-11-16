import { Component, Input, OnInit, } from '@angular/core';
import { IMonthSchedules } from '../../../interfaces/interfaces';


@Component({
  selector: 'app-month-view',
  templateUrl: './month-view.component.html',
  styleUrls: ['./month-view.component.css']
})
export class MonthViewComponent implements OnInit {

  @Input() monthSchedules!: IMonthSchedules;
  
  constructor() { }
  
  ngOnInit(): void {
  }
}
