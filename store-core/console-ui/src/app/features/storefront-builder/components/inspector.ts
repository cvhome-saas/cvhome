/** Console-native; not a port from seller-core. */
import {ChangeDetectionStrategy, Component, computed, inject, signal} from '@angular/core';
import {CdkDrag, CdkDragDrop, CdkDragHandle, CdkDropList} from '@angular/cdk/drag-drop';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {LayoutItem, LayoutSection, ManifestField, ManifestKind, ManifestLabel} from '@models/layout';
import type {MediaAsset} from '@models/content';
import {FormField, Icon, NumberField, Select, type SelectOption, TextField, TextareaField, Toggle} from '@cvhome-saas/ui-kit/ui';
import {BuilderMediaSelect} from './media-select';

import {BuilderFacade} from '../facades/builder.facade';

/** A section or one of its items — both hold `props` + localized `text`, and fields write to either. */
type FieldHolder = {props: Record<string, unknown>; text: LayoutSection['text']};

/**
 * The right pane: a form generated from the manifest's field DSL for the selected section — variant,
 * the kind's fields, the items editor for kinds with repeatable blocks, and the shared style knobs.
 * Localized fields edit `text[key][activeLang]`; everything else writes `props[key]`.
 */
@Component({
  selector: 'app-builder-inspector',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    BuilderMediaSelect, CdkDropList, CdkDrag, CdkDragHandle, FormField, Icon, NumberField, Select,
    TextField, TextareaField, Toggle, TranslocoDirective,
  ],
  templateUrl: './inspector.html',
  styleUrls: ['./inspector.css', '../../../shared/styles/field.css'],
})
export class BuilderInspector {
  protected readonly facade = inject(BuilderFacade);
  private readonly transloco = inject(TranslocoService);

  protected readonly section = this.facade.selected;

  protected readonly spec = computed<ManifestKind | undefined>(() => {
    const section = this.section();
    return section ? this.facade.kindSpec(section.kind) : undefined;
  });

  /** Which media field the picker dialog is open for: a section field key, or `item:<id>:<key>`. */
  protected readonly mediaFieldOpen = signal<string | null>(null);

  /** Item ids whose card is expanded. */
  protected readonly openItems = signal<ReadonlySet<string>>(new Set());

  protected readonly saveAsPresetOpen = signal(false);
  protected readonly presetName = signal('');

  protected label(label: ManifestLabel | undefined): string {
    if (!label) {
      return '';
    }
    return this.facade.lang() === 'ar' ? label.ar : label.en;
  }

  protected labelWithLang(field: ManifestField): string {
    const base = this.label(field.label);
    return field.localized ? `${base} · ${this.facade.lang()}` : base;
  }

  protected visible(field: ManifestField, section: LayoutSection): boolean {
    return !field.visibleIf || section.props[field.visibleIf.key] === field.visibleIf.equals;
  }

  protected variantOptions(): SelectOption[] {
    return (this.spec()?.variants ?? []).map((variant) => ({
      value: variant.id,
      label: this.label(variant.label),
      detail: variant.source === 'theme' ? this.transloco.translate('builder.inspector.themeVariant') : undefined,
    }));
  }

  protected selectOptions(field: ManifestField): SelectOption[] {
    return (field.options ?? []).map((option) => ({value: option.value, label: this.label(option.label)}));
  }

  protected styleOptions(keys: readonly string[], prefix: string): SelectOption[] {
    return keys.map((key) => ({value: key, label: this.transloco.translate(`builder.style.${prefix}${key}`)}));
  }

  // ------------------------------------------------------------------------------------- field values

  protected str(field: ManifestField, holder: FieldHolder): string {
    const value = field.localized ? holder.text[field.key]?.[this.facade.lang()] : holder.props[field.key];
    return value == null ? '' : String(value);
  }

  protected bool(field: ManifestField, holder: FieldHolder): boolean {
    return holder.props[field.key] === true;
  }

  protected numOf(field: ManifestField, holder: FieldHolder): number | null {
    const value = holder.props[field.key];
    return typeof value === 'number' ? value : null;
  }

  protected has(field: ManifestField, holder: FieldHolder): boolean {
    const value = field.localized ? holder.text[field.key]?.[this.facade.lang()] : holder.props[field.key];
    return value !== undefined && value !== null && value !== '';
  }

  protected setField(field: ManifestField, value: unknown, itemId?: string): void {
    const lang = this.facade.lang();
    const write = (holder: FieldHolder) => {
      if (field.localized) {
        holder.text = {...holder.text, [field.key]: {...(holder.text[field.key] ?? {}), [lang]: String(value ?? '')}};
      } else {
        holder.props = {...holder.props, [field.key]: value};
      }
    };
    if (itemId) {
      this.facade.updateSelectedItem(itemId, (item) => {
        write(item);
      });
    } else {
      this.facade.updateSelected((section) => {
        write(section);
      });
    }
  }

  // ------------------------------------------------------------------------------------------- links

