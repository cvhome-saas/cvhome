import {AfterViewInit, Component, OnDestroy, OnInit} from '@angular/core';
import {StoreService} from "../../../../../service/store.service";
import {map, mergeMap, tap} from "rxjs/operators";
import {ActivatedRoute} from "@angular/router";
import {Store} from "../../../../../domain/commons";
import {Subscription} from "rxjs";

@Component({
    selector: 'app-manage-store',
    templateUrl: './manage-store.component.html',
    styleUrls: ['./manage-store.component.css']
})
export class ManageStoreComponent implements OnInit, AfterViewInit, OnDestroy {
    currentStoreId: string = '';
    store: Store | undefined;
    storeInfoSubscription: Subscription | undefined;

    constructor(private activatedRoute: ActivatedRoute, private storeService: StoreService) {

    }


    ngAfterViewInit(): void {
        this.storeInfoSubscription = this.activatedRoute?.params
            .pipe(
                map(it => it['storeId']),
                tap(it => this.currentStoreId = it),
                mergeMap(it => this.storeService.getStoreInfo({id: it}))
            )
            .subscribe(it => {
                this.store = it
            });

    }

    ngOnDestroy(): void {
        if (this.storeInfoSubscription) {
            this.storeInfoSubscription.unsubscribe()
        }
    }

    ngOnInit(): void {
    }

}
