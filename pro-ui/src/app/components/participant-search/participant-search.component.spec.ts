import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParticipantSearchComponent } from './participant-search.component';

describe('ParticipantSearchComponent', () => {
  let component: ParticipantSearchComponent;
  let fixture: ComponentFixture<ParticipantSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ParticipantSearchComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ParticipantSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
