import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainedOnProjectsComponent } from './trained-on-projects.component';

describe('TrainedOnProjectsComponent', () => {
  let component: TrainedOnProjectsComponent;
  let fixture: ComponentFixture<TrainedOnProjectsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TrainedOnProjectsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TrainedOnProjectsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
