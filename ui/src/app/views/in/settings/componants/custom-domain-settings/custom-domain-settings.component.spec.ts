import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomDomainSettingsComponent } from './custom-domain-settings.component';

describe('CustomDomainSettingsComponent', () => {
  let component: CustomDomainSettingsComponent;
  let fixture: ComponentFixture<CustomDomainSettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CustomDomainSettingsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CustomDomainSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
