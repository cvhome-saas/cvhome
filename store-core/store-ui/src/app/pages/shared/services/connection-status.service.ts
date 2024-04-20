import { Injectable } from "@angular/core";
import { Observable, timer } from "rxjs";
import { switchMap } from "rxjs/operators";
import { HttpClient } from "@angular/common/http";
import { environment } from '../../../../environments/environment';
import {Health} from "../models/health";

@Injectable({
    providedIn: 'root'
})
export class ConnectionStatusService {
    url = environment.apiUrl;
    constructor(private http: HttpClient) {

    }

    getStatusConnection(): Observable<Health> {
        const url = this.url.substring(0, this.url.length - 3)
        return timer(0, 30000)
            .pipe(
                switchMap(_ => this.http.get<Health>(`${url}actuator/health/ping`))
            );
    }
}
