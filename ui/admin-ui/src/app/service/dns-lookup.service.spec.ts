import {TestBed} from '@angular/core/testing';

import {DnsLookupService} from './dns-lookup.service';

describe('DnsLookupService', () => {
    let service: DnsLookupService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(DnsLookupService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
