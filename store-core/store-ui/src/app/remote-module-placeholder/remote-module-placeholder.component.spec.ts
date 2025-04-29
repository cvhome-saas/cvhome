import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RemoteModulePlaceholderComponent } from './remote-module-placeholder.component';

describe('RemoteModulePlaceholderComponent', () => {
  let component: RemoteModulePlaceholderComponent;
  let fixture: ComponentFixture<RemoteModulePlaceholderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RemoteModulePlaceholderComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RemoteModulePlaceholderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
