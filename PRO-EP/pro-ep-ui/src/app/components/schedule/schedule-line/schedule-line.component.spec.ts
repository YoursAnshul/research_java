import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduleLineComponent } from './schedule-line.component';

describe('ScheduleLineComponent', () => {
  let component: ScheduleLineComponent;
  let fixture: ComponentFixture<ScheduleLineComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScheduleLineComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScheduleLineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
