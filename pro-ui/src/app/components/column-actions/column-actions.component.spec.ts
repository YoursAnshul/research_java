import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ColumnActionsComponent } from './column-actions.component';

describe('ColumnActionsComponent', () => {
  let component: ColumnActionsComponent;
  let fixture: ComponentFixture<ColumnActionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ColumnActionsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ColumnActionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
