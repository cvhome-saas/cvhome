import {ComponentFixture, TestBed} from '@angular/core/testing';

import {StoreAutocompleteComponent} from './store-autocomplete.component';

describe('StoreAutocompleteComponent', () => {
  let component: StoreAutocompleteComponent;
  let fixture: ComponentFixture<StoreAutocompleteComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StoreAutocompleteComponent]
    });
    fixture = TestBed.createComponent(StoreAutocompleteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
