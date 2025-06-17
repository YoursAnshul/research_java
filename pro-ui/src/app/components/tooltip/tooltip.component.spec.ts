import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TooltipComponent } from './tooltip.component';
import { Component } from '@angular/core';

@Component({
  template: `<app-tooltip text="Test tooltip" position="right"><button>Hover me</button></app-tooltip>`
})
class TestHostComponent {}

describe('TooltipComponent', () => {
  let component: TooltipComponent;
  let fixture: ComponentFixture<TooltipComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TooltipComponent, TestHostComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TooltipComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
}); 