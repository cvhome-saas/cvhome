import {Component} from '@angular/core';

@Component({
    selector: 'store-ui-public',
    standalone: false,
    styleUrls: ['public.component.scss'],
    template: `
        <ngx-empty-layout>
            <router-outlet></router-outlet>
        </ngx-empty-layout>
    `,
})
export class PublicComponent {

}
