import {NextResponse} from 'next/server';
import {SECTION_CATALOG, SECTION_PRESETS} from '@store-front/theme';
import {getTheme} from '@/shell/theme/get-theme';

/**
 * What the active theme can render, for the builder: the section catalogue merged with the theme's own
 * registry. The console generates its Add-section library and inspector forms from this — the manifest reads
 * the very registry the renderer resolves against, so the builder can never offer what the theme cannot draw.
 *
 * Public and store-scoped only through the `Theme` header spg injects, like every storefront render; CORS is
 * open because the console lives on another origin and the data is as public as the storefront itself.
 */
export async function GET() {
    const theme = await getTheme();
    const kinds = SECTION_CATALOG.map(spec => {
        const themed = theme.sections?.[spec.kind] ?? {};
        const themedIds = Object.keys(themed);
        const variants = [
            ...spec.variants.map(variant => ({
                ...variant,
                source: themedIds.includes(variant.id) ? 'theme' : 'fallback',
            })),
            ...themedIds
                .filter(id => !spec.variants.some(variant => variant.id === id))
                .map(id => ({id, label: {en: id, ar: id}, source: 'theme' as const, exclusive: true})),
        ];
        return {
            kind: spec.kind,
            label: spec.label,
            icon: spec.icon,
            variants,
            fields: spec.fields,
            itemFields: spec.itemFields,
            itemLabel: spec.itemLabel,
            maxItems: spec.maxItems,
        };
    });
    return NextResponse.json({themeId: theme.id, kinds, presets: SECTION_PRESETS}, {
        headers: {
            // private: the body varies on the spg-injected Theme header and the dev override cookie,
            // which a shared cache keyed only on the URL would happily mix across stores
            'Cache-Control': 'private, max-age=60, stale-while-revalidate=60',
            'Vary': 'Theme, Cookie',
            'Access-Control-Allow-Origin': '*',
        },
    });
}
