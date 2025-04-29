import {Component} from '@angular/core';

@Component({
    selector: 'ngx-subscription',
    standalone: false,
    styleUrls: ['subscription.component.scss'],
    template: `
        <router-outlet></router-outlet>
    `,
})
export class SubscriptionComponent {

    constructor() {
    }
}
