import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ManageStoreOrdersComponent} from "./manage-store-orders.component";


describe('ManageStoreOrdersComponent', () => {
    let component: ManageStoreOrdersComponent;
    let fixture: ComponentFixture<ManageStoreOrdersComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ManageStoreOrdersComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ManageStoreOrdersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
