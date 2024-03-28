import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ManageStoreUsersComponent} from './manage-store-users.component';

describe('ManageStoreUsersComponent', () => {
    let component: ManageStoreUsersComponent;
    let fixture: ComponentFixture<ManageStoreUsersComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ManageStoreUsersComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ManageStoreUsersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
