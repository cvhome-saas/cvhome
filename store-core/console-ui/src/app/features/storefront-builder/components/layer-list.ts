/** Console-native; not a port from seller-core. */
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {CdkDrag, CdkDragDrop, CdkDragHandle, CdkDropList} from '@angular/cdk/drag-drop';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';

import {BuilderFacade} from '../facades/builder.facade';

/**
 * The page's sections as a draggable list — the builder's structural view. Reordering here is the
 * reorder; the canvas only mirrors it after the debounced save lands.
 */
@Component({
  selector: 'app-builder-layer-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CdkDropList, CdkDrag, CdkDragHandle, Icon, TranslocoDirective],
  template: `
    <ng-container *transloco="let t">
      <div class="layers" cdkDropList (cdkDropListDropped)="dropped($event)">
        @for (section of sections(); track section.id) {
          <div
            class="layer"
            cdkDrag
            [cdkDragDisabled]="section.locked === true"
            role="button"
            tabindex="0"
            [class.selected]="section.id === facade.selectedId()"
            [class.hovered]="section.id === facade.hoveredId()"
            [class.hidden-section]="section.visibility?.hidden"
            (click)="facade.selectedId.set(section.id)"
            (keyup.enter)="facade.selectedId.set(section.id)"
            (keyup.alt.arrowup)="facade.moveById(section.id, -1)"
            (keyup.alt.arrowdown)="facade.moveById(section.id, 1)"
            (mouseenter)="facade.hoveredId.set(section.id)"
            (mouseleave)="facade.hoveredId.set(null)"
          >
            @if (section.locked) {
              <span class="grip locked-glyph" [title]="t('builder.layers.locked')"><app-icon name="lock" [size]="13" /></span>
            } @else {
              <span class="grip" cdkDragHandle><app-icon name="grip" [size]="14" /></span>
            }
            <span class="layer-copy">
              <span class="layer-name">{{ name(section.kind) }}</span>
              <span class="layer-kind">{{ section.variant || t('builder.layers.defaultVariant') }}</span>
            </span>
            @if (facade.warningFor(section.id); as warning) {
              <span class="warn-dot" [title]="warning" aria-hidden="true"></span>
            }
            <button
              type="button"
              class="eye"
              [title]="t(section.visibility?.hidden ? 'builder.layers.show' : 'builder.layers.hide')"
              (click)="$event.stopPropagation(); facade.toggleHidden(section.id)"
            >
              <app-icon [name]="section.visibility?.hidden ? 'eyeOff' : 'eye'" [size]="14" />
            </button>
          </div>
        } @empty {
          <p class="empty">{{ t('builder.layers.empty') }}</p>
        }
      </div>
    </ng-container>
  `,
  styles: `
    .layers { display: flex; flex-direction: column; gap: 2px; padding: 8px; }
    .layer {
      display: flex; align-items: center; gap: 8px; padding: 8px 10px; border-radius: var(--radius-md);
      cursor: pointer; background: transparent;
    }
    .layer:hover { background: var(--muted); }
    .layer.selected { background: var(--primary-soft, var(--muted)); }
    .layer.hovered:not(.selected) { background: var(--muted); }
    .locked-glyph { cursor: default; }
    .warn-dot {
      width: 8px; height: 8px; border-radius: 50%; flex: none;
      background: var(--chart-4-foreground, var(--chart-5-foreground));
    }
    .layer.hidden-section .layer-copy { opacity: 0.45; }
    .grip { cursor: grab; color: var(--muted-foreground); display: inline-flex; }
    .layer-copy { display: flex; flex-direction: column; min-width: 0; flex: 1; }
    .layer-name { font-size: 13px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .layer-kind { font-size: 11px; color: var(--muted-foreground); }
    .eye { border: 0; background: none; cursor: pointer; color: var(--muted-foreground); display: inline-flex; padding: 4px; border-radius: var(--radius-md); }
    .eye:hover { background: var(--muted); }
    .empty { padding: 16px; font-size: 13px; color: var(--muted-foreground); }
    .cdk-drag-preview { border-radius: var(--radius-md); background: var(--background); }
    .cdk-drag-placeholder { opacity: 0.3; }
  `,
})
export class BuilderLayerList {
  protected readonly facade = inject(BuilderFacade);

  protected readonly sections = computed(() => this.facade.doc()?.sections ?? []);

  protected name(kind: string): string {
    const label = this.facade.kindSpec(kind)?.label;
    return label?.[this.facade.lang() === 'ar' ? 'ar' : 'en'] ?? kind;
  }

  protected dropped(event: CdkDragDrop<unknown>): void {
    if (event.previousIndex !== event.currentIndex) {
      this.facade.move(event.previousIndex, event.currentIndex);
    }
  }
}
