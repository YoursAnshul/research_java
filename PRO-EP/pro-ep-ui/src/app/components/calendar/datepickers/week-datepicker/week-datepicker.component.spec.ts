import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WeekDatepickerComponent } from './week-datepicker.component';

describe('WeekDatepickerComponent', () => {
  let component: WeekDatepickerComponent;
  let fixture: ComponentFixture<WeekDatepickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WeekDatepickerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WeekDatepickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
