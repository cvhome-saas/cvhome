import {Component} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink, TranslocoDirective],
  template: `
    <main class="not-found" *transloco="let t">
      <h1>{{ t('notFound.heading') }}</h1>
      <p>{{ t('notFound.copy') }}</p>
      <a routerLink="/">{{ t('notFound.back') }}</a>
    </main>
  `,
  styles: `
    .not-found {
      display: grid;
      min-block-size: 100vh;
      place-content: center;
      gap: 1rem;
      padding: 2rem;
      background: var(--background);
      color: var(--foreground);
      text-align: center;
    }

    h1 {
      margin: 0;
      color: var(--foreground-strong);
      font-size: clamp(2.4rem, 6vw, 4rem);
      font-weight: 300;
      letter-spacing: -0.035em;
    }

    p {
      margin: 0;
      color: var(--muted-foreground);
    }

    a {
      justify-self: center;
      min-block-size: 3rem;
      padding: 0.8rem 1.5rem;
      border-radius: var(--radius-full);
      background: var(--primary);
      color: var(--primary-foreground);
      font-weight: 700;
    }
  `,
})
export class NotFound {}
