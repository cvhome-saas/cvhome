import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
    providedIn: 'root'
})
export class DnsLookupService {
    private DNS_URL: string = "https://dns.google/resolve";

    constructor(private httpClient: HttpClient) {
    }

    lookup(domain: string): Observable<DnsLookupResponse> {
        return this.httpClient.get<DnsLookupResponse>(this.getRequestUrl(domain))
    }

    getRequestUrl(domain: string): string {
        return this.DNS_URL + `?name=${domain}`;
    }
}

interface DnsLookupResponse {
    Status: number;
    TC: boolean;
    RD: boolean;
    RA: boolean;
    AD: boolean;
    CD: boolean;
    Question: Question[];
    Answer: Answer[];
    Authority: Answer[];
}

interface Answer {
    name: string;
    type: number;
    TTL: number;
    data: string;
}

interface Question {
    name: string;
    type: number;
}
