import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChannelsSettingsComponent } from './channels-settings.component';

describe('ChannelsSettingsComponent', () => {
  let component: ChannelsSettingsComponent;
  let fixture: ComponentFixture<ChannelsSettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ChannelsSettingsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChannelsSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
