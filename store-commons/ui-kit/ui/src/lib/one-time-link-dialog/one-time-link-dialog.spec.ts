import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {kitTranslocoTesting} from '@cvhome-saas/ui-kit/i18n';
import {OneTimeLinkDialog} from './one-time-link-dialog';

@Component({
  imports: [OneTimeLinkDialog],
  template: `
    <app-one-time-link-dialog
      [open]="open()"
      title="Invitation link"
      message="Send this to Ada."
      linkLabel="Invitation link"
      link="https://uaa.example/accept-invitation?token=abc"
      warning="Shown once."
      doneLabel="Done"
      (dismissed)="dismissed = dismissed + 1"
    />
  `,
})
class Host {
  readonly open = signal(false);
  dismissed = 0;
}

describe('OneTimeLinkDialog', () => {
  let fixture: ComponentFixture<Host>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Host, ...kitTranslocoTesting().imports],
      providers: [...kitTranslocoTesting().providers],
    }).compileComponents();
    fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
  });

  const dialog = (): HTMLDialogElement => fixture.nativeElement.querySelector('dialog');

  /** The state leads: the element opens and closes as `open` flips, and never on its own. */
  it('follows the open input', () => {
    expect(dialog().open).toBeFalse();
    fixture.componentInstance.open.set(true);
    fixture.detectChanges();
    expect(dialog().open).toBeTrue();
    fixture.componentInstance.open.set(false);
    fixture.detectChanges();
    expect(dialog().open).toBeFalse();
  });

  it('renders the link where the copy field can reach it', () => {
    fixture.componentInstance.open.set(true);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('code').textContent).toContain('token=abc');
  });

  /** Done asks the host to close rather than closing the element, so a second open still works. */
  it('emits dismissed from the done button and leaves the element to the host', () => {
    fixture.componentInstance.open.set(true);
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('.primary-action') as HTMLButtonElement).click();
    expect(fixture.componentInstance.dismissed).toBe(1);
    expect(dialog().open).toBeTrue();
  });
});
