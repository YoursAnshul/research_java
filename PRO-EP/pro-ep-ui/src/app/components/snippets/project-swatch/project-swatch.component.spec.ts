import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectSwatchComponent } from './project-swatch.component';

describe('ProjectSwatchComponent', () => {
  let component: ProjectSwatchComponent;
  let fixture: ComponentFixture<ProjectSwatchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectSwatchComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectSwatchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
