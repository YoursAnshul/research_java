import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnySelectComponent } from './any-select.component';

describe('AnySelectComponent', () => {
  let component: AnySelectComponent;
  let fixture: ComponentFixture<AnySelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AnySelectComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AnySelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
