import {TestBed} from '@angular/core/testing';

import {DomainOwnershipService} from './domain-ownership.service';

describe('DomainOwnershipService', () => {
  let service: DomainOwnershipService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DomainOwnershipService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
