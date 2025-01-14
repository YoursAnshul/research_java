import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DayDatepickerComponent } from './day-datepicker.component';

describe('DayDatepickerComponent', () => {
  let component: DayDatepickerComponent;
  let fixture: ComponentFixture<DayDatepickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DayDatepickerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DayDatepickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
