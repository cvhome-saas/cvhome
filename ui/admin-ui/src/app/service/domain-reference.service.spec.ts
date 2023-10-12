import {TestBed} from '@angular/core/testing';

import {DomainReferenceService} from './domain-reference.service';

describe('DomainReferenceService', () => {
    let service: DomainReferenceService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(DomainReferenceService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
