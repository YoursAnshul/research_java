import { Component, EventEmitter, Injectable, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MAT_MOMENT_DATE_ADAPTER_OPTIONS, MomentDateAdapter } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { DateRange, MatDatepicker, MatDateRangeSelectionStrategy, MAT_DATE_RANGE_SELECTION_STRATEGY } from '@angular/material/datepicker';
import { Moment } from 'moment';


@Component({
  selector: 'app-range-datepicker',
  templateUrl: './range-datepicker.component.html',
  styleUrls: ['./range-datepicker.component.css']
})
export class RangeDatepickerComponent implements OnInit {
  @Input() selectedDate!: FormControl;
  @Output() selectedDateChange = new EventEmitter<FormControl>();
  @Input() selectedDateRange!: FormGroup;
  @Output() selectedDateRangeChange = new EventEmitter<FormGroup>();

  constructor() { }

  ngOnInit(): void {

    this.selectedDate = new FormControl();

    if (!this.selectedDateRange) {
      this.selectedDateRange = new FormGroup({
        start: new FormControl(),
        end: new FormControl(),
      });
    }

  }

  //emit selected date
  emitSelectedDate(): void {
    let selectedDt: Date = new Date(this.selectedDateRange.value.start);
    this.selectedDate.setValue(selectedDt);
    this.selectedDateChange.emit(this.selectedDate);

    this.selectedDateRangeChange.emit(this.selectedDateRange);
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
  chosenDayHandler(normalizedDay: Moment, datepicker: MatDatepicker<Moment>) {
    //console.log(normalizedDay);
  }
}
