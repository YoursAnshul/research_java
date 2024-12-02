import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HoverMessageComponent } from './hover-message.component';

describe('HoverMessageComponent', () => {
  let component: HoverMessageComponent;
  let fixture: ComponentFixture<HoverMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [HoverMessageComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(HoverMessageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
