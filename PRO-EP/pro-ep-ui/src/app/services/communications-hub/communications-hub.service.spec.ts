import { TestBed } from '@angular/core/testing';

import { CommunicationsHubService } from './communications-hub.service';

describe('CommunicationsHubService', () => {
  let service: CommunicationsHubService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CommunicationsHubService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
