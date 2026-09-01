import type {LayoutSectionData} from '@store-front/types';
import type {PageContext, ThemeDefinition} from '@store-front/theme';
import type {HomeLayoutData} from '@/shell/loaders/home';
import {resolveRenderer} from './resolve-renderer';
import {BuilderBridge} from './builder-bridge';

const SPACING: Record<string, string> = {none: '', sm: 'py-3', md: 'py-6 lg:py-8', lg: 'py-10 lg:py-14'};

const WIDTH: Record<string, string> = {
    content: 'mx-auto w-full max-w-(--container-content) px-4',
    wide: 'mx-auto w-full max-w-(--container-wide) px-4',
    full: 'w-full',
};

const TONE: Record<string, string> = {
    default: '',
    muted: 'bg-muted',
    inverse: 'bg-primary text-primary-foreground',
};

/** Absent device classes hide the section on that breakpoint band; null/empty devices means all. */
function deviceClasses(section: LayoutSectionData): string {
    const devices = section.devices;
    if (!devices || devices.length === 0) return '';
    const classes: string[] = [];
    if (!devices.includes('mobile')) classes.push('max-md:hidden');
    if (!devices.includes('tablet')) classes.push('md:max-lg:hidden');
    if (!devices.includes('desktop')) classes.push('lg:hidden');
    return classes.join(' ');
}

/**
 * The home page, composed by the shell: every section of the store's layout document, drawn by the theme's
 * registered renderer or the shell fallback, inside the theme's Root. In preview (the builder's iframe) each
 * section carries `data-section-id` and the postMessage bridge is mounted.
 */
export function SectionList({theme, ctx, home}: {
    theme: ThemeDefinition;
    ctx: PageContext;
    home: HomeLayoutData;
}) {
    const {layout, resolved, preview} = home;
    return (
        <>
            {preview && <BuilderBridge/>}
            {layout.sections.map(section => {
                const Renderer = resolveRenderer(theme, section);
                if (!Renderer) return null;
                const tone = TONE[section.style?.tone ?? 'default'] ?? '';
                const spacing = SPACING[section.style?.spacing ?? 'md'] ?? SPACING.md;
                const width = WIDTH[section.style?.width ?? 'content'] ?? WIDTH.content;
                return (
                    <section key={section.id} id={section.anchor ?? undefined}
                             data-section-id={preview ? section.id : undefined}
                             data-section-kind={preview ? section.kind : undefined}
                             className={`${tone} ${deviceClasses(section)}`}>
                        <div className={`${width} ${spacing}`}>
                            <Renderer ctx={ctx} section={section} data={resolved[section.id]} preview={preview}/>
                        </div>
                    </section>
                );
            })}
        </>
    );
}
