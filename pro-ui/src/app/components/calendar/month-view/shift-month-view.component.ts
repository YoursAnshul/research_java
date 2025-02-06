import { Component, Input, OnInit, } from '@angular/core';
import { IMonthSchedules } from '../../../interfaces/interfaces';


@Component({
  selector: 'app-shift-month-view',
  templateUrl: './shift-month-view.component.html',
  styleUrls: ['./shift-month-view.component.css']
})
export class ShiftMonthViewComponent implements OnInit {

  @Input() monthSchedules!: IMonthSchedules;
  
  constructor() { }
  
  ngOnInit(): void {
  }
}
