import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DomainSettingsComponent} from './domain-settings.component';

describe('DomainSettingsComponent', () => {
    let component: DomainSettingsComponent;
    let fixture: ComponentFixture<DomainSettingsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DomainSettingsComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(DomainSettingsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
