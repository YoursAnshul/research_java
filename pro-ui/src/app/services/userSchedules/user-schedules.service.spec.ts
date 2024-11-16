import { TestBed } from '@angular/core/testing';

import { UserSchedulesService } from './user-schedules.service';

describe('UserSchedulesService', () => {
  let service: UserSchedulesService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserSchedulesService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
