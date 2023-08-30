import {ComponentFixture, TestBed} from '@angular/core/testing';

import {LeftSideBarNavComponent} from './left-side-bar-nav.component';

describe('LeftSideBarNavComponent', () => {
  let component: LeftSideBarNavComponent;
  let fixture: ComponentFixture<LeftSideBarNavComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LeftSideBarNavComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(LeftSideBarNavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
