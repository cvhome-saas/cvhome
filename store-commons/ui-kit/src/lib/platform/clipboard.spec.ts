import {copyText} from './clipboard';

/*
 * The console is reached over plain HTTP on a named host, so `navigator.clipboard` is undefined and
 * the selection fallback is not an edge case — it is the path every copy button takes in
 * development. These specs drive that path.
 */
describe('copyText', () => {
  let dialog: HTMLDialogElement | null = null;
  let carrierHost: string | null;

  beforeEach(() => {
    carrierHost = null;
    /*
     * `execCommand` is not implemented in a way that puts anything on a real clipboard here, so it
     * is replaced with a probe that records **where the selected element lived**. That is the thing
     * the bug was about: a carrier outside an open modal cannot be selected at all.
     */
    spyOn(document, 'execCommand').and.callFake(() => {
      const active = document.activeElement;
      const carrier = document.querySelector('textarea[aria-hidden="true"]');
      carrierHost = carrier?.parentElement?.tagName.toLowerCase() ?? null;
      return active !== null;
    });
  });

  afterEach(() => {
    dialog?.remove();
    dialog = null;
  });

  it('copies from the body when nothing is modal', async () => {
    const copied = await copyText('plain-value');

    expect(copied).toBeTrue();
    expect(carrierHost).toBe('body');
  });

  /*
   * The reported bug: `showModal()` puts the dialog in the top layer and makes the rest of the
   * document inert, so a carrier appended to `<body>` can never be selected. Every copy button
   * inside a dialog reported failure while the same control worked in a table.
   */
  it('copies from inside an open modal dialog rather than the body', async () => {
    dialog = document.createElement('dialog');
    document.body.appendChild(dialog);
    dialog.showModal();

    const copied = await copyText('http://console-ui.gateway.com:8000/accept-invitation?token=abc');

    expect(copied).toBeTrue();
    expect(carrierHost).toBe('dialog');
  });

  /* A non-modal `<dialog open>` does not make anything inert, so the body is still right. */
  it('ignores a dialog that is open but not modal', async () => {
    dialog = document.createElement('dialog');
    document.body.appendChild(dialog);
    dialog.show();

    const copied = await copyText('plain-value');

    expect(copied).toBeTrue();
    expect(carrierHost).toBe('body');
  });

  it('leaves nothing behind afterwards', async () => {
    await copyText('plain-value');

    expect(document.querySelector('textarea[aria-hidden="true"]')).toBeNull();
  });
});
