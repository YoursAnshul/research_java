import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectForecastingComponent } from './project-forecasting.component';

describe('ProjectForecastingComponent', () => {
  let component: ProjectForecastingComponent;
  let fixture: ComponentFixture<ProjectForecastingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ProjectForecastingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ProjectForecastingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
