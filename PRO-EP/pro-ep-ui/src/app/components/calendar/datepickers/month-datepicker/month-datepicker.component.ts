import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MAT_MOMENT_DATE_ADAPTER_OPTIONS, MomentDateAdapter } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepicker } from '@angular/material/datepicker';
import { Moment } from 'moment';

@Component({
  selector: 'app-month-datepicker',
  templateUrl: './month-datepicker.component.html',
  styleUrls: ['./month-datepicker.component.css'],
  providers: [
    {
      provide: DateAdapter,
      useClass: MomentDateAdapter,
      deps: [MAT_DATE_LOCALE, MAT_MOMENT_DATE_ADAPTER_OPTIONS]
    },
    {
      provide: MAT_DATE_FORMATS, useValue: {
        parse: {
          dateInput: "MM/YYYY"
        },
        display: {
          dateInput: "MM/YYYY",
          monthYearLabel: "MMM YYYY",
          dateA11yLabel: "LL",
          monthYearA11yLabel: "MMMM YYYY",
        }
      },
    }
  ],
})

export class MonthDatepickerComponent implements OnInit {
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
    let selectedDt: Date = new Date(this.selectedDate.value);
    console.log(normalizedYear.year());
    selectedDt.setFullYear(normalizedYear.year());
    this.selectedDate.setValue(selectedDt);

    //emit the selected date because this month handler will not trigger the (dateChange) action
    this.emitSelectedDate();

    //console.log(normalizedYear);
  }

  //handle month selection
  chosenMonthHandler(normalizedMonth: Moment, datepicker: MatDatepicker<Moment>) {
    let selectedDt: Date = new Date(this.selectedDate.value);
    selectedDt.setFullYear(normalizedMonth.year());
    selectedDt.setMonth(normalizedMonth.month());
    selectedDt.setDate(1);
    this.selectedDate.setValue(selectedDt);

    //emit the selected date because this month handler will not trigger the (dateChange) action
    this.emitSelectedDate();

    //close the datepicker so we don't get prompted for a day
    datepicker.close();
    //console.log(normalizedMonth);
  }

  //handle day selection
  chosenDayHandler(normalizedDay: any, datepicker: MatDatepicker<Moment>) {
    //console.log(normalizedDay);
  }

}
