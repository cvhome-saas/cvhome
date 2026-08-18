import {Component, input} from '@angular/core';

type ButtonTone = 'primary' | 'secondary' | 'ghost';

@Component({
  selector: 'app-button',
  template: `<ng-content />`,
  host: {
    '[class]': 'classes()',
  },
})
export class Button {
  readonly tone = input<ButtonTone>('primary');
  readonly compact = input(false);

  protected classes(): string {
    const base =
      'inline-flex items-center justify-center gap-2 rounded-full font-semibold transition';
    // 48px / 38px minimum targets, with the vertical padding the design system specifies.
    const size = this.compact()
      ? 'min-h-[38px] px-[1.05rem] py-[0.55rem] text-caption'
      : 'min-h-12 px-6 py-[0.8rem] text-body-compact';
    const tones: Record<ButtonTone, string> = {
      primary:
        'border border-primary bg-primary text-primary-foreground hover:bg-primary-emphasis hover:shadow-lift-primary',
      secondary:
        'border border-border bg-transparent text-foreground hover:border-edge hover:bg-accent hover:text-foreground-strong',
      ghost: 'border border-transparent bg-transparent text-current hover:bg-input',
    };
    return `${base} ${size} ${tones[this.tone()]}`;
  }
}
