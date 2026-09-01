/** Console-native; not a port from seller-core. */
import {ChangeDetectionStrategy, Component, computed, inject, signal} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import type {IconName} from '@shared/ui/icon/icon-paths';

import type {ManifestPreset, SavedSection} from '@models/layout';

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
        @if (facade.insertTarget() !== undefined) {
          <div class="target-banner">
            <span>{{ t('builder.library.insertingHere') }}</span>
            <button type="button" class="icon-action" [attr.aria-label]="t('builder.library.cancelInsert')"
                    [title]="t('builder.library.cancelInsert')" (click)="facade.insertTarget.set(undefined)">
              <app-icon name="x" [size]="14" />
            </button>
          </div>
        }
        <h3>{{ t('builder.library.presets') }}</h3>
        <div class="grid">
          @for (preset of presets(); track preset.id) {
            <button type="button" class="tile" draggable="true"
                    [class.lifting]="liftedKey() === 'preset:' + preset.id"
                    (dragstart)="dragPreset(preset, $event)" (dragend)="dragEnded()"
                    (click)="facade.addFromPreset(preset, facade.selectedId())">
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
              <button type="button" class="tile-main" draggable="true"
                      [class.lifting]="liftedKey() === 'saved:' + saved.id"
                      (dragstart)="dragSaved(saved, $event)" (dragend)="dragEnded()"
                      (click)="facade.addSavedSection(saved, facade.selectedId())">
                <app-icon [name]="icon(saved.kind)" [size]="18" />
                <span>{{ saved.name }}</span>
              </button>
              <button
                type="button"
                class="tile-delete"
                [title]="t('builder.library.deleteSaved')"
                [attr.aria-label]="t('builder.library.deleteSaved')"
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
      cursor: grab; font-size: 12px; font-weight: 600; text-align: start; color: var(--foreground);
      transition: border-color 0.15s, background 0.15s, transform 0.18s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.18s;
    }
    .tile:hover {
      border-color: var(--primary); background: var(--primary-soft, var(--muted));
      transform: translateY(-1px); box-shadow: var(--lift-subtle);
    }
    .tile:active { transform: translateY(0); }
    /* the picked-up tile stays behind as a dashed shadow of itself while its chip travels */
    .lifting, .tile:has(.tile-main.lifting) { opacity: 0.45; border-style: dashed; }
    .tile.saved { padding: 0; flex-direction: row; align-items: stretch; }
    .tile-main {
      flex: 1; display: flex; flex-direction: column; align-items: flex-start; gap: 6px;
      padding: 10px; border: 0; background: none; cursor: pointer; font: inherit; text-align: start; min-width: 0;
      color: var(--foreground);
    }
    .tile-delete { border: 0; background: none; cursor: pointer; color: var(--muted-foreground); padding: 0 8px; }
    .tile-delete:hover { color: var(--chart-5-foreground); }
    .empty { grid-column: 1 / -1; font-size: 12px; color: var(--muted-foreground); margin: 0; padding: 4px 2px; }
    .target-banner {
      display: flex; align-items: center; justify-content: space-between; gap: 8px;
      padding: 6px 6px 6px 10px; border-radius: var(--radius-md); font-size: 12px; font-weight: 600;
      background: var(--primary-soft, var(--muted)); color: var(--foreground);
    }
  `,
})
export class BuilderSectionLibrary {
  protected readonly facade = inject(BuilderFacade);

  /** The tile currently being dragged, left dimmed in place while its chip travels with the cursor. */
  protected readonly liftedKey = signal<string | null>(null);

  protected dragPreset(preset: ManifestPreset, event: DragEvent): void {
    event.dataTransfer?.setData('text/plain', preset.id);
    this.dragChip(event, this.label(preset.label));
    this.liftedKey.set(`preset:${preset.id}`);
    this.facade.startDrag({preset}, preset.kind, this.label(preset.label));
  }

  protected dragSaved(saved: SavedSection, event: DragEvent): void {
    event.dataTransfer?.setData('text/plain', String(saved.id));
    this.dragChip(event, saved.name);
    this.liftedKey.set(`saved:${saved.id}`);
    this.facade.startDrag({saved}, saved.kind, saved.name);
  }

  protected dragEnded(): void {
    this.liftedKey.set(null);
    this.facade.endDrag();
  }

  /** A compact labeled chip as the drag image, instead of the browser's washed-out tile snapshot. */
  private dragChip(event: DragEvent, label: string): void {
    const chip = document.createElement('div');
    chip.textContent = label;
    chip.style.cssText = 'position:fixed;top:-100px;inset-inline-start:0;padding:8px 16px;'
      + 'border-radius:999px;background:var(--primary);color:var(--primary-foreground);'
      + 'font:600 12px/1 system-ui;box-shadow:var(--lift);white-space:nowrap;';
    document.body.appendChild(chip);
    event.dataTransfer?.setDragImage(chip, 20, 16);
    setTimeout(() => chip.remove());
  }

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
