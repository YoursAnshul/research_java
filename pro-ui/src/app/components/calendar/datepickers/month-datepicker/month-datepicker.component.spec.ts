import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MonthDatepickerComponent } from './month-datepicker.component';

describe('MonthDatepickerComponent', () => {
  let component: MonthDatepickerComponent;
  let fixture: ComponentFixture<MonthDatepickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MonthDatepickerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MonthDatepickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
