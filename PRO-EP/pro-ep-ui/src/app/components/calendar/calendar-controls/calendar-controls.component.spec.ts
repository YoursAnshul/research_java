import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CalendarControlsComponent } from './calendar-controls.component';

describe('CalendarControlsComponent', () => {
  let component: CalendarControlsComponent;
  let fixture: ComponentFixture<CalendarControlsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CalendarControlsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CalendarControlsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
