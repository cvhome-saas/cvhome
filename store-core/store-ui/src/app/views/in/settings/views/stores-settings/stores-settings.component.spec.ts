import {ComponentFixture, TestBed} from '@angular/core/testing';

import {StoresSettingsComponent} from './stores-settings.component';

describe('StoresSettingsComponent', () => {
    let component: StoresSettingsComponent;
    let fixture: ComponentFixture<StoresSettingsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [StoresSettingsComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(StoresSettingsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
