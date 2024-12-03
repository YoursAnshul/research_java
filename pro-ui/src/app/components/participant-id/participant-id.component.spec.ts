import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParticipantIdComponent } from './participant-id.component';

describe('ParticipantIdComponent', () => {
  let component: ParticipantIdComponent;
  let fixture: ComponentFixture<ParticipantIdComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ParticipantIdComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ParticipantIdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
