import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ManageStoreCatalogComponent} from './manage-store-catalog.component';

describe('ManageStoreCatalogComponent', () => {
    let component: ManageStoreCatalogComponent;
    let fixture: ComponentFixture<ManageStoreCatalogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ManageStoreCatalogComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ManageStoreCatalogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
