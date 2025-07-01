import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
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
  selector: 'app-day-datepicker',
  templateUrl: './day-datepicker.component.html',
  styleUrls: ['./day-datepicker.component.css'],
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
          dateInput: 'MM/DD/YYYY',
        },
        display: {
          dateInput: 'MM/DD/YYYY',
          monthYearLabel: 'MMM YYYY',
          dateA11yLabel: 'LL',
          monthYearA11yLabel: 'MMMM YYYY',
        },
      },
    },
  ],
})
export class DayDatepickerComponent implements OnInit, AfterViewInit {
  @Input() selectedDate!: FormControl;
  @Output() selectedDateChange = new EventEmitter<FormControl>();
  public minDate: null | Date = null;
  constructor(private cdr: ChangeDetectorRef) {}
  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }
  ngOnInit(): void {
    const storedMinDate = localStorage.getItem('minSelectableDate');
    if (storedMinDate) {
      const parsedDate = new Date(storedMinDate);
      if (!isNaN(parsedDate.getTime())) {
        this.minDate = new Date(
          parsedDate.getFullYear(),
          parsedDate.getMonth(),
          1
        );

        const month = this.minDate.getMonth();
      }
    } else {
      const today = new Date();
      this.minDate = new Date(today.getFullYear(), today.getMonth(), 1);
    }
  }
  public dateFilter = (d: any): boolean => {
    if (!d || !this.minDate) return true;

    const date = d instanceof Date ? d : new Date(d);

    const dateMonth = date.getMonth();
    const dateYear = date.getFullYear();

    const blockedMonth = this.minDate.getMonth();
    const blockedYear = this.minDate.getFullYear();

    return !(dateMonth === blockedMonth && dateYear === blockedYear);
  };

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
  chosenMonthHandler(
    normalizedMonth: Moment,
    datepicker: MatDatepicker<Moment>
  ) {
    //datepicker.close();
    //console.log(normalizedMonth);
  }

  //handle day selection
  chosenDayHandler(normalizedDay: any, datepicker: MatDatepicker<Moment>) {
    //console.log(normalizedDay);
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === 'Tab') {
      this.onFinalDateInput(event);
    }
  }

  // onFinalDateInput(event: Event): void {
  //   const input = (event.target as HTMLInputElement).value;
  //   const parsed = moment(input, 'MM/DD/YYYY', true);
  //   if (parsed.isValid()) {
  //     const newDate = parsed.toDate();
  //     this.selectedDate.setValue(newDate);
  //   } else {
  //     console.warn('Invalid date input:', input);
  //     const now = new Date();
  //     this.selectedDate.setValue(now);
  //   }

  //   this.selectedDateChange.emit(this.selectedDate);
  // }

  onFinalDateInput(event: Event): void {
    const input = (event.target as HTMLInputElement).value;
    const parsed = moment(input, 'MM/DD/YYYY', true);

    if (parsed.isValid()) {
      const year = parsed.year();

      if (year === 1969 || year < 2024 || year > 2026) {
        console.warn('Restricted or invalid year detected:', year);
        this.selectedDate.setValue(new Date());
      } else {
        this.selectedDate.setValue(parsed.toDate());
      }
    } else {
      console.warn('Invalid date format or input:', input);
      this.selectedDate.setValue(new Date());
    }

    this.selectedDateChange.emit(this.selectedDate);
  }
}