  /** A `link` field's stored value; a legacy plain string reads as a URL link. */
  protected linkOf(field: ManifestField, holder: FieldHolder): {type: string; value: string} {
    const raw = holder.props[field.key];
    if (raw && typeof raw === 'object') {
      const link = raw as {type?: string; value?: string};
      return {type: link.type ?? 'url', value: link.value ?? ''};
    }
    return {type: 'url', value: typeof raw === 'string' ? raw : ''};
  }

  /** An emptied value clears the whole link — the storefront treats a value-less link as "points nowhere". */
  protected setLink(field: ManifestField, current: {type: string; value: string}, part: 'type' | 'value',
                    value: string, itemId?: string): void {
    const next = {...current, [part]: value};
    this.setField(field, next.value ? next : null, itemId);
  }

  protected linkTypeOptions(): SelectOption[] {
    return ['url', 'category', 'product'].map((key) =>
      ({value: key, label: this.transloco.translate(`builder.link.${key}`)}));
  }

  protected linkValueHint(type: string): string {
    return this.transloco.translate(`builder.link.${type}Hint`);
  }

  // ------------------------------------------------------------------------------------------- media

  protected openMedia(key: string, itemId?: string): void {
    this.mediaFieldOpen.set(itemId ? `item:${itemId}:${key}` : key);
  }

  /** The asset id the open media field currently holds, so the picker can mark it. */
  protected readonly mediaCurrentId = computed<number | null>(() => {
    const target = this.mediaFieldOpen();
    const section = this.facade.selected();
    if (!target || !section) {
      return null;
    }
    let holder: {props: Record<string, unknown>} | undefined = section;
    let key = target;
    if (target.startsWith('item:')) {
      const [, itemId, fieldKey] = target.split(':');
      holder = section.items?.find((item) => item.id === itemId);
      key = fieldKey;
    }
    const value = holder?.props[key];
    return typeof value === 'number' ? value : null;
  });

  protected mediaPicked(asset: MediaAsset): void {
    const target = this.mediaFieldOpen();
    this.mediaFieldOpen.set(null);
    if (!target) {
      return;
    }
    // The resolved URL rides beside the id under a key derived from the field, so a kind with two media
    // fields cannot have the second pick clobber the first's rendered URL. 'mediaId' keeps the storefront's
    // established 'mediaUrl' name.
    const urlKey = (key: string): string => (key === 'mediaId' ? 'mediaUrl' : `${key}Url`);
    if (target.startsWith('item:')) {
      const [, itemId, key] = target.split(':');
      this.facade.updateSelectedItem(itemId, (item) => {
        item.props = {...item.props, [key]: asset.id, [urlKey(key)]: asset.url};
      });
    } else {
      this.facade.updateSelected((section) => {
        section.props = {...section.props, [target]: asset.id, [urlKey(target)]: asset.url};
      });
    }
  }

  // ------------------------------------------------------------------------------------------- style

  protected setStyle(key: 'spacing' | 'width' | 'tone', value: string): void {
    this.facade.updateSelected((section) => {
      section.style = {...(section.style ?? {}), [key]: value};
    });
  }

  protected setVariant(value: string): void {
    this.facade.updateSelected((section) => {
      section.variant = value;
    });
  }

  // ---------------------------------------------------------------------------------- product source

  protected sourceType(section: LayoutSection): string {
    return (section.props['source'] as {type?: string} | undefined)?.type ?? 'group';
  }

  protected sourceCode(section: LayoutSection): string {
    return (section.props['source'] as {code?: string} | undefined)?.code ?? '';
  }

  protected sourceOptions(): SelectOption[] {
    return ['group', 'category', 'newest'].map((value) => ({
      value,
      label: this.transloco.translate(`builder.source.${value}`),
    }));
  }

  protected setSource(part: 'type' | 'code', value: string): void {
    this.facade.updateSelected((section) => {
      const source = {...((section.props['source'] as Record<string, unknown>) ?? {})};
      source[part] = value;
      if (part === 'type' && value === 'newest') {
        delete source['code'];
      }
      section.props = {...section.props, source};
    });
  }

  // ------------------------------------------------------------------------------------------- items

  protected toggleItem(id: string): void {
    this.openItems.update((open) => {
      const next = new Set(open);
      if (!next.delete(id)) {
        next.add(id);
      }
      return next;
    });
  }

  protected itemDropped(event: CdkDragDrop<unknown>): void {
    if (event.previousIndex !== event.currentIndex) {
      this.facade.moveItem(event.previousIndex, event.currentIndex);
    }
  }

  protected itemTitle(item: LayoutItem, index: number): string {
    const lang = this.facade.lang();
    const text = item.text;
    const first = Object.values(text)[0];
    return text['heading']?.[lang] ?? text['title']?.[lang] ?? text['quote']?.[lang]
      ?? (first ? Object.values(first)[0] : undefined) ?? `#${index + 1}`;
  }

  // ------------------------------------------------------------------------------------ saved preset

  protected savePreset(): void {
    const name = this.presetName().trim();
    if (name) {
      this.facade.saveSelectedAsPreset(name);
      this.saveAsPresetOpen.set(false);
      this.presetName.set('');
    }
  }
}
