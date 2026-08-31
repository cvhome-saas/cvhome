/** Console-native; not a port from seller-core. */
import {ChangeDetectionStrategy, Component, computed, inject} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import type {IconName} from '@shared/ui/icon/icon-paths';

import {BuilderFacade} from '../facades/builder.facade';

/**
 * The Add-section library: the theme manifest's presets (finished-looking starting points, never bare
 * kinds) and the merchant's own saved sections. Insert lands after the current selection.
 */
@Component({
  selector: 'app-builder-section-library',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icon, TranslocoDirective],
  template: `
    <ng-container *transloco="let t">
      <div class="library">
        <h3>{{ t('builder.library.presets') }}</h3>
        <div class="grid">
          @for (preset of presets(); track preset.id) {
            <button type="button" class="tile" (click)="facade.addFromPreset(preset, facade.selectedId())">
              <app-icon [name]="icon(preset.kind)" [size]="18" />
              <span>{{ label(preset.label) }}</span>
            </button>
          } @empty {
            <p class="empty">{{ t('builder.library.noManifest') }}</p>
          }
        </div>

        <h3>{{ t('builder.library.mySections') }}</h3>
        <div class="grid">
          @for (saved of facade.savedSections(); track saved.id) {
            <div class="tile saved">
              <button type="button" class="tile-main" (click)="facade.addSavedSection(saved, facade.selectedId())">
                <app-icon [name]="icon(saved.kind)" [size]="18" />
                <span>{{ saved.name }}</span>
              </button>
              <button
                type="button"
                class="tile-delete"
                [title]="t('builder.library.deleteSaved')"
                (click)="facade.deleteSavedSection(saved.id)"
              >
                <app-icon name="trash" [size]="13" />
              </button>
            </div>
          } @empty {
            <p class="empty">{{ t('builder.library.noSaved') }}</p>
          }
        </div>
      </div>
    </ng-container>
  `,
  styles: `
    .library { padding: 10px; display: flex; flex-direction: column; gap: 8px; overflow-y: auto; }
    h3 {
      margin: 6px 2px 2px; font-size: 11px; font-weight: 700; letter-spacing: 0.06em;
      text-transform: uppercase; color: var(--muted-foreground);
    }
    .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
    .tile {
      position: relative; display: flex; flex-direction: column; align-items: flex-start; gap: 6px;
      padding: 10px; border: 1px solid var(--border); border-radius: var(--radius-md); background: var(--background);
      cursor: pointer; font-size: 12px; font-weight: 600; text-align: start; color: var(--foreground);
    }
    .tile:hover { border-color: var(--primary); background: var(--primary-soft, var(--muted)); }
    .tile.saved { padding: 0; flex-direction: row; align-items: stretch; }
    .tile-main {
      flex: 1; display: flex; flex-direction: column; align-items: flex-start; gap: 6px;
      padding: 10px; border: 0; background: none; cursor: pointer; font: inherit; text-align: start; min-width: 0;
      color: var(--foreground);
    }
    .tile-delete { border: 0; background: none; cursor: pointer; color: var(--muted-foreground); padding: 0 8px; }
    .tile-delete:hover { color: var(--chart-5-foreground); }
    .empty { grid-column: 1 / -1; font-size: 12px; color: var(--muted-foreground); margin: 0; padding: 4px 2px; }
  `,
})
export class BuilderSectionLibrary {
  protected readonly facade = inject(BuilderFacade);

  protected readonly presets = computed(() => this.facade.manifest()?.presets ?? []);

  protected label(label: {en: string; ar: string}): string {
    return this.facade.lang() === 'ar' ? label.ar : label.en;
  }

  /** The manifest speaks lucide-ish names; the console's icon set has its own. */
  private static readonly ICONS: Record<string, IconName> = {
    'image': 'images', 'shopping-bag': 'shoppingCart', 'layout-grid': 'layoutGrid', 'tag': 'tag',
    'align-left': 'fileEdit', 'help-circle': 'questionCircle', 'newspaper': 'file',
    'quote': 'messageCircle', 'mail': 'envelope', 'shield': 'shield', 'play': 'play', 'award': 'star',
  };

  protected icon(kind: string): IconName {
    const manifestIcon = this.facade.kindSpec(kind)?.icon;
    return (manifestIcon ? BuilderSectionLibrary.ICONS[manifestIcon] : undefined) ?? 'layoutGrid';
  }
}
