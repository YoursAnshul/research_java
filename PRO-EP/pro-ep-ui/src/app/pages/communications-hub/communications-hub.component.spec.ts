import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommunicationsHubComponent } from './communications-hub.component';

describe('CommunicationsHubComponent', () => {
  let component: CommunicationsHubComponent;
  let fixture: ComponentFixture<CommunicationsHubComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CommunicationsHubComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CommunicationsHubComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
