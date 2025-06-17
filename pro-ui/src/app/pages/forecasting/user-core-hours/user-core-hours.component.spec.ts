import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserCoreHoursComponent } from './user-core-hours.component';

describe('UserCoreHoursComponent', () => {
  let component: UserCoreHoursComponent;
  let fixture: ComponentFixture<UserCoreHoursComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UserCoreHoursComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UserCoreHoursComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
