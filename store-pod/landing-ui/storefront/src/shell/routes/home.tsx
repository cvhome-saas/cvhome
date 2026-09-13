import type {ThemeDefinition} from '@store-front/theme';
import {loadHome} from '@/shell/loaders/home';
import {SectionList} from '@/shell/sections/section-list';
import {loadPageContext} from '@/shell/loaders/page-context';

/**
 * The home page is composed by the shell from the store's layout document — every store has one (the content
 * service materializes a starter default), so there is no theme-coded home any more. `?preview=<token>`
 * renders the draft for the builder's canvas.
 */
export function homePage(theme: ThemeDefinition) {
    return async function HomePage({searchParams}: {
        searchParams: Promise<Record<string, string | string[] | undefined>>;
    }) {
        const {preview} = await searchParams;
        const token = typeof preview === 'string' && preview ? preview : undefined;
        const [ctx, home] = await Promise.all([loadPageContext(theme), loadHome(token)]);
        return <SectionList theme={theme} ctx={ctx} home={home}/>;
    };
}
