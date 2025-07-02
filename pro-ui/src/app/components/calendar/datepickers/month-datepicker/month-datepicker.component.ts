import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl } from '@angular/forms';
import {
  MAT_MOMENT_DATE_ADAPTER_OPTIONS,
  MomentDateAdapter,
} from '@angular/material-moment-adapter';
import {
  DateAdapter,
  MAT_DATE_FORMATS,
  MAT_DATE_LOCALE,
} from '@angular/material/core';
import { MatDatepicker } from '@angular/material/datepicker';
import moment, { Moment } from 'moment';
@Component({
  selector: 'app-month-datepicker',
  templateUrl: './month-datepicker.component.html',
  styleUrls: ['./month-datepicker.component.css'],
  providers: [
    {
      provide: DateAdapter,
      useClass: MomentDateAdapter,
      deps: [MAT_DATE_LOCALE, MAT_MOMENT_DATE_ADAPTER_OPTIONS],
    },
    {
      provide: MAT_DATE_FORMATS,
      useValue: {
        parse: {
          dateInput: 'MM/YYYY',
        },
        display: {
          dateInput: 'MM/YYYY',
          monthYearLabel: 'MMM YYYY',
          dateA11yLabel: 'LL',
          monthYearA11yLabel: 'MMMM YYYY',
        },
      },
    },
  ],
})
export class MonthDatepickerComponent implements OnInit {
  @Input() selectedDate!: FormControl;
  @Output() selectedDateChange = new EventEmitter<FormControl>();
  resuldDate: Date | null = null;
  constructor() {}

  ngOnInit(): void {
    const resultDateStr = localStorage.getItem('resultDate');
    if (resultDateStr) {
      this.resuldDate = new Date(resultDateStr);
      this.resuldDate.setMonth(this.resuldDate.getMonth() + 1);
    }
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
  chosenMonthHandler(
    normalizedMonth: Moment,
    datepicker: MatDatepicker<Moment>
  ) {
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

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === 'Tab') {
      this.onFinalMonthInput(event);
    }
  }

  onFinalMonthInput(event: Event): void {
    const inputValue = (event.target as HTMLInputElement).value;
    const parsed = moment(inputValue, 'MM/YYYY', true);

    if (parsed.isValid()) {
      const year = parsed.year();

      if (year === 1969 || year < 2024 || year > 2026) {
        console.warn('Restricted or invalid year in month input:', year);
        const now = moment().date(1);
        this.selectedDate.setValue(now.toDate());
      } else {
        parsed.date(1);
        this.selectedDate.setValue(parsed.toDate());
      }
    } else {
      console.warn('Invalid month input format:', inputValue);
      const now = moment().date(1);
      this.selectedDate.setValue(now.toDate());
    }

    this.selectedDateChange.emit(this.selectedDate);
  }
  dateFilter = (date: Moment | null): boolean => {
    if (!date || !this.resuldDate) return true;
    const min = moment(this.resuldDate).startOf('month');
    const current = moment(date).startOf('month');
    return !current.isSame(min, 'month');
  };
}
