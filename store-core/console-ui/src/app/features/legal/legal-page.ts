import {Component, inject} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs';

/**
 * Terms and Privacy policy.
 *
 * TODO(lessons.md): legal documents have no source — see lessons.md, "Marketing — legal documents are unauthored".
 * seller-ui carries the same two routes and renders a decorative header with no text at all. Rather than repeat
 * that, or invent binding legal copy, the page states plainly that the document is not published yet and points at
 * the contact channels, which are real.
 */
@Component({
  selector: 'app-legal-page',
  imports: [RouterLink, TranslocoDirective],
  template: `
    <main class="legal" *transloco="let t">
      <a class="back" routerLink="/">{{ t('legal.backToHome') }}</a>
      <h1>{{ t('legal.' + document() + '.title') }}</h1>
      <p class="lede">{{ t('legal.' + document() + '.lede') }}</p>
      <p class="pending" role="status">{{ t('legal.pending') }}</p>
      <a class="contact" href="/#contact">{{ t('legal.contactUs') }}</a>
    </main>
  `,
  styleUrl: './legal-page.css',
})
export class LegalPage {
  private readonly route = inject(ActivatedRoute);

  /** `terms` or `privacy`, from the route so both documents share one component and one stylesheet. */
  protected readonly document = toSignal(
    this.route.data.pipe(map((data) => data['document'] as string)),
    {initialValue: 'terms'},
  );
}
