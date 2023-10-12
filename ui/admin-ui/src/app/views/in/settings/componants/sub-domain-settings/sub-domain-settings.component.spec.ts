import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SubDomainSettingsComponent} from './sub-domain-settings.component';

describe('SubDomainSettingsComponent', () => {
    let component: SubDomainSettingsComponent;
    let fixture: ComponentFixture<SubDomainSettingsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [SubDomainSettingsComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(SubDomainSettingsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
