import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageStoreLandingComponent } from './manage-store-landing.component';

describe('ManageStoreLandingComponent', () => {
  let component: ManageStoreLandingComponent;
  let fixture: ComponentFixture<ManageStoreLandingComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ManageStoreLandingComponent]
    });
    fixture = TestBed.createComponent(ManageStoreLandingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
