import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MAT_MOMENT_DATE_ADAPTER_OPTIONS, MomentDateAdapter } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepicker } from '@angular/material/datepicker';
import { Moment } from 'moment';

@Component({
  selector: 'app-day-datepicker',
  templateUrl: './day-datepicker.component.html',
  styleUrls: ['./day-datepicker.component.css'],
  providers: [
    {
      provide: DateAdapter,
      useClass: MomentDateAdapter,
      deps: [MAT_DATE_LOCALE, MAT_MOMENT_DATE_ADAPTER_OPTIONS]
    },
    {
      provide: MAT_DATE_FORMATS, useValue: {
        parse: {
          dateInput: "MM/DD/YYYY"
        },
        display: {
          dateInput: "MM/DD/YYYY",
          monthYearLabel: "MMM YYYY",
          dateA11yLabel: "LL",
          monthYearA11yLabel: "MMMM YYYY",
        }
      },
    }
  ],
})

export class DayDatepickerComponent implements OnInit {
  @Input() selectedDate!: FormControl;
  @Output() selectedDateChange = new EventEmitter<FormControl>();

  constructor() { }

  ngOnInit(): void {
  }

  //emit selected date
  emitSelectedDate(): void {
    let selectedDt: Date = new Date(this.selectedDate.value);
    this.selectedDate.setValue(selectedDt);
    this.selectedDateChange.emit(this.selectedDate);
  }

  //handle year selection
  chosenYearHandler(normalizedYear: Moment) {
    //console.log(normalizedYear);
  }

  //handle month selection
  chosenMonthHandler(normalizedMonth: Moment, datepicker: MatDatepicker<Moment>) {
    //datepicker.close();
    //console.log(normalizedMonth);
  }

  //handle day selection
  chosenDayHandler(normalizedDay: any, datepicker: MatDatepicker<Moment>) {
    //console.log(normalizedDay);
  }

}
