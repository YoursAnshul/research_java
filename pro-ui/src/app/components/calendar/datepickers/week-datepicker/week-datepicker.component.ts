import {Component, EventEmitter, Injectable, Input, OnInit, Output} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import { MAT_MOMENT_DATE_ADAPTER_OPTIONS, MomentDateAdapter } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import {
  DateRange,
  MAT_DATE_RANGE_SELECTION_STRATEGY,
  MatDatepicker,
  MatDateRangeSelectionStrategy
} from '@angular/material/datepicker';
import { Moment } from 'moment';
@Injectable()
export class WeekRangeSelectionStrategy<D> implements MatDateRangeSelectionStrategy<D> {
  constructor(private _dateAdapter: DateAdapter<D>) { }

  selectionFinished(date: D | null): DateRange<D> {
    return this._createWeekRange(date);
  }

  createPreview(activeDate: D | null): DateRange<D> {
    return this._createWeekRange(activeDate);
  }

  private _createWeekRange(date: D | null): DateRange<D> {
    if (date) {
      let thisDate: Date = new Date(this._dateAdapter.format(date, 'DD/MM/YYYY'));
      let startDay: number = thisDate.getUTCDate() - thisDate.getUTCDay() + 1; // First day is the day of the month - the day of the week (+ 1 for Monday)
      let endDay: number = startDay + 6; // last day is the first day + 6

      let startMonth: number = thisDate.getMonth() + 1;
      let endMonth: number = thisDate.getMonth() + 1;

      let startYear: number = thisDate.getFullYear();
      let endYear: number = thisDate.getFullYear();

      let currentMonthLength: number = this._dateAdapter.getNumDaysInMonth(date);

      if (startDay < 1) {
        let previousMonth: D = date;
        this._dateAdapter.addCalendarMonths(previousMonth, - 1);
        let previousMonthLength: number = this._dateAdapter.getNumDaysInMonth(previousMonth);
        startMonth = startMonth - 1;
        startDay = previousMonthLength + (startDay + 1); //+1 for Monday

        //set the year
        if (startMonth == 12) {
          startYear = startYear - 1;
        }
      }

      //if the last day goes into the next month, increment the month and subtract the lastDay from the current month's length
      if (endDay > currentMonthLength) {
        endMonth = endMonth + 1;
        endDay = endDay - currentMonthLength;

        //set the year
        if (endMonth == 1) {
          endYear = endYear + 1;
        }
      }

      let weekStart: D = this._dateAdapter.createDate(startYear, startMonth - 1, startDay);
      let weekEnd: D = this._dateAdapter.createDate(endYear, endMonth - 1, endDay);

      //console.log(weekStart + ' - ' + weekEnd);

      return new DateRange<D>(weekStart, weekEnd);
    }

    return new DateRange<D>(null, null);
  }
}
@Component({
  selector: 'app-week-datepicker',
  templateUrl: './week-datepicker.component.html',
  styleUrls: ['./week-datepicker.component.css'],
  providers: [
    {
      provide: MAT_DATE_RANGE_SELECTION_STRATEGY,
      useClass: WeekRangeSelectionStrategy
    }
  ],
})

export class WeekDatepickerComponent implements OnInit {
  @Input() selectedDate!: FormControl;
  @Output() selectedDateChange = new EventEmitter<FormControl>();
  @Input() selectedDateRange!: FormGroup;
  @Output() selectedDateRangeChange = new EventEmitter<FormGroup>();

  constructor() { }

  ngOnInit(): void {
  }

  //emit selected date
  emitSelectedDate(): void {
    let selectedDt: Date = new Date(this.selectedDateRange.value.start);
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
