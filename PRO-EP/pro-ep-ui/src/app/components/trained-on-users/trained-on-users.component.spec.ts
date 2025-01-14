import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainedOnUsersComponent } from './trained-on-users.component';

describe('TrainedOnUsersComponent', () => {
  let component: TrainedOnUsersComponent;
  let fixture: ComponentFixture<TrainedOnUsersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TrainedOnUsersComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TrainedOnUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
